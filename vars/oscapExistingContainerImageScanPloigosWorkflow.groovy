import ploigos.params.UserExistingContainerImageScanWorkflowParams
import ploigos.params.ExistingContainerImageScanWorkflowParams

/* Convince wrapper for `servicePipeline` to run workflow to CI/CD a Java service.
 */
def call(Map paramsMap) {
    /* validate user params so that user can't override non user params
     *
     * NOTE: groovy will auto throw an exception if user supplies param that
     *      can't be de-serilized into the UserServiceWorkflowParams class
     */
    new UserExistingContainerImageScanWorkflowParams(paramsMap)

    // create All params from params map now the user params have been vlaidated
    params = new ExistingContainerImageScanWorkflowParams(paramsMap)

    // set javaService specific infra params
    params.workflowWorkerImageContainerScanning = "ploigos/ploigos-tool-openscap:v1.0.0"

    // invoke the service pipline
    existingContainerImageScanPloigosWorkflow(params)
}
