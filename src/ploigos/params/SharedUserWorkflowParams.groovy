package ploigos.params

/* User parameters shared by all workflows
 */
class SharedUserWorkflowParams implements Serializable {
    /* log any *sh commands used during execution */
    String verbose = 'false'

    /* Path to the Step Runner configuration to pass to the
     * Workflow Step Runner when running workflow steps. */
    String stepRunnerConfigDir = ''

    /* Name of the Kubernetes Secret containing the PGP private keys to import for use by SOPS
     * to decrypt encrypted Step Runner config. */
    String pgpKeysSecretName = 'jenkins-pgp-private-key'

    /* Kubernetes ServiceAccount that the Jenkins Worker Kubernetes Pod should be deployed with.
     *
     * IMPORTANT
     * ---------
     * So that `buildah`/`podman` can perform rootles container operations then this service account
     * needs to have access to slightly escalted privilages.
     *
     * If `workflowWorkerContainerOperationsUsePrivilegeEscalation` is `true` then this
     * this Kubernetes ServiceAccount needs to have access (via RoleBinding to Role)
     * to a SecurityContextConstraints that allows for `allowPrivilegeEscalation`.
     *
     * If `workflowWorkerContainerOperationsUsePrivilegeEscalation` is `false` then this
     * this Kubernetes ServiceAccount needs to have access (via RoleBinding to Role)
     * to a SecurityContextConstraints that allows for the use of the
     * `SETUID` and `SETGID` capabilities.
     */
    String workflowServiceAccountName = 'pipeline'

    /* Name of the ConfigMap to mount as a trusted CA Bundle.
     * Useful for when interacting with external services signed by an internal CA.
     * If not specified then ignored. */
    String trustedCABundleConfigMapName = ''

    /* Kubernetes ConfigMap name containing shared Ploigos configuration file(s).
     *
     * Typically this would be provided by an infrastrcture or release engineering team so
     * that development teams dont have to have duplicate configuration that can be provided
     * and shared among multiple teams/projects.
     *
     * EX: the uri for container image repoistory would be a good thing
     *     to put in shared config.
     */
    String platformConfigConfigMapName = null

    /* Kubernetes Secret name containing shared Ploigos configuration file(s).
     *
     * IMPORTANT: Since Kubernetes Secrets are not encrypted it is highly recomended that
     *            the contents of this Secret be encrypted with SOPS or similar.
     *
     * Typically this would be provided by an infrastrcture or release engineering team so
     * that development teams dont have to have duplicate configuration that can be provided
     * and shared among multiple teams/projects.
     *
     * EX: the usernmae and password for container image repoistory would be a good thing
     *     to put in shared secret config, assuming crednetials shared with more then one
     *     team/project.
     */
    String platformConfigSecretName = null
}
