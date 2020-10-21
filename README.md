# TSSC Jenkins Library

Library of Jenkins domain specific language implementation of the
[TSSC workflow](https://rhtconsulting.github.io/tsc-docs/#tssc-workflow-assembly-tssc).

# Use

Documentation on the different ways this repository can be used.

## Directly

If your organization has decided to use the
[TSSC workflow](https://rhtconsulting.github.io/tsc-docs/#tssc-workflow-assembly-tssc) as defined
and has connectivity to Github then your organization can setup your `Jenkinsfile`s to directly
reference the workflows defined in this project.

> :warning: **WARNING** When referencing this project directly
always be sure to reference a tag of this project and not the `main` branch. As the
[TSSC workflow](https://rhtconsulting.github.io/tsc-docs/#tssc-workflow-assembly-tssc) changes in
definition the `main` branch of this project will update and could cause breaking changes to
your organizations workflows if your configuration files are not updated to match the new
changes.

## Reference

If your organization has defined its own derivative of the
[TSSC workflow](https://rhtconsulting.github.io/tsc-docs/#tssc-workflow-assembly-tssc) then
this repository could be used as reference to create your oranizations own Jenkins library project
similarly defined with different steps and/or order to steps while still using the
[tssc-python-package abstraction layer](https://github.com/rhtconsulting/tssc-python-package).

## Internal Fork

If your organization operates in a connection disadvantaged environment then you could internally
fork this repository into your network.

It is suggested that your organization sets up a way to monitor this repository for new releases
and update your internal fork and referencing projects accordingly.

It is requested that if you find any bugs or make any enhancements in your internal fork
that would be applicable to a wider audience that you do a merge request against this repository
so the wide community can benefit.

# Workflows

Documentation on the workflow implementations, aka pipelines, provided in this repository.

## Minimal (ploigosWorkflowMinimal)

Implements the Minimal Ploigos Workflow.

### Parameters

| Parameter                         | Default                       | Description
| --------------------------------- | ----------------------------- | -----------
| verbose                           | `'false'`                     | log any \*sh commands used during execution
| stepRunnerConfigDir               | `''`                          | Path to the Step Runner configuration to pass to the Workflow Step Runner when running workflow steps.
| pgpKeysSecretName                 |                               | Name of the Kubernetes Secret containing the PGP private keys to import for use by SOPS to decrypt encrypted Step Runner config.
| envNameDev                        | `'DEV'`                       | Name of the "Development" environment used in the Step Runner configuration files and to pass to the Workflow Step Runner when running a step targeted to the "Development" environment.
| envNameTest                       | `'TEST'`                      | Name of the "Test" environment used in the Step Runner configuration files and to pass to the Workflow Step Runner when running a step targeted to the "Test" environment.
| envNameProd                       | `'PROD'`                      | Name of the "Production" environment used in the Step Runner configuration files and to pass to the Workflow Step Runner when running a step targeted to the "Production" environment.
| ciOnlyGitRefPatterns              | `['^$']`                      | Regex pattern for git references that should only go through the Continues Integration (CI) workflow.
| devGitRefPatterns                 | `['^feature/.+$', '^PR-.+$']` | Regex pattern for git references that should go through the Continues Integration (CI) workflow and then the deployment to "Development" environment(s) (IE: "DEV" environment) workflow.
| releaseGitRefPatterns             | `['^main$']`                  | Regex pattern for git references that should go through the Continues Integration (CI) workflow and then the deployment to "Release" environment(s) (IE: "TEST" and then "PROD" environments) workflow.
| stepRunnerPackageName             | `'tssc'`                      | Name of the python package to use as the Workflow Step Runner.
| stepRunnerUpdateLibrary           | `false`                       | If 'true', then pull the Workflow Step Runner library source code and build it. <br/>If 'false', use the version of the Workflow Step Runner library that is pre-installed in the CI worker images. <br/><br/>If 'false' then the following parameters are ignored:<br/><ul><li>`stepRunnerLibSourceUrl`</li><li>`stepRunnerLibIndexUrl`</li><li>`stepRunnerLibExtraIndexUrl`</li><li>`stepRunnerLibVersion`</li></ul>
| stepRunnerLibIndexUrl             | `'https://pypi.org/simple/'`  | If `stepRunnerUpdateLibrary` is true and `stepRunnerLibSourceUrl` is not supplied then this will be passed to pip as '--index-url' for installing the Workflow Step Runner library and its dependencies.<br/><br/>**NOTE:** PIP is indeterminate whether it will pull packages from `--index-url` or `--extra-index-url`, therefor be sure to specify `stepRunnerLibVersion` if trying to pull a specific version from a specific index.<br/><br/>See:<ul><li>https://pip.pypa.io/en/stable/reference/pip_install/#id48</li></ul>
| stepRunnerLibExtraIndexUrl        | `'https://pypi.org/simple/'`  | If `stepRunnerUpdateLibrary` is true and `stepRunnerLibSourceUrl` is not supplied then this will be passed to pip as '--extra-index-url' for installing the Workflow Step Runner library and its dependencies.<br/><br/>**NOTE:** PIP is indeterminate whether it will pull packages from `--index-url` or `--extra-index-url`, therefor be sure to specify `stepRunnerLibVersion` if trying to pull a specific version from a specific index.<br/><br/>See:<ul><li>https://pip.pypa.io/en/stable/reference/pip_install/#id48</li></ul>
| stepRunnerLibVersion              |                               | If `stepRunnerUpdateLibrary` is true and `stepRunnerLibSourceUrl` is not supplied then this will be passed to pip as as the version of the Workflow Step Runner library to install.<br/><br/>**NOTE:** If not given pip will install the latest from either `stepRunnerLibIndexUrl` or `stepRunnerLibExtraIndexUrl` indeterminately.
| stepRunnerLibSourceUrl            |                               | If none empty value given and `stepRunnerUpdateLibrary` is true this will be used as the source location to install the Workflow Step Runner library from rather then from a PEP 503 compliant repository.<br/><br/>If given then the following parameters are ignored:<ul><li>`stepRunnerLibIndexUrl`</li><li>`stepRunnerLibExtraIndexUrl`</li><li>`stepRunnerLibVersion`</li></ul><br/><br/>Examples:<ul><li>`git+https://github.com/ploigos/ploigos-step-runner.git@feature/NAPSSPO-1018` installs from the public 'ploigos' fork from the 'feature/NAPSSPO-1018' branch.</li><li>`git+https://gitea.internal.example.xyz/tools/ploigos-step-runner.git@main` installs from an internal fork of the step runner library from the 'main' branch.</li></ul>
| workflowWorkerRunAsUser           | `1001`                        | The UID to run the workflow worker containers as.<br/><br/>**IMPORTANT:** From experimentation this NEEDS be a UID that exists in the worker container images. This is due to limitations of how subuid, subgid, and namespaces work and their appropriate ranges not being created for random UID is not created with `useradd` and how that interacts with `buildah unshare` for rootless container builds within a container.<br/><br/>**NOTE:** The quay.io/ploigos/ploigos-base image uses UID 1001 but if you don't like that UID then you can use https://github.com/ploigos/ploigos-containers to create custom versions of the Ploigos workflow containers and passing in the container ARG `PLOIGOS_USER_UID` to change the UID.
| workflowWorkersImagePullPolicy    | `'IfNotPresent'`              | Policy for pulling new versions of the imageTag for the CI worker images when running this pipeline.
| workflowWorkerImageDefault        | `'ploigos/ploigos-ci-agent-jenkins:latest'` | Container image to use when creating a workflow worker to run pipeline steps when no other specific container image has been specified for that step.
| workflowWorkerImagePackage        |                               | Container image to use when creating a workflow worker to run pipeline steps when performing package application step(s).
| workflowWorkerImagePushArtifacts  |                               | Container image to use when creating a workflow worker to run pipeline steps when performing push push packaged artifacts step(s).
| workflowWorkerImageContainerOperations | `'ploigos/ploigos-tool-containers:latest'` | Container image to use when creating a workflow worker to run pipeline steps when performing container operations (build/push/etc) step(s).
| workflowWorkerImageDeploy         | `'ploigos/ploigos-tool-argocd:latest'` | Container image to use when creating a workflow worker to run pipeline steps when performing deploy step(s).
| workflowServiceAccountName        | `'jenkins'`                   | Kubernetes ServiceAccount that the Jenkins Worker Kubernetes Pod should be deployed with.<br/><br/>**IMPORTANT:** This Kubernetes ServiceAccount needs to have access (via RoleBinding to Role) to a SecurityContextConstraints that can runAsUser workflowWorkerRunAsUser.

### Examples

#### Use Ploigos Step Runner from embedded binary
**NOTE:** Assumes container platform is configured with container image search path that can find
the Ploigos workflow worker images by short name. EX, quay.io is on the container image search path.

```
// Load the Ploigos Jenkins Library
library identifier: 'ploigos-jenkins-library@v1.0.0',
retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/ploigos/ploigos-jenkins-library.git'
])

// run the pipeline
ploigosWorkflowStandard(
    stepRunnerConfigDir: 'cicd/ploigos-step-runner-config/',
    pgpKeysSecretName: 'pgp-keys-ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowServiceAccountName: 'ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowWorkerImageDefault: 'ploigos/ploigos-ci-agent-jenkins:v1.0.0',
    workflowWorkerImagePackage: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImagePushArtifacts: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImageContainerOperations: 'ploigos/ploigos-tool-containers:v1.0.0',
    workflowWorkerImageDeploy: 'ploigos/ploigos-tool-argocd:v1.0.0'
)
```

#### Use Ploigos Step Runner from source
**NOTE:** Assumes container platform is configured with container image search path that can find
the Ploigos workflow worker images by short name. EX, quay.io is on the container image search path.

```
// Load the Ploigos Jenkins Library
library identifier: 'ploigos-jenkins-library@v1.0.0',
retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/ploigos/ploigos-jenkins-library.git'
])

// run the pipeline
ploigosWorkflowStandard(
    stepRunnerConfigDir: 'cicd/ploigos-step-runner-config/',
    pgpKeysSecretName: 'pgp-keys-ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowServiceAccountName: 'ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowWorkerImageDefault: 'ploigos/ploigos-ci-agent-jenkins:v1.0.0',
    workflowWorkerImagePackage: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImagePushArtifacts: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImageContainerOperations: 'ploigos/ploigos-tool-containers:v1.0.0',
    workflowWorkerImageDeploy: 'ploigos/ploigos-tool-argocd:v1.0.0',

    stepRunnerUpdateLibrary: true,
    stepRunnerLibSourceUrl: "git+https://github.com/ploigos/ploigos-step-runner.git@main"
)
```

## Standard (ploigosWorkflowStandard)

Implements the Standard Ploigos Workflow.

### Parameters

| Parameter                         | Default                       | Description
| --------------------------------- | ----------------------------- | -----------
| verbose                           | `'false'`                     | log any \*sh commands used during execution
| stepRunnerConfigDir               | `''`                          | Path to the Step Runner configuration to pass to the Workflow Step Runner when running workflow steps.
| pgpKeysSecretName                 |                               | Name of the Kubernetes Secret containing the PGP private keys to import for use by SOPS to decrypt encrypted Step Runner config.
| envNameDev                        | `'DEV'`                       | Name of the "Development" environment used in the Step Runner configuration files and to pass to the Workflow Step Runner when running a step targeted to the "Development" environment.
| envNameTest                       | `'TEST'`                      | Name of the "Test" environment used in the Step Runner configuration files and to pass to the Workflow Step Runner when running a step targeted to the "Test" environment.
| envNameProd                       | `'PROD'`                      | Name of the "Production" environment used in the Step Runner configuration files and to pass to the Workflow Step Runner when running a step targeted to the "Production" environment.
| ciOnlyGitRefPatterns              | `['^$']`                      | Regex pattern for git references that should only go through the Continues Integration (CI) workflow.
| devGitRefPatterns                 | `['^feature/.+$', '^PR-.+$']` | Regex pattern for git references that should go through the Continues Integration (CI) workflow and then the deployment to "Development" environment(s) (IE: "DEV" environment) workflow.
| releaseGitRefPatterns             | `['^main$']`                  | Regex pattern for git references that should go through the Continues Integration (CI) workflow and then the deployment to "Release" environment(s) (IE: "TEST" and then "PROD" environments) workflow.
| stepRunnerPackageName             | `'tssc'`                      | Name of the python package to use as the Workflow Step Runner.
| stepRunnerUpdateLibrary           | `false`                       | If 'true', then pull the Workflow Step Runner library source code and build it. <br/>If 'false', use the version of the Workflow Step Runner library that is pre-installed in the CI worker images. <br/><br/>If 'false' then the following parameters are ignored:<br/><ul><li>`stepRunnerLibSourceUrl`</li><li>`stepRunnerLibIndexUrl`</li><li>`stepRunnerLibExtraIndexUrl`</li><li>`stepRunnerLibVersion`</li></ul>
| stepRunnerLibIndexUrl             | `'https://pypi.org/simple/'`  | If `stepRunnerUpdateLibrary` is true and `stepRunnerLibSourceUrl` is not supplied then this will be passed to pip as '--index-url' for installing the Workflow Step Runner library and its dependencies.<br/><br/>**NOTE:** PIP is indeterminate whether it will pull packages from `--index-url` or `--extra-index-url`, therefor be sure to specify `stepRunnerLibVersion` if trying to pull a specific version from a specific index.<br/><br/>See:<ul><li>https://pip.pypa.io/en/stable/reference/pip_install/#id48</li></ul>
| stepRunnerLibExtraIndexUrl        | `'https://pypi.org/simple/'`  | If `stepRunnerUpdateLibrary` is true and `stepRunnerLibSourceUrl` is not supplied then this will be passed to pip as '--extra-index-url' for installing the Workflow Step Runner library and its dependencies.<br/><br/>**NOTE:** PIP is indeterminate whether it will pull packages from `--index-url` or `--extra-index-url`, therefor be sure to specify `stepRunnerLibVersion` if trying to pull a specific version from a specific index.<br/><br/>See:<ul><li>https://pip.pypa.io/en/stable/reference/pip_install/#id48</li></ul>
| stepRunnerLibVersion              |                               | If `stepRunnerUpdateLibrary` is true and `stepRunnerLibSourceUrl` is not supplied then this will be passed to pip as as the version of the Workflow Step Runner library to install.<br/><br/>**NOTE:** If not given pip will install the latest from either `stepRunnerLibIndexUrl` or `stepRunnerLibExtraIndexUrl` indeterminately.
| stepRunnerLibSourceUrl            |                               | If none empty value given and `stepRunnerUpdateLibrary` is true this will be used as the source location to install the Workflow Step Runner library from rather then from a PEP 503 compliant repository.<br/><br/>If given then the following parameters are ignored:<ul><li>`stepRunnerLibIndexUrl`</li><li>`stepRunnerLibExtraIndexUrl`</li><li>`stepRunnerLibVersion`</li></ul><br/><br/>Examples:<ul><li>`git+https://github.com/ploigos/ploigos-step-runner.git@feature/NAPSSPO-1018` installs from the public 'ploigos' fork from the 'feature/NAPSSPO-1018' branch.</li><li>`git+https://gitea.internal.example.xyz/tools/ploigos-step-runner.git@main` installs from an internal fork of the step runner library from the 'main' branch.</li></ul>
| workflowWorkerRunAsUser           | `1001`                        | The UID to run the workflow worker containers as.<br/><br/>**IMPORTANT:** From experimentation this NEEDS be a UID that exists in the worker container images. This is due to limitations of how subuid, subgid, and namespaces work and their appropriate ranges not being created for random UID is not created with `useradd` and how that interacts with `buildah unshare` for rootless container builds within a container.<br/><br/>**NOTE:** The quay.io/ploigos/ploigos-base image uses UID 1001 but if you don't like that UID then you can use https://github.com/ploigos/ploigos-containers to create custom versions of the Ploigos workflow containers and passing in the container ARG `PLOIGOS_USER_UID` to change the UID.
| workflowWorkersImagePullPolicy    | `'IfNotPresent'`              | Policy for pulling new versions of the imageTag for the CI worker images when running this pipeline.
| workflowWorkerImageDefault        | `'ploigos/ploigos-ci-agent-jenkins:latest'` | Container image to use when creating a workflow worker to run pipeline steps when no other specific container image has been specified for that step.
| workflowWorkerImageUnitTest       |                               | Container image to use when creating a workflow worker to run pipeline steps when performing unit test step(s).
| workflowWorkerImagePackage        |                               | Container image to use when creating a workflow worker to run pipeline steps when performing package application step(s).
| workflowWorkerImageStaticCodeAnalysis |                           | Container image to use when creating a workflow worker to run pipeline steps when performing static code analysis step(s).
| workflowWorkerImagePushArtifacts  |                               | Container image to use when creating a workflow worker to run pipeline steps when performing push push packaged artifacts step(s).
| workflowWorkerImageContainerOperations | `'ploigos/ploigos-tool-containers:latest'` | Container image to use when creating a workflow worker to run pipeline steps when performing container operations (build/push/etc) step(s).
| workflowWorkerImageContainerImageStaticComplianceScan    | `'ploigos/ploigos-tool-openscap:latest'` | Container image to use when creating a workflow worker to run pipeline steps when performing container image static compliance scan step(s).
| workflowWorkerImageContainerImageStaticVulnerabilityScan | `'ploigos/ploigos-tool-openscap:latest'` | Container image to use when creating a workflow worker to run pipeline steps when performing container image static vulnerability scan step(s).
| workflowWorkerImageDeploy         | `'ploigos/ploigos-tool-argocd:latest'` | Container image to use when creating a workflow worker to run pipeline steps when performing deploy step(s).
| workflowWorkerImageValidateEnvironmentConfiguration      | `'ploigos/ploigos-tool-config-lint:latest'` | Container image to use when creating a workflow worker to run pipeline steps when performing validate environment configuration step(s).
| workflowWorkerImageUAT            |                               | Container image to use when creating a workflow worker to run pipeline steps when performing user acceptance tests (UAT) step(s).
| workflowServiceAccountName        | `'jenkins'`                   | Kubernetes ServiceAccount that the Jenkins Worker Kubernetes Pod should be deployed with.<br/><br/>**IMPORTANT:** This Kubernetes ServiceAccount needs to have access (via RoleBinding to Role) to a SecurityContextConstraints that can runAsUser workflowWorkerRunAsUser.

### Examples

#### Use Ploigos Step Runner from embedded binary
**NOTE:** Assumes container platform is configured with container image search path that can find
the Ploigos workflow worker images by short name. EX, quay.io is on the container image search path.

```
// Load the Ploigos Jenkins Library
library identifier: 'ploigos-jenkins-library@v1.0.0',
retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/ploigos/ploigos-jenkins-library.git'
])

// run the pipeline
ploigosWorkflowStandard(
    stepRunnerConfigDir: 'cicd/ploigos-step-runner-config/',
    pgpKeysSecretName: 'pgp-keys-ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowServiceAccountName: 'ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowWorkerImageDefault: 'ploigos/ploigos-ci-agent-jenkins:v1.0.0',
    workflowWorkerImageUnitTest: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImagePackage: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImageStaticCodeAnalysis: 'ploigos/ploigos-tool-sonar:v1.0.0',
    workflowWorkerImagePushArtifacts: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImageContainerOperations: 'ploigos/ploigos-tool-containers:v1.0.0',
    workflowWorkerImageContainerImageStaticComplianceScan: 'ploigos/ploigos-tool-openscap:v1.0.0',
    workflowWorkerImageContainerImageStaticVulnerabilityScan: 'ploigos/ploigos-tool-openscap:v1.0.0',
    workflowWorkerImageDeploy: 'ploigos/ploigos-tool-argocd:v1.0.0',
    workflowWorkerImageValidateEnvironmentConfiguraiton: 'ploigos/ploigos-tool-config-lint:v1.0.0',
    workflowWorkerImageUAT: 'ploigos/ploigos-tool-maven:v1.0.0'
)
```

#### Use Ploigos Step Runner from source
**NOTE:** Assumes container platform is configured with container image search path that can find
the Ploigos workflow worker images by short name. EX, quay.io is on the container image search path.

```
// Load the Ploigos Jenkins Library
library identifier: 'ploigos-jenkins-library@v1.0.0',
retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/ploigos/ploigos-jenkins-library.git'
])

// run the pipeline
ploigosWorkflowStandard(
    stepRunnerConfigDir: 'cicd/ploigos-step-runner-config/',
    pgpKeysSecretName: 'pgp-keys-ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowServiceAccountName: 'ploigos-workflow-ref-quarkus-mvn-jenkins-std-fruit',

    workflowWorkerImageDefault: 'ploigos/ploigos-ci-agent-jenkins:v1.0.0',
    workflowWorkerImageUnitTest: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImagePackage: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImageStaticCodeAnalysis: 'ploigos/ploigos-tool-sonar:v1.0.0',
    workflowWorkerImagePushArtifacts: 'ploigos/ploigos-tool-maven:v1.0.0',
    workflowWorkerImageContainerOperations: 'ploigos/ploigos-tool-containers:v1.0.0',
    workflowWorkerImageContainerImageStaticComplianceScan: 'ploigos/ploigos-tool-openscap:v1.0.0',
    workflowWorkerImageContainerImageStaticVulnerabilityScan: 'ploigos/ploigos-tool-openscap:v1.0.0',
    workflowWorkerImageDeploy: 'ploigos/ploigos-tool-argocd:v1.0.0',
    workflowWorkerImageValidateEnvironmentConfiguraiton: 'ploigos/ploigos-tool-config-lint:v1.0.0',
    workflowWorkerImageUAT: 'ploigos/ploigos-tool-maven:v1.0.0',

    stepRunnerUpdateLibrary: true,
    stepRunnerLibSourceUrl: "git+https://github.com/ploigos/ploigos-step-runner.git@main"
)
```