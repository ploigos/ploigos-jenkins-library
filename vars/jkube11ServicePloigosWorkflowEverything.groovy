import ploigos.params.UserServiceWorkflowParams
import ploigos.params.ServiceWorkflowParams

/* Convince wrapper for `servicePipeline` to run workflow to CI/CD a Java service.
 */
def call(Map paramsMap) {
    /* validate user params so that user can't override non user params
     *
     * NOTE: groovy will auto throw an exception if user supplies param that
     *      can't be de-serilized into the UserServiceWorkflowParams class
     */
    new UserServiceWorkflowParams(paramsMap)

    // create All params from params map now the user params have been vlaidated
    params = new ServiceWorkflowParams(paramsMap)

    // set javaService specific infra params
    params.workflowWorkerImageAppOperations = "ploigos/ploigos-tool-maven:v1.0.0.java11.ubi8"
    params.workflowWorkerImageContainerOperations = "ploigos/ploigos-tool-jkube:v1.0.0.java11.ubi8"

    // invoke the service pipline
    servicePloigosWorkflowEverything(params)
}
