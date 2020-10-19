#!/usr/bin/env groovy

class PipelineInput implements Serializable {
    /* Path to pass to the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/)
     * `--config` flag. */
    String   configDir   = ''

    /* Name of the "Development" environment used in the configuration files and to pass to the
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) `--environment` flag
     * where appropriate. */
    String   envNameDev  = 'DEV'

    /* Name of the "Test" environment used in the configuration files and to pass to the
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) `--environment` flag
     * where appropriate. */
    String   envNameTest = 'TEST'

    /* Name of the "Production" environment used in the configuration files and to pass to the
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) `--environment` flag
     * where appropriate. */
    String   envNameProd = 'PROD'

    /* Array of regex patterns for branches that should be deployed to
     * Test and then Production environments. */
    String[] releaseBranchPatterns  = ['main']

    /* Array of regex patterns for branches that should be deployed to
     * Development environments */
    String[] devBranchPatterns      = ['^feature/.*$', '^PR-.*$']

    /* URI to the container registry hosting the Jenkins workers images used by this pipeline. */
    String jenkinsWorkersImageRegesitryURI   = 'quay.io'

    /* Container image repository name hosting the Jenkins workers images used by this pipeline. */
    String jenkinsWorkersImageRepositoryName = 'tssc'

    /* Container image tag to use for the Jenkins workers images used by this pipeline. */
    String jenkinsWorkersImageTag            = 'latest'

    /* Policy for pulling new versions of the jenkinsWorkersImageTag when running this pipeline. */
    String jenkinsWorkersImagePullPolicy     = 'IfNotPresent'

    /* If `true`, then pull the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/)
     * source code and build it. If `false` use the version of
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) that is pre-installed
     * in the Jenkins workers container images. If `false`, `tsscLibIndexUrl`,
     * `tsscLibExtraIndexUrl`, `tsscLibVersion`, and `tsscLibSourceUrl` are ignored. */
    boolean updateTsscLibrary = false

    /* If `tsscLibSourceUrl` is not supplied and `updateTsscLibrary` is `true` this will be passed
     * to pip as `--index-url` for installing the
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) and its dependencies.
     *
     * NOTE:    PIP is indeterminate whether it will pull packages from
     *          --index-url or --extra-index-url, therefor be sure to specify tsscLibVersion
     *          if trying to pull a specific version from a specific index.
     *
     * @SEE https://pip.pypa.io/en/stable/reference/pip_install/#id48 */
    String tsscLibIndexUrl  = 'https://pypi.org/simple/'

    /* If `tsscLibSourceUrl` is not supplied and `updateTsscLibrary` is `true` this will be passed
     * to pip as `--extra-index-url` for installing the
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) and its dependencies.
     *
     * NOTE:    PIP is indeterminate whether it will pull packages from
     *          --index-url or --extra-index-url, therefor be sure to specify tsscLibVersion
     *          if trying to pull a specific version from a specific index.
     *
     * @SEE https://pip.pypa.io/en/stable/reference/pip_install/#id48 */
    String tsscLibExtraIndexUrl = 'https://pypi.org/simple/'

    /* If `tsscLibSourceUrl` is not supplied and `updateTsscLibrary` is `true` this will be passed
     * to pip as the version of the
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) to install.
     *
     * NOTE:    If not given pip will install the latest from either --index-url or
     *          --extra-index-url indeterminantly */
    String tsscLibVersion   = null

    /* If given and `updateTsscLibrary` is `true` this will be used as the source location to
     * install the TSSC library](https://rhtconsulting.github.io/tssc-python-package/) from rather
     * then from a PEP 503 compliant repository. <br><br>If given then `tsscLibIndexUrl`,
     * `tsscLibExtraIndexUrl`, and `tsscLibVersion` are ignored.
     *
     * EXAMPLE 1: git+https://github.com/rhtconsulting/tssc-python-package.git@feature/NAPSSPO-1018
     *            installs from the public 'rhtconsulting' fork from
     *            the 'feature/NAPSSPO-1018' branch.
     *
     * EXAMPLE 2: git+https://gitea.internal.example.xyz/tools/tssc-python-package.git@main
     *            installs from an internal fork of the 'tssc' library from the 'main' branch. */
    String tsscLibSourceUrl = null

    /* ID of Jenkins Credential of type 'Secret file' storing PGP key to install.
     *
     * This PGP key will be imported so that the
     * [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) can decrypt any
     * [SOPS](https://github.com/mozilla/sops) encrypted configuration in the given `configDir`.
     *
     * @SEE https://www.jenkins.io/doc/book/using/using-credentials/#configuring-credentials */
    String credentialIDsopsPGPKey = 'sops-pgp-key'

