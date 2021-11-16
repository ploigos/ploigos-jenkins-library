def setupWorkflowStepRunner() {
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
