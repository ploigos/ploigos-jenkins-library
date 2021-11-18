package ploigos.params

import ploigos.params.UserServiceWorkflowParams
import ploigos.params.ConstantEnvironmentWorkflowParams

/* All the paramters for existingContainerImageScan* workflows.
 */
class ExistingContainerImageScanWorkflowParams extends UserExistingContainerImageScanWorkflowParams implements ConstantEnvironmentWorkflowParams {
    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing container scanning (vulnerability/compliance/etc) step(s) */
    String workflowWorkerImageContainerScanning = null
}
