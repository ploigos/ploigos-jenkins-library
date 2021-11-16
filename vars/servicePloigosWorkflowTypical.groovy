#!/usr/bin/env groovy

import ploigos.params.ServiceWorkflowParams

/* Workflow to CI/CD a deployable container based service.
 */
def call(ServiceWorkflowParams params) {
    /* Match everything that isn't a-z, a-Z, 0-9, -, _, or .
    *
    * See https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#syntax-and-character-set
    */
    String KUBE_LABEL_NOT_SAFE_CHARS_REGEX = /[^a-zA-Z0-9\-_\.]/
    int KUBE_LABEL_MAX_LENGTH = 62

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

    String WORKFLOW_WORKER_NAME_AGENT                = 'jnlp'
    String WORKFLOW_WORKER_NAME_APP_OPERATIONS       = 'app-ci'
    String WORKFLOW_WORKER_NAME_STATIC_CODE_ANALYSIS = 'static-code-analysis'
    String WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS = 'container-operations'
    String WORKFLOW_WORKER_NAME_CONTAINER_SCANNING   = 'container-scanning'
    String WORKFLOW_WORKER_NAME_DEPLOY               = 'deploy'

    /* Workspace for the container users home directory.
     *
     * Important because the home directory is where the python virtual environment will be setup
     * to be shared with future steps. */
    String WORKFLOW_WORKER_WORKSPACE_HOME_PATH = "/home/ploigos"

    /* Name of the virtual environment to set up in the given home worksapce. */
    String WORKFLOW_WORKER_VENV_NAME = 'venv-ploigos'

    /* Path to virtual python environment that PSR is in and/or will be installed into, must be on a persistent volume that can be shared between containers */
    String WORKFLOW_WORKER_VENV_PATH = "${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}/${WORKFLOW_WORKER_VENV_NAME}"

    // Directory into which platform configuration is mounted, if applicable
    String PLATFORM_CONFIG_DIR = "/opt/ploigos-platform-config"

    // set platform config mount and volume if enabled
    boolean ENABLE_PLATFORM_CONFIG = (params.platformConfigConfigMapName?.trim())
    String PLATFORM_CONFIG_MOUNT = ENABLE_PLATFORM_CONFIG ? """
          - name: ploigos-platform-config
            mountPath: ${PLATFORM_CONFIG_DIR}/config
    """ : ""
    String PLATFORM_CONFIG_VOLUME = ENABLE_PLATFORM_CONFIG ? """
        - name: ploigos-platform-config
          configMap:
            name: ${params.platformConfigConfigMapName}
    """ : ""

    // set platform config secret mount and volume if enabled
    boolean ENABLE_PLATFORM_CONFIG_SECRETS = (params.platformConfigSecretName?.trim())
    String PLATFORM_CONFIG_SECRETS_MOUNT = ENABLE_PLATFORM_CONFIG_SECRETS ? """
          - name: ploigos-platform-config-secrets
            mountPath: ${PLATFORM_CONFIG_DIR}/secrets
    """ : ""
    String PLATFORM_CONFIG_SECRETS_VOLUME = ENABLE_PLATFORM_CONFIG_SECRETS ? """
        - name: ploigos-platform-config-secrets
          secret:
            secretName: ${params.platformConfigSecretName}
    """ : ""

    // Combine this app's local config with platform-level config if any provided
    String PSR_CONFIG_ARG = ENABLE_PLATFORM_CONFIG || ENABLE_PLATFORM_CONFIG_SECRETS ?
        "${PLATFORM_CONFIG_DIR} ${params.stepRunnerConfigDir}" : "${params.stepRunnerConfigDir}"

    // set trusted ca bundle mount and volume if enabled
    boolean ENABLE_TRUSTED_CA_BUNDLE_CONFIG_MAP = (params.trustedCABundleConfigMapName?.trim())
    String TLS_MOUNTS = ENABLE_TRUSTED_CA_BUNDLE_CONFIG_MAP ? """
          - name: trusted-ca
            mountPath: /etc/pki/ca-trust/source/anchors
            readOnly: true
    """ : ""
    String TLS_VOLUMES = ENABLE_TRUSTED_CA_BUNDLE_CONFIG_MAP? """
        - name: trusted-ca
          configMap:
            name: ${params.trustedCABundleConfigMapName}
    """ : ""

    // determine additional security context to use for container operations
    String WORKFLOW_WORKER_CONTAINER_OPERATIONS_ADDITIONAL_SECURITY_CONTEXTS = params.workflowWorkerContainerOperationsUsePrivilegeEscalation? """
            allowPrivilegeEscalation: true
    """ : """
            capabilities:
              add:
              - 'SETUID'
              - 'SETGID'
    """

    pipeline {
        options {
            ansiColor('xterm')
        }
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
        serviceAccount: ${params.workflowServiceAccountName}
        containers:
        - name: ${WORKFLOW_WORKER_NAME_AGENT}
          image: "${params.workflowWorkerImageAgent}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          securityContext:
            runAsUser: ${params.workflowWorkerRunAsUser}
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_CONFIG_MOUNT}
          ${PLATFORM_CONFIG_SECRETS_MOUNT}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_APP_OPERATIONS}
          image: "${params.workflowWorkerImageAppOperations}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          securityContext:
            runAsUser: ${params.workflowWorkerRunAsUser}
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          - mountPath: /var/pgp-private-keys
            name: pgp-private-keys
          ${PLATFORM_CONFIG_MOUNT}
          ${PLATFORM_CONFIG_SECRETS_MOUNT}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_STATIC_CODE_ANALYSIS}
          image: "${params.workflowWorkerImageStaticCodeAnalysis}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          securityContext:
            runAsUser: ${params.workflowWorkerRunAsUser}
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_CONFIG_MOUNT}
          ${PLATFORM_CONFIG_SECRETS_MOUNT}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS}
          image: "${params.workflowWorkerImageContainerOperations}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          securityContext:
            runAsUser: ${params.workflowWorkerRunAsUser}
            ${WORKFLOW_WORKER_CONTAINER_OPERATIONS_ADDITIONAL_SECURITY_CONTEXTS}
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_CONFIG_MOUNT}
          ${PLATFORM_CONFIG_SECRETS_MOUNT}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_CONTAINER_SCANNING}
          image: "${params.workflowWorkerImageContainerScanning}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          securityContext:
            runAsUser: ${params.workflowWorkerRunAsUser}
            ${WORKFLOW_WORKER_CONTAINER_OPERATIONS_ADDITIONAL_SECURITY_CONTEXTS}
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_CONFIG_MOUNT}
          ${PLATFORM_CONFIG_SECRETS_MOUNT}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_DEPLOY}
          image: "${params.workflowWorkerImageDeploy}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          securityContext:
            runAsUser: ${params.workflowWorkerRunAsUser}
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_CONFIG_MOUNT}
          ${PLATFORM_CONFIG_SECRETS_MOUNT}
          ${TLS_MOUNTS}
        volumes:
        - name: home-ploigos
          emptyDir: {}
        - name: pgp-private-keys
          secret:
            secretName: ${params.pgpKeysSecretName}
        ${PLATFORM_CONFIG_VOLUME}
        ${PLATFORM_CONFIG_SECRETS_VOLUME}
        ${TLS_VOLUMES}
    """
            }
        }

         stages {
            stage('SETUP') {
                parallel {
                    stage('SETUP: Workflow Step Runner') {
                        environment {
                            GIT_SSL_NO_VERIFY               = "${params.stepRunnerLibSourceGitTLSNoVerify}"
                            UPDATE_STEP_RUNNER_LIBRARY      = "${params.stepRunnerUpdateLibrary}"
                            STEP_RUNNER_LIB_SOURCE_URL      = "${params.stepRunnerLibSourceUrl}"
                            STEP_RUNNER_LIB_INDEX_URL       = "${params.stepRunnerLibIndexUrl}"
                            STEP_RUNNER_LIB_EXTRA_INDEX_URL = "${params.stepRunnerLibExtraIndexUrl}"
                            STEP_RUNNER_LIB_VERSION         = "${params.stepRunnerLibVersion}"
                            STEP_RUNNER_PACKAGE_NAME        = "${params.stepRunnerPackageName}"
                            WORKFLOW_WORKER_VENV_PATH       = "${WORKFLOW_WORKER_VENV_PATH}"
                            VERBOSE                         = "${params.verbose}"
                        }
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                script {
                                    ploigosUtils.setupWorkflowStepRunner()
                                }
                            }
                        }
                    }
                    stage('SETUP: PGP Keys') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    echo "*******************"
                                    echo "* Import PGP Keys *"
                                    echo "*******************"
                                    gpg --import /var/pgp-private-keys/*
                                """
                            }
                        }
                    }
                } // parallel
            } // SETUP
            stage('Continuous Integration') {
                stages {
                    stage('CI: Generate Metadata') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step generate-metadata
                                """
                            }
                        }
                    }
                    stage('CI: Tag Source Code') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step tag-source
                                """
                            }
                        }
                    }
                    stage('CI: Run Unit Tests') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step unit-test
                                """
                            }
                        }
                    }
                    stage('CI: Package Application') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step package
                                """
                            }
                        }
                    }
                    stage('CI: Static Code Analysis') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_STATIC_CODE_ANALYSIS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step static-code-analysis
                                """
                            }
                        }
                    }
                    stage('CI: Push Application to Repository') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step push-artifacts
                                """
                            }
                        }
                    }
                    stage('CI: Create Container Image') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step create-container-image
                                """
                            }
                        }
                    }
                    stage('CI: Static Image Scan') {
                        parallel {
                            stage('CI: Static Image Scan: Vulnerability') {
                                steps {
                                    container("${WORKFLOW_WORKER_NAME_CONTAINER_SCANNING}") {
                                        sh """
                                            if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                            set -eu -o pipefail

                                            source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                            psr \
                                                --config ${PSR_CONFIG_ARG} \
                                                --step container-image-static-vulnerability-scan
                                        """
                                    }
                                }
                            }
                        }
                    }
                    stage('CI: Push Container Image to Repository') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step push-container-image
                                """
                            }
                        }
                    }
               }
            } // CI Stages

            stage('DEV') {
                when {
                    expression {
                        result = false
                        params.devGitRefPatterns.find {
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
                            container("${WORKFLOW_WORKER_NAME_DEPLOY}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step deploy \
                                        --environment ${params.envNameDev}
                                """
                            }
                        }
                    }
                    stage('DEV: Run User Acceptance Tests') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step uat \
                                        --environment ${params.envNameDev}
                                """
                            }
                        }
                    }
                }
            } // DEV Stage

            stage('TEST') {
                when {
                    expression {
                        result = false
                        params.releaseGitRefPatterns.find {
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
                            container("${WORKFLOW_WORKER_NAME_DEPLOY}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step deploy \
                                        --environment ${params.envNameTest}
                                    """
                            }
                        }
                    }
                    stage('Run User Acceptance Tests') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step uat \
                                        --environment ${params.envNameTest}
                                """
                            }
                        }
                    }
               }
            } // TEST Stage

            stage('PROD') {
                when {
                    expression {
                        result = false
                        params.releaseGitRefPatterns.find {
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
                            container("${WORKFLOW_WORKER_NAME_DEPLOY}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step deploy \
                                        --environment ${params.envNameProd}
                                """
                            }
                        }
                    }
               }
            } // PROD Stage
        } // stages
        post {
            always {
                container("${WORKFLOW_WORKER_NAME_APP_OPERATIONS}") {
                    sh """
                        if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                        set -eu -o pipefail

                        source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                        psr \
                            --config ${PSR_CONFIG_ARG} \
                            --step report
                    """
                    archiveArtifacts artifacts: 'step-runner-working/report/*.zip', fingerprint: true
                }
            } // always
        } // post
    } // pipeline
} // call
