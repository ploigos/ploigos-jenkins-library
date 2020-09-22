#!/usr/bin/env groovy

class PipelineInput implements Serializable {
    /* path relative to the root of the project going through the Workflow
     * to the directory containing the workflow configuration files. */
    String   configDir   = ''

    /* name of the "Development" enviornment used in the configuration files */
    String   envNameDev  = 'DEV'

    /* name of the "Test" enviornment used in the configuration files */
    String   envNameTest = 'TEST'

    /* name of the "Production" enviornment used in the configuration files */
    String   envNameProd = 'PROD'

    /* Array of regex patterns for branches that should be deployed to
     * Test and then Production environments. */
    String[] releaseBranchPatterns  = ['main']

    /* Array of regex patterns for branches that should be deployed to
     * Development environments */
    String[] devBranchPatterns      = ['^feature/.*$', '^PR-.*$']

    /* URI to the container registry hosting the Jenkins workers images used by this pipeline */
    String jenkinsWorkersImageRegesitryURI   = 'quay.io'

    /* Container image repository name hosting the Jenkins workers images used by this pipeline */
    String jenkinsWorkersImageRepositoryName = 'tssc'

    /* Container image tag to use for the Jenkins workers images used by this pipeline */
    String jenkinsWorkersImageTag            = 'latest'

    /* Policy for pulling new versions of the jenkinsWorkersImageTag when running this pipeline */
    String jenkinsWorkersImagePullPolicy     = 'IfNotPresent'

    /* If tsscLibSourceUrl is not supplied this will be passed to pip as --index-url
     * for installing the 'tssc' python library and its dependicies.
     *
     * NOTE:    PIP is indeterminate whether it will pull packages from
     *          --index-url or --extra-index-url, therefor be sure to specify tsscLibVersion
     *          if trying to pull a specific version from a specific index.
     *
     * @SEE https://pip.pypa.io/en/stable/reference/pip_install/#id48 */
    String tsscLibIndexUrl  = 'https://pypi.org/simple/'

    /* If tsscLibSourceUrl is not supplied this will be passed to pip as --extra-index-url
     * for installing the 'tssc' python library and its dependicies.
     *
     * NOTE:    PIP is indeterminate whether it will pull packages from
     *          --index-url or --extra-index-url, therefor be sure to specify tsscLibVersion
     *          if trying to pull a specific version from a specific index.
     *
     * @SEE https://pip.pypa.io/en/stable/reference/pip_install/#id48 */
    String tsscLibExtraIndexUrl = 'https://pypi.org/simple/'

    /* If tsscLibSourceUrl is not supplied this will be passed to pip as the version
     * of the 'tssc' library to install.
     *
     * NOTE:    If not given pip will install the latest from either --index-url or
     *          --extra-index-url indeterminantly */
    String tsscLibVersion   = null

    /* If given this will be used as the source location to install the 'tssc' library from
     * rather then from a PEP 503 compliant repository.
     *
     * EXAMPLE 1: git+https://github.com/rhtconsulting/tssc-python-package.git@feature/NAPSSPO-1018
     *            installs from the public 'rhtconsulting' fork from
     *            the 'feature/NAPSSPO-1018' branch.
     *
     * EXAMPLE 2: git+https://gitea.internal.example.xyz/tools/tssc-python-package.git@main
     *            installs from an internal fork of the 'tssc' library from the 'main' branch. */
    String tsscLibSourceUrl = null

    /* Jenkins Credential of type 'Secret file' storing PGP key to install.
     *
     * This is helpful if some of the projects configuration is encrypted with SOPS and
     * Jenkins is expected to use a PGP key to decrypt that configuration.
     *
     * @SEE https://www.jenkins.io/doc/book/using/using-credentials/#configuring-credentials */
    String credentialIDsopsPGPKey = 'sops-pgp-key'
}

