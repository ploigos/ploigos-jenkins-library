package ploigos.params

import ploigos.params.UserServiceWorkflowParams
import ploigos.params.ConstantEnvironmentWorkflowParams

/* All the paramters for service* workflows.
 */
class ServiceWorkflowParams extends UserServiceWorkflowParams implements ConstantEnvironmentWorkflowParams {
    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing app operations, such as unit-test, package, push.
     *
     * This image is also used for other miscalaneous steps that need a place to run but dont
     * need specialized tooling. */
    String workflowWorkerImageAppOperations = null

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing static code analysis step(s). */
    String workflowWorkerImageStaticCodeAnalysis = "ploigos/ploigos-tool-sonar:v1.0.0"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing container operations (build/push/sign/etc) step(s). */
    String workflowWorkerImageContainerOperations = "ploigos/ploigos-tool-containers:v1.0.0"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing container scanning (vulnerability/compliance/etc) step(s) */
    String workflowWorkerImageContainerScanning = "ploigos/ploigos-tool-openscap:v1.0.0"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing deploy step(s). */
    String workflowWorkerImageDeploy = "ploigos/ploigos-tool-argocd:v1.0.0"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing automated-governance step(s). */
    String workflowWorkerImageAutomatedGovernance = "ploigos/ploigos-tool-autogov:v1.0.0"

    /* Container image to use when creating a workflow worker
     * to run pipeline steps when performing validate environment configuration step(s). */
    String workflowWorkerImageValidateEnvironmentConfiguration = "ploigos/ploigos-tool-config-lint:v1.0.0"
}