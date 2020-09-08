#!/usr/bin/env groovy

// Java Backend Reference Jenkinsfile
def call(
  environment,
  gitCredentialsId,
  sonarqubeCredentialsId,
  argocdCredentialsId,
  artifactRepoCredentialsId,
  applicationName
) {

  String JENKINS_WORKER_IMAGE_JNLP    = 'quay.io/tssc/tssc-ci-agent-jenkins:latest'
  String JENKINS_WORKER_IMAGE_MAVEN   = 'quay.io/tssc/tssc-tool-maven:latest'
  String JENKINS_WORKER_IMAGE_BUILDAH = 'quay.io/tssc/tssc-tool-buildah:latest'
  String JENKINS_WORKER_IMAGE_ARGOCD  = 'quay.io/tssc/tssc-tool-argocd:latest'
  String JENKINS_WORKER_IMAGE_SKOPEO  = 'quay.io/tssc/tssc-tool-skopeo:latest'
  String JENKINS_WORKER_IMAGE_SONAR   = 'quay.io/tssc/tssc-tool-sonar:latest'
  String REGISTRY_SECRET_NAME         = 'quay-basic-auth'

  pipeline {

    agent {
      kubernetes {
        label "${applicationName}-${env.BUILD_ID}"
        cloud 'openshift'
        yaml """
  apiVersion: v1
  kind: Pod
  spec:
    serviceAccount: jenkins
    containers:
    - name: 'jnlp'
      image: "${JENKINS_WORKER_IMAGE_JNLP}"
      tty: true
    - name: 'maven'
      image: "${JENKINS_WORKER_IMAGE_MAVEN}"
      tty: true
      command: ['sh', '-c', 'cat']
    - name: 'buildah'
      image: "${JENKINS_WORKER_IMAGE_BUILDAH}"
      tty: true
      command: ['sh', '-c', 'cat']
      securityContext:
        privileged: true
    - name: 'argocd'
      image: "${JENKINS_WORKER_IMAGE_ARGOCD}"
      tty: true
      command: ['sh', '-c', 'cat']
    - name: 'skopeo'
      image: "${JENKINS_WORKER_IMAGE_SKOPEO}"
      tty: true
      volumeMounts:
      - mountPath: /home/tssc/
        name: quay-registry-secret
      command: ['sh', '-c', 'cat']
    - name: 'sonar'
      image: "${JENKINS_WORKER_IMAGE_SONAR}"
      tty: true
      command: ['sh', '-c', 'cat']
    - name: 'config-lint'
      image: 'quay.io/tssc/tssc-tool-config-lint:latest'
      tty: true
      command: ['sh', '-c', 'cat']
    volumes:
    - name: quay-registry-secret
      secret:
        defaultMode: 440
        secretName: ${REGISTRY_SECRET_NAME}
        items:
        - key: .dockerconfigjson
          path: .docker/config.json
  """
              }
          }

    stages {

      stage('Setup') {
        steps {

          sh """
            python -m venv tssc
            source tssc/bin/activate

            pip install --index-url https://test.pypi.org/simple/ --extra-index-url https://pypi.org/simple tssc --upgrade

          """
        } // steps
      } // stage

      stage('Generate Metadata') {
        steps {
          container('maven') {
            sh """
              source tssc/bin/activate
              python -m tssc -c cicd/tssc-config.yml --step generate-metadata --environment ${environment}
            """
          } // container
        } // steps
      } // stage

      stage('Tag Source Code with Metadata') {
        steps {
          container('maven') {
            withCredentials([usernamePassword(credentialsId: "${gitCredentialsId}", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
              sh """
                source tssc/bin/activate
                python -m tssc -c cicd/tssc-config.yml --step tag-source --step-config password=${GIT_PASSWORD} username=${GIT_USERNAME} --environment ${environment}
                """
            } // withCredentials
          } // container
        } // steps
      } // stage

      stage('Run Unit Tests (Maven)') {
        steps {
          container('maven') {
            sh """
              source tssc/bin/activate
              python -m tssc -c cicd/tssc-config.yml --step unit-test --environment ${environment}
            """
          } // container
        } // steps
      } // stage

      stage('Compile / Build / Package Application (Maven)') {
        steps {
          container('maven') {
            sh """
              source tssc/bin/activate
              python -m tssc -c cicd/tssc-config.yml --step package --environment ${environment}
            """
          } // container
        } // steps
      } // stage

      stage('Static Code Analysis') {
        steps {
          container('sonar') {
            withCredentials([usernamePassword(credentialsId: "${sonarqubeCredentialsId}", passwordVariable: 'SONAR_PASSWORD', usernameVariable: 'SONAR_USER')]) {
              sh """
                source tssc/bin/activate
                python -m tssc -c cicd/tssc-config.yml --step static-code-analysis --step-config password=${SONAR_PASSWORD} user=${SONAR_USER} --environment ${environment}
              """
            } // withCredentials
          } // container
        } // steps
      } // stage

      stage('Push Artifacts to Repository with Metadata') {
        steps {
          container('maven') {
            withCredentials([usernamePassword(credentialsId: "${artifactRepoCredentialsId}", passwordVariable: 'ARTIFACTORY_PASSWORD', usernameVariable: 'ARTIFACTORY_USERNAME')]) {
              sh """
                source tssc/bin/activate
                python -m tssc -c cicd/tssc-config.yml -s push-artifacts --step-config password=${ARTIFACTORY_PASSWORD} user=${ARTIFACTORY_USERNAME} --environment ${environment}
                """
            } // withCredentials
          } // container
        } // steps
      } // stage

      stage('Compose Container') {
        steps {
          container('buildah') {
            sh """
              source tssc/bin/activate
              python -m tssc -c cicd/tssc-config.yml --step create-container-image --environment ${environment}
            """

            } // container
          } // steps
        } // stage

      stage('Push New Container Image with Metadata') {
        steps {
          container('skopeo') {
            sh """
              source tssc/bin/activate
              python -m tssc -c cicd/tssc-config.yml --step push-container-image --environment ${environment}
            """
            } // container
        } // steps
      } // stage

      stage('Image Unit Testing (TBD)') {
        steps {
          echo '${STAGE_NAME}'
        } // steps
      } // stage

      stage('Static Image Scans') {

        parallel {
          stage('Static Compliance Image Scan (OpenSCAP)') {
            steps {
              echo '${STAGE_NAME}'
            } // steps
          } // stage

        stage('Static Vulnerability Image Scan (OpenSCAP)') {
          steps {
            echo '${STAGE_NAME}'
          } // steps
        } // stage
        } // parallel
      } // stage

      stage('Push Trusted Container Image with Metadata') {
        steps {
          echo '${STAGE_NAME}'
        } // steps
      } // stage

      stage('Deploy') {
        steps {
          container('argocd') {
            withCredentials([usernamePassword(credentialsId: "${gitCredentialsId}", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME'),
            usernamePassword(credentialsId: "${argocdCredentialsId}", passwordVariable: 'ARGOCD_PASSWORD', usernameVariable: 'ARGOCD_USERNAME')
            ]) {
              sh """
                source tssc/bin/activate
                python -m tssc -c cicd/tssc-config.yml --step deploy --step-config git-password=${GIT_PASSWORD} git-username=${GIT_USERNAME} argocd-password=${ARGOCD_PASSWORD} argocd-username=${ARGOCD_USERNAME} --environment ${environment}
                """
            } // withCredentials
          } // container
        } // steps
      } // stage

    } //stages

  } // pipeline

} // call
