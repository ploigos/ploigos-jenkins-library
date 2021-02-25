#!/usr/bin/env groovy

class WorkflowParams implements Serializable {
    /* log any *sh commands used during execution */
    String verbose = 'false'

    /* Path to the Step Runner configuration to pass to the
     * Workflow Step Runner when running workflow steps. */
    String stepRunnerConfigDir = ''

    /* Name of the Kubernetes Secret containing the PGP private keys to import for use by SOPS
     * to decrypt encrypted Step Runner config. */
    String pgpKeysSecretName = null

    /* Name of the "Development" environment used in the Step Runner configuration
     * files and to pass to the Workflow Step Runner when running a step targeted to
     * the "Development" environment. */
    String envNameDev = 'DEV'

    /* Name of the "Test" environment used in the Step Runner configuration
     * files and to pass to the Workflow Step Runner when running a step targeted to
     * the "Test" environment. */
    String envNameTest = 'TEST'

    /* Name of the "Production" environment used in the Step Runner configuration
     * files and to pass to the Workflow Step Runner when running a step targeted to
     * the "Production" environment.*/
    String envNameProd = 'PROD'

    /* Regex pattern for git references that should only go through the
     * Continues Integration (CI) workflow. */
    String[] ciOnlyGitRefPatterns = ['^$']

    /* Regex pattern for git references that should go through the
     * Continues Integration (CI) workflow and then the deployment to
     * "Development" environment(s) (IE: "DEV" environment) workflow. */
    String[] devGitRefPatterns = ['^feature/.+$', '^PR-.+$']

    /* Regex pattern for git references that should go through the
     * Continues Integration (CI) workflow and then the deployment to
     * "Release" environment(s) (IE: "TEST" and then "PROD" environments) workflow. */
    String[] releaseGitRefPatterns = ['^main$']

    /* Name of the python package to use as the Workflow Step Runner. */
    String stepRunnerPackageName = 'ploigos-step-runner'

    /* If 'true', then pull the Workflow Step Runner library source code and build it.
     * If 'false', use the version of the Workflow Step Runner library that is pre-installed
     * in the CI worker images.
     *
     * If 'false' then the following parameters are ignored:
     *    - 'stepRunnerLibSourceUrl'
     *    - 'stepRunnerLibIndexUrl'
     *    - 'stepRunnerLibExtraIndexUrl'
     *    - 'stepRunnerLibVersion' */
    boolean stepRunnerUpdateLibrary = false

    /* If 'stepRunnerUpdateLibrary' is true and 'stepRunnerLibSourceUrl' is not supplied then this
     * will be passed to pip as '--index-url' for installing the Workflow Step Runner library
     * and its dependencies.
     *
     * NOTE
     * ----
     * PIP is indeterminate whether it will pull packages from '--index-url' or
     * '--extra-index-url', therefor be sure to specify 'stepRunnerLibVersion'
     * if trying to pull a specific version from a specific index.
     *
     * SEE
     * ---
     * - https://pip.pypa.io/en/stable/reference/pip_install/#id48 */
    String stepRunnerLibIndexUrl = 'https://pypi.org/simple/'

    /* If 'stepRunnerUpdateLibrary' is true and 'stepRunnerLibSourceUrl' is not supplied then this
     * will be passed to pip as '--extra-index-url' for installing the Workflow Step Runner library
     * and its dependencies.
     *
     * NOTE
     * ----
     * PIP is indeterminate whether it will pull packages from '--index-url' or
     * '--extra-index-url', therefor be sure to specify 'stepRunnerLibVersion'
     * if trying to pull a specific version from a specific index.
     *
     * SEE
     * ---
     * - https://pip.pypa.io/en/stable/reference/pip_install/#id48 */
    String stepRunnerLibExtraIndexUrl = 'https://pypi.org/simple/'

    /* If 'stepRunnerUpdateLibrary' is true and 'stepRunnerLibSourceUrl' is not supplied then this
     * will be passed to pip as as the version of the Workflow Step Runner library to install.
     *
     * NOTE
     * ----
     * If not given pip will install the latest from either 'stepRunnerLibIndexUrl' or
     * 'stepRunnerLibExtraIndexUrl' indeterminately. */
    String stepRunnerLibVersion = ""

