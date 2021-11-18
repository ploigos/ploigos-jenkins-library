package ploigos.params

import ploigos.params.SharedUserWorkflowParams

/* User parameters for service workflows.
 */
class UserServiceWorkflowParams extends SharedUserWorkflowParams {
    /* Name of the "Development" environment used in the Step Runner configuration
     * files and to pass to the Workflow Step Runner when running a step targeted to
     * the "Development" environment. */
    String envNameDev = 'DEV'

    /* Name of the "Development" environment used in the Step Runner configuration
     * files and to pass to the Workflow Step Runner when running a step targeted to
     * the "Development" test environment. */
    String envNameDevTest = 'DEVTEST'

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
    String[] devGitRefPatterns = ['^feature/.+$', '^PR-.+$', '^MR-.+$', '^fix/.+$']

    /* Regex pattern for git references that should go through the
     * Continues Integration (CI) workflow and then the deployment to
     * "Release" environment(s) (IE: "DEVTEST", "TEST", and then "PROD" environments) workflow. */
    String[] releaseGitRefPatterns = ['^main$']
}