    /* Kubernetes ServiceAccount that the Jenkins Worker Kubernetes Pod should be deployed with.
     *
     * IMPORTANT: This Kubernetes ServiceAccount needs to have access (via RoleBinding to Role)
     *            to a SecurityContextConstraints that can runAsUser kubernetesPodRunAsUserUID.
     *
     * EXAMPLE SecurityContextConstraints:
     *      kind: SecurityContextConstraints
     *      apiVersion: security.openshift.io/v1
     *      metadata:
     *      annotations:
     *          kubernetes.io/description: TODO
     *       name: run-as-user-${kubernetesPodRunAsUserUID}
     *       runAsUser:
     *       type: MustRunAsRange
     *       uidRangeMax: ${kubernetesPodRunAsUserUID}
     *       uidRangeMin: ${kubernetesPodRunAsUserUID}
     *       seLinuxContext:
     *       type: MustRunAs
     */
    String kubernetesServiceAccountForJenkinsWorkersPod = 'jenkins'

    /* The UID to run the Jenkins Worker Kubernetes containers as.
     *
     * IMPORTANT:   From experimentation this NEEDS be a UID that exists in the Jenkins workers
     *              images. This is due to limitations of how subuid, subgid, and namespaces work
     *              and their appropriate ranges not being created for random UID is not created
     *              with `useradd` and how that interacts with `buildah unshare` for rootless
     *              container builds within a container.
     *
     * NOTE: The quay.io/tssc/tssc-base image uses UID 1001 but if you don't like that UID
     *       then you can use https://github.com/rhtconsulting/tssc-containers to create custom
     *       versions of the tssc containers and passing in the container ARG `TSSC_USER_UID` to
     *       change the UID.
     */
    int kubernetesPodRunAsUserUID = 1001
}

// Java Backend Reference Jenkinsfile
def call(Map inputMap) {
    /* Match everything that isn't a-z, a-Z, 0-9, -, _, or .
    *
    * See https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#syntax-and-character-set
    */
    String KUBE_LABEL_NOT_SAFE_CHARS_REGEX = /[^a-zA-Z0-9\-_\.]/
    int KUBE_LABEL_MAX_LENGTH = 62

    input = new PipelineInput(inputMap)
    String JENKINS_WORKER_IMAGE_JNLP       = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-ci-agent-jenkins:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_MAVEN      = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-maven:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_CONTAINERS = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-containers:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_ARGOCD     = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-argocd:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_SONAR      = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-sonar:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_CONFIGLINT = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-config-lint:${input.jenkinsWorkersImageTag}"
    String JENKINS_WORKER_IMAGE_OPENSCAP   = "${input.jenkinsWorkersImageRegesitryURI}/${input.jenkinsWorkersImageRepositoryName}/tssc-tool-openscap:${input.jenkinsWorkersImageTag}"

    // SEE: https://stackoverflow.com/questions/25088034/use-git-repo-name-as-env-variable-in-jenkins-job
    String GIT_BRANCH = scm.branches[0].name
    String GIT_BRANCH_KUBE_LABEL_VALUE = GIT_BRANCH
        .replaceAll(KUBE_LABEL_NOT_SAFE_CHARS_REGEX, '_')
        .drop(GIT_BRANCH.length()-KUBE_LABEL_MAX_LENGTH)
    String GIT_URL = scm.userRemoteConfigs[0].url
    String GIT_REPO_NAME = "${GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')}"
    String GIT_REPO_NAME_KUBE_LABEL_VALUE = GIT_REPO_NAME
        .replaceAll(KUBE_LABEL_NOT_SAFE_CHARS_REGEX, '-')
        .drop(GIT_REPO_NAME.length()-KUBE_LABEL_MAX_LENGTH)

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
                cloud 'openshift'
                yaml """
    apiVersion: v1
    kind: Pod
    metadata:
        labels:
            git-repo-name: ${GIT_REPO_NAME_KUBE_LABEL_VALUE}
            git-branch-name: ${GIT_BRANCH_KUBE_LABEL_VALUE}
            jenkins-build-id: ${env.BUILD_ID}
    spec:
        serviceAccount: ${input.kubernetesServiceAccountForJenkinsWorkersPod}
        securityContext:
            runAsUser: ${input.kubernetesPodRunAsUserUID}
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
        - name: 'containers'
          image: "${JENKINS_WORKER_IMAGE_CONTAINERS}"
          imagePullPolicy: "${input.jenkinsWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'cat']
          volumeMounts:
          - mountPath: /home/tssc
            name: home-tssc
        - name: 'argocd'
          image: "${JENKINS_WORKER_IMAGE_ARGOCD}"
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
                        echo "Create Python venv"
                        python -m venv --system-site-packages --copies tssc
                    """

                    script {
                        if(input.updateTsscLibrary) {
                            sh """
                                echo "Install TSSC module"
                                source tssc/bin/activate
                                python -m pip install --upgrade pip
                                ${TSSC_LIB_INSTALL_CMD}
                            """
                        }
                    }

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
                            container('containers') {
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
                            container('containers') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step push-container-image
                                """
                            } // container
                        } // steps
                    } // stage
                    stage('Sign Trusted Container Image') {
                        steps {
                            container('containers') {
                                sh """
                                    source tssc/bin/activate
                                    python -m tssc \
                                        --config ${input.configDir} \
                                        --step sign-container-image
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
