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

    /* If 'stepRunnerUpdateLibrary' is true and 'stepRunnerLibSourceUrl' is specified this value
     * determines whether to verify the Git TLS when checking out the step runner library source
     * for installation. */
    boolean stepRunnerLibSourceGitTLSNoVerify = false

    /* Policy for pulling new versions of the imageTag for the CI worker images
     * when running this pipeline. */
    String workflowWorkersImagePullPolicy = 'IfNotPresent'

    /* CPU resource request for worker pods created when running this pipeline */
    String workflowWorkersRequestsCPU = '.5'

    /* Memory resource request for worker pods created when running this pipeline */
    String workflowWorkersRequestsMemory = '500Mi'

    /* CPU resource limit for worker pods created when running this pipeline */
    String workflowWorkersLimitsCPU = '1'

    /* Memory resource limit for worker pods created when running this pipeline */
    String workflowWorkersLimitsMemory = '1Gi'

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when no other specific container image has been
     * specified for that step. */
    String workflowWorkerImageDefault = "ploigos/ploigos-base:latest"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps for connecting to CI tool */
    String workflowWorkerImageAgent = "ploigos/ploigos-ci-agent-jenkins:latest"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing container operations (build/push/etc) step(s). */
    String workflowWorkerImageContainerOperations = "ploigos/ploigos-tool-containers:latest"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing container image static compliance scan step(s). */
    String workflowWorkerImageContainerImageStaticComplianceScan = "ploigos/ploigos-tool-openscap:latest"

    /* Container image to use when creating a workflow worker to run pipeline steps
     * when performing container image static vulnerability scan step(s). */
    String workflowWorkerImageContainerImageStaticVulnerabilityScan = "ploigos/ploigos-tool-openscap:latest"

    /* Kubernetes ServiceAccount that the Jenkins Worker Kubernetes Pod should be deployed with.
     *
     * IMPORTANT
     * ---------
     * This Kubernetes ServiceAccount needs to have access (via RoleBinding to Role)
     * to a SecurityContextConstraints that can use the following capabilities to be
     * able to perform rootless container builds.
     *
     *   - SETUID
     *   - SETGID
     *
     * EXAMPLE SecurityContextConstraints: TODO LINK
     */
    String workflowServiceAccountName = 'jenkins'

    /* Flag indicating that platform-level configuration is separated from
     * app-level configuration, instead provided by way of the following Kubernetes
     * objects, which are mounted into the agent Pod:
     *  - A ConfigMap named ploigos-platform-config
     *  - A Secret named ploigos-platform-config-secrets */
    boolean separatePlatformConfig = false

    /* Name of the ConfigMap to mount as a trusted CA Bundle.
     * Useful for when interacting with external services signed by an internal CA.
     * If not specified then ignored. */
    String trustedCABundleConfigMapName = null

    /* Registry URL of the container image to scan.
     * i.e. In `quay.io/myorg/mycontainer:mytag` registry URL is `quay.io`. */
    String registryURL = ''

    /* Org/Subdirectory of the container image to scan.
     * i.e. In `quay.io/myorg/mycontainer:mytag` image organization is `myorg`. */
    String imageOrg = ''

    /* Repository/Container of the container image to scan.
     * i.e. In `quay.io/myorg/mycontainer:mytag` image name is `mycontainer`.*/
    String imageName = ''

    /* Container image tag to scan.
     * i.e. In `quay.io/myorg/mycontainer:mytag` image tag is `mytag`. */
    String imageTag = ''
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

    String WORKFLOW_WORKER_NAME_DEFAULT              = 'default'
    String WORKFLOW_WORKER_NAME_AGENT                = 'jnlp'
    String WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS = 'containers'
    String WORKFLOW_WORKER_NAME_CONTAINER_IMAGE_STATIC_COMPLIANCE_SCAN    = 'container-image-static-compliance-scan'
    String WORKFLOW_WORKER_NAME_CONTAINER_IMAGE_STATIC_VULNERABILITY_SCAN = 'container-image-static-vulnerability-scan'

    /* Workspace for the container users home directory.
     *
     * Important because the home directory is where the python virtual environment will be setup
     * to be shared with future steps. */
    String WORKFLOW_WORKER_WORKSPACE_HOME_PATH = "/home/ploigos"

    /* Name of the virtual environment to set up in the given home worksapce. */
    String WORKFLOW_WORKER_VENV_NAME = 'venv-ploigos'

    /* Path to virtual python environment that PSR is in and/or will be installed into, must be on a persistent volume that can be shared between containers */
    String WORKFLOW_WORKER_VENV_PATH = "${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}/${WORKFLOW_WORKER_VENV_NAME}"

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

    /* determine if trusted CA bundle config map is specified. */
    boolean ENABLE_TRUSTED_CA_BUNDLE_CONFIG_MAP = (params.trustedCABundleConfigMapName?.trim())

    /* Additional mount for agent containers, if trustedCaConfig == true */
    String TLS_MOUNTS = ENABLE_TRUSTED_CA_BUNDLE_CONFIG_MAP ? """
          - name: trusted-ca
            mountPath: /etc/pki/ca-trust/source/anchors
            readOnly: true
    """ : ""

    /* Additional volume for agent containers, if trustedCaConfig == true */
    String TLS_VOLUMES = ENABLE_TRUSTED_CA_BUNDLE_CONFIG_MAP? """
        - name: trusted-ca
          configMap:
            name: ${params.trustedCABundleConfigMapName}
    """ : ""

    /* Combine this app's local config with platform-level config, if separatePlatformConfig == true */
    String PSR_CONFIG_ARG = params.separatePlatformConfig ?
        "${PLATFORM_CONFIG_DIR} ${params.stepRunnerConfigDir}" : "${params.stepRunnerConfigDir}"

    /* Verify and setup the Image_Target for this container scan. */
    String IMAGE_TARGET = ""
    if (params.imageOrg) {
        IMAGE_TARGET = "${params.imageOrg}/${params.imageName}:${params.imageTag}"
    } else {
        IMAGE_TARGET = "${params.imageName}:${params.imageTag}"
    }

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
            jenkins-build-id: ${env.BUILD_ID}
    spec:
        serviceAccount: ${params.workflowServiceAccountName}
        containers:
        - name: ${WORKFLOW_WORKER_NAME_AGENT}
          image: "${params.workflowWorkerImageAgent}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          resources:
            limits:
              cpu: "${params.workflowWorkersRequestsCPU}"
              memory: "${params.workflowWorkersRequestsMemory}"
            requests:
              cpu: "${params.workflowWorkersLimitsCPU}"
              memory: "${params.workflowWorkersLimitsMemory}"
          tty: true
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          - mountPath: /var/pgp-private-keys
            name: pgp-private-keys
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_DEFAULT}
          image: "${params.workflowWorkerImageDefault}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          resources:
            limits:
              cpu: "${params.workflowWorkersRequestsCPU}"
              memory: "${params.workflowWorkersRequestsMemory}"
            requests:
              cpu: "${params.workflowWorkersLimitsCPU}"
              memory: "${params.workflowWorkersLimitsMemory}"
          tty: true
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          - mountPath: /var/pgp-private-keys
            name: pgp-private-keys
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_CONTAINER_OPERATIONS}
          image: "${params.workflowWorkerImageContainerOperations}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          resources:
            limits:
              cpu: "${params.workflowWorkersRequestsCPU}"
              memory: "${params.workflowWorkersRequestsMemory}"
            requests:
              cpu: "${params.workflowWorkersLimitsCPU}"
              memory: "${params.workflowWorkersLimitsMemory}"
          tty: true
          securityContext:
            capabilities:
              add:
              - 'SETUID'
              - 'SETGID'
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_CONTAINER_IMAGE_STATIC_COMPLIANCE_SCAN}
          image: "${params.workflowWorkerImageContainerImageStaticComplianceScan}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          resources:
            limits:
              cpu: "${params.workflowWorkersRequestsCPU}"
              memory: "${params.workflowWorkersRequestsMemory}"
            requests:
              cpu: "${params.workflowWorkersLimitsCPU}"
              memory: "${params.workflowWorkersLimitsMemory}"
          tty: true
          volumeMounts:
          - mountPath: ${WORKFLOW_WORKER_WORKSPACE_HOME_PATH}
            name: home-ploigos
          ${PLATFORM_MOUNTS}
          ${TLS_MOUNTS}
        - name: ${WORKFLOW_WORKER_NAME_CONTAINER_IMAGE_STATIC_VULNERABILITY_SCAN}
          image: "${params.workflowWorkerImageContainerImageStaticVulnerabilityScan}"
          imagePullPolicy: "${params.workflowWorkersImagePullPolicy}"
          resources:
            limits:
              cpu: "${params.workflowWorkersRequestsCPU}"
              memory: "${params.workflowWorkersRequestsMemory}"
            requests:
              cpu: "${params.workflowWorkersLimitsCPU}"
              memory: "${params.workflowWorkersLimitsMemory}"
          tty: true
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
                                        python -m venv --system-site-packages --copies ${WORKFLOW_WORKER_VENV_PATH}
                                    '''

                                    sh '''
                                        #!/bin/sh
                                        if [ "${VERBOSE}" == "true" ]; then set -x; else set +x; fi
                                        set -e -o pipefail

                                        if [[ ${UPDATE_STEP_RUNNER_LIBRARY} =~ true|True ]]; then
                                            echo "*********************"
                                            echo "* Update Python Pip *"
                                            echo "*********************"

                                            source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
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
                } // parallel
            } // SETUP
            stage('Continuous Integration') {
                stages {
                    stage('CI: Static Image Scan') {
                        parallel {
                            stage('CI: Static Image Scan: Compliance') {
                                steps {
                                    container("${WORKFLOW_WORKER_NAME_CONTAINER_IMAGE_STATIC_COMPLIANCE_SCAN}") {
                                        sh """
                                            if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                            set -eu -o pipefail

                                            source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                            psr \
                                                --config ${PSR_CONFIG_ARG} \
                                                --step container-image-static-compliance-scan \
						                        --step-config \
                                                    container-image-tag=${params.registryURL}/${IMAGE_TARGET} \
                                                    container-image-pull-repository-type='docker://'
                                        """
                                    }
                                }
                            }
                            stage('CI: Static Image Scan: Vulnerability') {
                                steps {
                                    container("${WORKFLOW_WORKER_NAME_CONTAINER_IMAGE_STATIC_VULNERABILITY_SCAN}") {
                                        sh """
                                            if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                                            set -eu -o pipefail

                                            source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                                            psr \
                                                --config ${PSR_CONFIG_ARG} \
                                                --step container-image-static-vulnerability-scan \
						                        --step-config \
                                                    container-image-tag=${params.registryURL}/${IMAGE_TARGET} \
                                                    container-image-pull-repository-type='docker://'
                                        """
                                    }
                                }
                            }

                        }
                    }
		        }
            } // CI Stages
        } // stages
        post {
            always {
                container("${WORKFLOW_WORKER_NAME_DEFAULT}") {
                    sh """
                        if [ "${params.verbose}" == "true" ]; then set -x; else set +x; fi
                        set -eu -o pipefail

                        source ${WORKFLOW_WORKER_VENV_PATH}/bin/activate
                        # NOTE: only passing service-name becasue its currently a required value
                        psr \
                            --config ${PSR_CONFIG_ARG} \
                            --step report \
                            --step-config \
                                organization=${params.imageOrg} \
                                application-name=${params.imageName} \
                                service-name='' \
                                version=${params.imageTag}
                    """
                    archiveArtifacts artifacts: 'step-runner-working/report/*.zip', fingerprint: true
                }
            } // always
        } // post
    } // pipeline
} // call
