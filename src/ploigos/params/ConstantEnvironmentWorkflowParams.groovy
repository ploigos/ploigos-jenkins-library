package ploigos.params

import ploigos.params.UserServiceWorkflowParams

/* Paramters that don't change from workflow to workflow and are rather constent for an enviornment.
 */
interface ConstantEnvironmentWorkflowParams extends Serializable {
    /* Policy for pulling new versions of the imageTag for the CI worker images
     * when running this pipeline. */
    String workflowWorkersImagePullPolicy = 'IfNotPresent'

    /* Container image to use when creating a workflow worker
     * to for connecting to the workflow runner tool */
    String workflowWorkerImageAgent = "openshift/origin-jenkins-agent-base:4.8.0"

    /* If `true` then use `allowPrivilegeEscalation` securityContex for the
     * container operations workflow worker container so that `buildah`/`podman`
     * can do rootless container operations.
     *
     * If `false` then use the `SETUID` and `SETGID` securityContext `capabilities` for the
     * container operations workflow worker container so that `buildah`/`podman`
     * can do rootless container operations.
     *
     * IMPORTANT
     * ---------
     * The Kubernetes ServiceAccount specified by `workflowServiceAccountName` needs to have
     * access (via RoleBinding to Role) to a SecurityContextConstraints that either allows for
     * `allowPrivilegeEscalation` (if `true`) or allows for use of the
     * `SETUID` and `SETGID` capabilities.
     */
    boolean workflowWorkerContainerOperationsUsePrivilegeEscalation = false

    /* UID to run workflow worker containers as.
     * What is important is that all the containers run as the same UID.
     *
     * The UID itself does not matter beyond what UIDs the Kubernetes ServiceAccount
     * specified via `workflowServiceAccountName` is allowed to use.
     */
    String workflowWorkerRunAsUser = "1001180042"

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

    /* If null empty value given and 'stepRunnerUpdateLibrary' is true this will be used as the source
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
    String stepRunnerLibSourceUrl = 'git+https://github.com/ploigos/ploigos-step-runner.git@main'

    /* If 'stepRunnerUpdateLibrary' is true and 'stepRunnerLibSourceUrl' is specified this value
     * determines whether to verify the Git TLS when checking out the step runner library source
     * for installation. */
    boolean stepRunnerLibSourceGitTLSNoVerify = false
}