    /* If none empty value given and 'stepRunnerUpdateLibrary' is true this will be used as the source
     * location to install the Workflow Step Runner library from rather then from a PEP 503 compliant
     * repository.
     *
     * If given then the following parameters are ignored:
     *   - 'stepRunnerLibIndexUrl'
     *   - 'stepRunnerLibExtraIndexUrl'
     *   - 'stepRunnerLibVersion'
     *
     * EXAMPLES
     * --------
     * git+https://github.com/ploigos/ploigos-step-runner.git@feature/NAPSSPO-1018
     * installs from the public 'ploigos' fork from the 'feature/NAPSSPO-1018' branch.
     *
     * git+https://gitea.internal.example.xyz/tools/ploigos-step-runner.git@main
     * installs from an internal fork of the step runner library from the 'main' branch. */
    String stepRunnerLibSourceUrl = ""

    /* The UID to run the workflow worker containers as.
     *
     * IMPORTANT:
     *  From experimentation this NEEDS be a UID that exists in the worker container images.
     *  This is due to limitations of how subuid, subgid, and namespaces work
     *  and their appropriate ranges not being created for random UID is not created
     *  with `useradd` and how that interacts with `buildah unshare` for rootless
     *  container builds within a container.
     *
     * NOTE:
     *  The quay.io/ploigos/ploigos-base image uses UID 1001 but if you don't like that UID
     *  then you can use https://github.com/ploigos/ploigos-containers to create custom
     *  versions of the Ploigos workflow containers and passing in the container ARG
     * `PLOIGOS_USER_UID` to change the UID. */
    int workflowWorkerRunAsUser = 1001

    /* Policy for pulling new versions of the imageTag for the CI worker images
     * when running this pipeline. */
    String workflowWorkersImagePullPolicy = 'IfNotPresent'

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when no other specific container image has been
     * specified for that step. */
    String workflowWorkerImageDefault = "ploigos/ploigos-ci-agent-jenkins:latest"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing package application step(s). */
    String workflowWorkerImagePackage = null

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing push push packaged artifacts step(s). */
    String workflowWorkerImagePushArtifacts = null

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing container operations (build/push/etc) step(s). */
    String workflowWorkerImageContainerOperations = "ploigos/ploigos-tool-containers:latest"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing deploy step(s). */
    String workflowWorkerImageDeploy = "ploigos/ploigos-tool-argocd:latest"

    /* Kubernetes ServiceAccount that the Jenkins Worker Kubernetes Pod should be deployed with.
     *
     * IMPORTANT
     * ---------
     * This Kubernetes ServiceAccount needs to have access (via RoleBinding to Role)
     * to a SecurityContextConstraints that can runAsUser workflowWorkerRunAsUser.
     *
     * EXAMPLE SecurityContextConstraints:
     *      kind: SecurityContextConstraints
     *      apiVersion: security.openshift.io/v1
     *      metadata:
     *      annotations:
     *          kubernetes.io/description: TODO
     *       name: run-as-user-${workflowWorkerRunAsUser}
     *       runAsUser:
     *       type: MustRunAsRange
     *       uidRangeMax: ${workflowWorkerRunAsUser}
     *       uidRangeMin: ${workflowWorkerRunAsUser}
     *       seLinuxContext:
     *       type: MustRunAsm */
    String workflowServiceAccountName = 'jenkins'

    /* Flag indicating that platform-level configuration is separated from
     * app-level configuration, instead provided by way of the following Kubernetes
     * objects, which are mounted into the agent Pod:
     *  - A ConfigMap named ploigos-platform-config
     *  - A Secret named ploigos-platform-config-secrets */
    boolean separatePlatformConfig = false

    /*
    Flag for utilizing a CA Bundle
    */
    boolean trustedCABundleConfig = false

    /*
	Variable for setting the name of the ConfigMap that is created to
        pass the additional CAs into the Containers that Jenkins uses as Agents
    */
    String trustedCABundleConfigMapName = 'trustedcabundle'

    /*
    Variable for setting toggling SSL Cert Verification in Git during the
        Pip Install of Step Runner. Set to 'true' for skipping cert verification.
    */
    String gitTlsNoVerify = false
}