// Java Backend Reference Jenkinsfile
def call(Map inputMap) {
    input = new PipelineInput(inputMap)

    String JENKINS_WORKER_IMAGE_JNLP       = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-ci-agent-jenkins:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_MAVEN      = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-maven:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_BUILDAH    = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-buildah:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_ARGOCD     = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-argocd:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_SKOPEO     = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-skopeo:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_SONAR      = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-sonar:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_CONFIGLINT = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-config-lint:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_OPENSCAP   = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-openscap:${input.jenkinsWorkersImageTag}"

    // SEE: https://stackoverflow.com/questions/25088034/use-git-repo-name-as-env-variable-in-jenkins-job
    String GIT_URL = scm.userRemoteConfigs[0].url
    String GIT_BRANCH = scm.branches[0].name.replaceAll(/[^a-zA-Z0-9]/, '-') // repalce everything that isnt a-zA-Z0-9 with -
    String GIT_REPO_NAME = "${GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')}"

    // determine the command to install the TSSC lib
    if(input.tsscLibSourceUrl) {
        TSSC_LIB_INSTALL_CMD = "python -m pip install --upgrade ${input.tsscLibSourceUrl}"
    } else {
        indexUrlFlag = ""
        if(input.tsscLibIndexUrl) {
            indexUrlFlag = "--index-url ${input.tsscLibIndexUrl}"
        }

        extraIndexUrlFlag = ""
        if(input.tsscLibExtraIndexUrl) {
            extraIndexUrlFlag = "--extra-index-url ${input.tsscLibExtraIndexUrl}"
        }

        TSSC_LIB_INSTALL_CMD = "python -m pip install --upgrade ${indexUrlFlag} ${extraIndexUrlFlag} tssc"

        if(input.tsscLibVersion) {
            TSSC_LIB_INSTALL_CMD += "==${input.tsscLibVersion}"
        }
    }

    pipeline {
        agent {
            kubernetes {
                label "${GIT_REPO_NAME}-${GIT_BRANCH}-${env.BUILD_ID}"
                cloud 'openshift'
                yaml """
    apiVersion: v1
    kind: Pod
    spec:
        serviceAccount: jenkins
        containers:
        - name: 'jnlp'
          image: "${JENKINS_WORKER_IMAGE_JNLP}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
        - name: 'maven'
          image: "${JENKINS_WORKER_IMAGE_MAVEN}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
        - name: 'buildah'
          image: "${JENKINS_WORKER_IMAGE_BUILDAH}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
          securityContext:
              privileged: true
        - name: 'argocd'
          image: "${JENKINS_WORKER_IMAGE_ARGOCD}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
        - name: 'skopeo'
          image: "${JENKINS_WORKER_IMAGE_SKOPEO}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
        - name: 'sonar'
          image: "${JENKINS_WORKER_IMAGE_SONAR}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
        - name: 'config-lint'
          image: "${JENKINS_WORKER_IMAGE_CONFIGLINT}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
        - name: 'openscap'
          image: "${JENKINS_WORKER_IMAGE_OPENSCAP}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
          securityContext:
              privileged: true
        volumes:
        - name: home-tssc
          emptyDir: {}
    """
            }
        }

        stages {
            stage('Setup') {
                steps {
                    sh """
                        echo "Install TSSC module"
                        python -m venv tssc
                        source tssc/bin/activate
                        python -m pip install --upgrade pip
                        ${TSSC_LIB_INSTALL_CMD}
                    """

                    withCredentials([
                        file(credentialsId: input.credentialIDsopsPGPKey, variable: 'sops_pgp_key')
                    ]) {
                        sh """
                            echo "Import PGP Key"
                            gpg --import ${sops_pgp_key}
                        """
                    }
                } // steps
            } // stage
            stage('Continuous Integration') {
                stages {
                    stage('Generate Metadata') {
                        steps {
                            container('maven') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step generate-metadata
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('Tag Source Code') {
                        steps {
                            container('maven') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step tag-source
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('Run Unit Tests (Maven)') {
                        steps {
                            container('maven') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step unit-test
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('Package Application') {
                        steps {
                            container('maven') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step package
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('Static Code Analysis') {
                        steps {
                            container('sonar') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step static-code-analysis
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('Push Artifacts to Repository') {
                        steps {
                            container('maven') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step push-artifacts
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('Create Container Image') {
                        steps {
                            container('buildah') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step create-container-image
                                """

                            } // container
                        } // steps
                    } // stage
                    stage('Image Unit Testing (Not Implemented)') {
                        steps {
                            echo "Not Implemented"
                        } // steps
                    } // stage
                    stage('Static Image Scans') {
                        parallel {
                            stage('Static Compliance Image Scan') {
                                steps {
                                    container('openscap') {
                                        sh """
                                            source tssc/bin/activate
                                            python -m tssc \
                                                --config ${input.configDir} \
                                                --step container-image-static-compliance-scan
                                        """
                                    } //container
                                } // steps
                            } // stage
                            stage('Static Vulnerability Image Scan (Not Implemented)') {
                                steps {
                                    echo "Not Implemented"
                                } // steps
                            } // stage
                        } // parallel
                    } // stage
                    stage('Push Trusted Container Image to Repository') {
                        steps {
                            container('skopeo') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step push-container-image
                                """
                            } // container
                        } // steps
                    } // stage
                } // CI Stage
            } // CI Stages

            stage('DEV') {
                when {
                    expression {
                        result = false
                        input.devBranchPatterns.find {
                            if (BRANCH_NAME ==~ it) {
                                result = true
                                return true
                            } else {
                                return false
                            }
                        }
                        return result
                    }
                }
                stages {
                    stage("DEV: Deploy or Update Environment") {
                        steps {
                            container('argocd') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step deploy \
                                        --environment ${input.envNameDev}
                                """
                            } // container
                        } // steps
                    } // stage
                    stage("DEV: Validate Environment Configuration") {
                        steps {
                            container('config-lint') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step validate-environment-configuration \
                                        --environment ${input.envNameDev}
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('DEV: UAT and Vulnerability Scans') {
                        parallel {
                            stage('DEV: Run User Acceptance Tests') {
                                steps {
                                    container('maven') {
                                        sh """
                                            source tssc/bin/activate
                                            python -m tssc \
                                                --config ${input.configDir} \
                                                --step uat \
                                                --environment ${input.envNameDev}
                                        """
                                    } // container
                                } // steps
                            } // stage
                            stage('DEV: Run Runtime Vulnerability Scans (Not Implemented)') {
                                steps {
                                    echo "Not Implemented"
                                } // steps
                            } // stage
                        } // parallel
                    } // UAT and Vuln Stage
                    stage('DEV: Run Performance Tests (Limited) (Not Implemented)') {
                        steps {
                            echo "Not Implemented"
                        } // steps
                    } // stage
                } // DEV Stages
            } // DEV Stage

            stage('TEST') {
                when {
                    expression {
                        result = false
                        input.releaseBranchPatterns.find {
                            if (BRANCH_NAME ==~ it) {
                                result = true
                                return true
                            } else {
                                return false
                            }
                        }
                        return result
                    }
                }
                stages {
                    stage('TEST: Deploy or Update Environment') {
                        steps {
                            container('argocd') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step deploy \
                                        --environment ${input.envNameTest}
                                    """
                            } // container
                        } // steps
                    } // stage
                    stage('TEST: Validate Environment Configuration') {
                        steps {
                            container('config-lint') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step validate-environment-configuration \
                                        --environment ${input.envNameTest}
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('TEST: UAT and Vulnerability Scans') {
                        parallel {
                            stage('Run User Acceptance Tests (Maven)') {
                                steps {
                                    container('maven') {
                                        sh """
                                            source tssc/bin/activate
                                            python -m tssc \
                                                --config ${input.configDir} \
                                                --step uat \
                                                --environment ${input.envNameTest}
                                        """
                                    } // container
                                } // steps
                            } // stage
                            stage('Run Runtime Vulnerability Scans (Not Implemented)') {
                                steps {
                                    echo "Not Implemented"
                                } // steps
                            } // stage
                        } // parallel
                    } // UAT and Vuln Stage
                    stage('TEST: Run Performance Tests (Not Implemented)') {
                        steps {
                            echo "Not Implemented"
                        } // steps
                    } // stage
                } // TEST Stages
            } // TEST Stage

            stage('PROD') {
                when {
                    expression {
                        result = false
                        input.releaseBranchPatterns.find {
                            if (BRANCH_NAME ==~ it) {
                                result = true
                                return true
                            } else {
                                return false
                            }
                        }
                        return result
                    }
                }
                stages {
                    stage('PROD: Deploy or Update Environment') {
                        steps {
                            container('argocd') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step deploy \
                                        --environment ${input.envNameProd}
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('PROD: Validate Environment Configuration') {
                        steps {
                            container('config-lint') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step validate-environment-configuration \
                                        --environment ${input.envNameProd}
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('PROD: Run Canary Testing (Not Implemented)') {
                        steps {
                            echo "Not Implemented"
                        } // steps
                    } // stage
                } // PROD Stages
            } // PROD Stage

            stage('Finish') {
                when {
                    expression {
                        result = false
                        input.releaseBranchPatterns.find {
                            if (BRANCH_NAME ==~ it) {
                                result = true
                                return true
                            } else {
                                return false
                            }
                        }
                        return result
                    }
                }
                stages {
                    stage('Collect, Bundle, & Publish Test Reports and Metadata') {
                        steps {
                            archiveArtifacts artifacts: 'tssc-results/**',
                            onlyIfSuccessful: true
                        } // steps
                    }
                } // stages
            } // Finish Stage
        } // stages
    } // pipeline
} // call
