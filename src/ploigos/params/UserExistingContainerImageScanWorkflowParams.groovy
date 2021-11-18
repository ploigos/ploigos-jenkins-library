package ploigos.params

/* User parameters for existing container image scan workflows
 */
class UserExistingContainerImageScanWorkflowParams extends SharedUserWorkflowParams {
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