// Java Backend Reference Jenkinsfile
def call(Map paramsMap) {
    /* Match everything that isn't a-z, a-Z, 0-9, -, _, or .
    *
    * See https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#syntax-and-character-set
    */
    String KUBE_LABEL_NOT_SAFE_CHARS_REGEX = /[^a-zA-Z0-9\-_\.]/
    int KUBE_LABEL_MAX_LENGTH = 62

    params = new WorkflowParams(paramsMap)

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

    String WORKFLOW_WORKER_NAME_DEFAULT              = 'jnlp'
    String WORKFLOW_WORKER_NAME_PACKAGE              = 'package'
    String WORKFLOW_WORKER_NAME_PUSH_ARTIFACTS       = 'push-artifacts'
    String WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS = 'containers'
    String WORKFLOW_WORKER_NAME_DEPLOY               = 'deploy'

    /* Workspace for the container users home directory.
     *
     * Important because the home directory is where the python virtual environment will be setup
     * to be shared with future steps. */
    String WORKFLOW_WORKER_WORKSPACE_HOME_PATH = "/home/ploigos"

    /* Name of the virtual environment to set up in the given home worksapce. */
    String WORKFLOW_WORKER_VENV_NAME = 'venv-ploigos'

    /* Directory into which platform configuration is mounted, if applicable */
    String PLATFORM_CONFIG_DIR = "/opt/platform-config"

    /* Additional mounts for agent containers, if separatePlatformConfig == true */
    String PLATFORM_MOUNTS = params.separatePlatformConfig ? """
          - mountPath: ${PLATFORM_CONFIG_DIR}/config.yml
            name: ploigos-platform-config
            subPath: config.yml
          - mountPath: ${PLATFORM_CONFIG_DIR}/config-secrets.yml
            name: ploigos-platform-config-secrets
            subPath: config-secrets.yml
    """ : ""

    /* Additional volumes for the agent Pod, if separatePlatformConfig == true */
    String PLATFORM_VOLUMES = params.separatePlatformConfig ? """
        - name: ploigos-platform-config
          configMap:
            name: ploigos-platform-config
        - name: ploigos-platform-config-secrets
          secret:
            secretName: ploigos-platform-config-secrets
    """ : ""

    /* Additional mount for agent containers, if trustedCaConfig == true */
    String TLS_MOUNTS = params.trustedCABundleConfig ? """
          - name: trusted-ca
            mountPath: /etc/pki/ca-trust/source/anchors
            readOnly: true
    """ : ""

    /* Additional volume for agent containers, if trustedCaConfig == true */
    String TLS_VOLUMES = params.trustedCABundleConfig ? """
        - name: trusted-ca
          configMap:
            name: ${params.trustedCABundleConfigMapName}
            items:
            - key: ca-bundle.crt
              path: tls-ca-bundle.pem
    """ : ""

    /* Combine this app's local config with platform-level config, if separatePlatformConfig == true */
    String PSR_CONFIG_ARG = params.separatePlatformConfig ?
        "${PLATFORM_CONFIG_DIR} ${params.stepRunnerConfigDir}" : "${params.stepRunnerConfigDir}"

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
        securityContext:
            runAsUser: ${params.workflowWorkerRunAsUser}
            fsGroup: ${params.workflowWorkerRunAsUser}
        containers:
        - name: ${WORKFLOW_WORKER_NAME_DEFAULT}
          image: "${params.workflowWorkerImageDefault}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          - mountPath: /var/pgp-private-keys
            name: pgp-private-keys
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_PACKAGE}
          image: "${params.workflowWorkerImagePackage}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'update-ca-trust && cat']
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_PUSH_ARTIFACTS}
          image: "${params.workflowWorkerImagePushArtifacts}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'update-ca-trust && cat']
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS}
          image: "${params.workflowWorkerImageContainerOperations}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'update-ca-trust && cat']
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_DEPLOY}
          image: "${params.workflowWorkerImageDeploy}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          tty: true
          command: ['sh', '-c', 'update-ca-trust && cat']
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        volumes:
        - name: home-ploigos
          emptyDir: {}
        - name: pgp-private-keys
          secret:
            secretName: ${params.pgpKeysSecretName}
        ${PLATFORM_VOLUMES}
        ${TLS_VOLUMES}
    """
            }
        }

        stages {
            stage('SETUP') {
                parallel {
                    stage('SETUP: Workflow Step Runner') {
                        environment {
                            GIT_SSL_NO_VERIFY               = "${params.gitTlsNoVerify}"
                            UPDATE_STEP_RUNNER_LIBRARY      = "${params.stepRunnerUpdateLibrary}"
                            STEP_RUNNER_LIB_SOURCE_URL      = "${params.stepRunnerLibSourceUrl}"
                            STEP_RUNNER_LIB_INDEX_URL       = "${params.stepRunnerLibIndexUrl}"
                            STEP_RUNNER_LIB_EXTRA_INDEX_URL = "${params.stepRunnerLibExtraIndexUrl}"
                            STEP_RUNNER_LIB_VERSION         = "${params.stepRunnerLibVersion}"
                            STEP_RUNNER_PACKAGE_NAME        = "${params.stepRunnerPackageName}"
                            VENV_NAME                       = "${WORKFLOW_WORKER_VENV_NAME}"
                            VERBOSE                         = "${params.verbose}"
                        }
                        steps {
                            container("${WORKFLOW_WORKER_NAME_DEFAULT}") {
                                script {
                                    /* NOTE:
                                     *  It is important that this is a string litteral block
                                     *  since it uses local bash variables within the script
                                     *  otherwise groovy will try to interprite the variables
                                     *  rathe then bash.
                                     *  That is why all the params are specified as environment
                                     *  variables to make them accessable to this script.
                                     */

                                    sh '''
                                        #!/bin/sh
                                        if [ "${VERBOSE}" == "true" ]; then set -x; else set +x; fi
                                        set -eu -o pipefail

                                        echo "**********************"
                                        echo "* Create Python venv *"
                                        echo "**********************"
                                        python -m venv --system-site-packages --copies ${HOME}/${VENV_NAME}
                                    '''

                                    sh '''
                                        #!/bin/sh
                                        if [ "${VERBOSE}" == "true" ]; then set -x; else set +x; fi
                                        set -e -o pipefail

                                        if [[ ${UPDATE_STEP_RUNNER_LIBRARY} =~ true|True ]]; then
                                            echo "*********************"
                                            echo "* Update Python Pip *"
                                            echo "*********************"

                                            source ${HOME}/${VENV_NAME}/bin/activate
                                            python -m pip install --upgrade pip

                                            if [[ ${STEP_RUNNER_LIB_SOURCE_URL} ]]; then
                                                STEP_RUNNER_LIB_INSTALL_CMD="python -m pip install --upgrade ${STEP_RUNNER_LIB_SOURCE_URL}"
                                            else
                                                indexUrlFlag=""

                                                if [[ ${STEP_RUNNER_LIB_INDEX_URL} ]]; then
                                                    indexUrlFlag="--index-url ${STEP_RUNNER_LIB_INDEX_URL}"
                                                fi

                                                extraIndexUrlFlag=""
                                                if [[ ${STEP_RUNNER_LIB_EXTRA_INDEX_URL} ]]; then
                                                    extraIndexUrlFlag="--extra-index-url  ${STEP_RUNNER_LIB_EXTRA_INDEX_URL}"
                                                fi

                                                STEP_RUNNER_LIB_INSTALL_CMD="python -m pip install --upgrade ${indexUrlFlag} ${extraIndexUrlFlag} ${STEP_RUNNER_PACKAGE_NAME}"

                                                if [[ ${STEP_RUNNER_LIB_VERSION} ]]; then
                                                    STEP_RUNNER_LIB_INSTALL_CMD+="==${STEP_RUNNER_LIB_VERSION}"
                                                fi
                                            fi

                                            echo "*************************************"
                                            echo "* Update Step Runner Python Package *"
                                            echo "*************************************"
                                            ${STEP_RUNNER_LIB_INSTALL_CMD}
                                        else
                                            echo "Using pre-installed Workflow Step Runner library"
                                        fi

                                        echo "****************************************************"
                                        echo "* Installed Step Runner Python Package Information *"
                                        echo "****************************************************"
                                        python -m pip show ${STEP_RUNNER_PACKAGE_NAME}
                                    '''
                                }
                            }
                        }
                    }
                    stage('SETUP: PGP Keys') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_DEFAULT}") {
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
                }
            }
            stage('Continuous Integration') {
                stages {
                    stage('CI: Generate Metadata') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_DEFAULT}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step generate-metadata
                                """
                            }
                        }
                    }
                    stage('CI: Tag Source Code') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_DEFAULT}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step tag-source
                                """
                            }
                        }
                    }
                    stage('CI: Package Application') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_PACKAGE}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step package
                                """
                            }
                        }
                    }
                    stage('CI: Push Application to Repository') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_PUSH_ARTIFACTS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
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

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step create-container-image
                                """
                            }
                        }
                    }
                    stage('CI: Push Container Image to Repository') {
                        steps {
                            container("${WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS}") {
                                sh """
                                    if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                    set -eu -o pipefail

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
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

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step deploy \
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

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
                                    psr \
                                        --config ${PSR_CONFIG_ARG} \
                                        --step deploy \
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

                                    source ${HOME}/${WORKFLOW_WORKER_VENV_NAME}/bin/activate
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
    } // pipeline
} // call
