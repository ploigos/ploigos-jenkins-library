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

# Pipelines

Documentation on the workflow implementations, aka pipelines, provided in this repository.

## Maven

[Implementation](./vars/pipelineMaven.groovy) of the
[TSSC workflow](https://rhtconsulting.github.io/tsc-docs/#tssc-workflow-assembly-tssc) for an
application built with Maven.

### Parameters
See [variable documentation](./vars/pipelineMaven.groovy#L3-L120).

| Parameter                         | Default                     | Description
| --------------------------------- | --------------------------- | -----------
| configDir                         |                             | Path to pass to the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) `--config` flag.
| envNameDev                        | DEV                         | Name of the "Development" environment used in the configuration files and to pass to the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) `--environment` flag where appropriate.
| envNameTest                       | TEST                        | Name of the "Test" environment used in the configuration files and to pass to the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) `--environment` flag where appropriate.
| envNameProd                       | PROD                        | Name of the "Production" environment used in the configuration files and to pass to the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) `--environment` flag where appropriate.
| releaseBranchPatterns             | ['main']                    | Array of regex patterns for branches that should be deployed to Test and then Production environments.
| devBranchPatterns                 | ['^feature/.*$', '^PR-.*$'] | Array of regex patterns for branches that should be deployed to Development environments.
| jenkinsWorkersImageRegesitryURI   | quay.io                     | URI to the container registry hosting the Jenkins workers images used by this pipeline.
| jenkinsWorkersImageRepositoryName | tssc                        | Container image repository name hosting the Jenkins workers images used by this pipeline.
| jenkinsWorkersImageTag            | latest                      | Container image tag to use for the Jenkins workers images used by this pipeline.
| jenkinsWorkersImagePullPolicy     | IfNotPresent                | Policy for pulling new versions of the `jenkinsWorkersImageTag` when running this pipeline.
| updateTsscLibrary                 | false                       | If `true`, then pull the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) source code and build it. If `false` use the version of [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) that is pre-installed in the Jenkins workers container images. <br><br>If `false`, `tsscLibIndexUrl`, `tsscLibExtraIndexUrl`, `tsscLibVersion`, and `tsscLibSourceUrl` are ignored.
| tsscLibIndexUrl                   | https://pypi.org/simple/    | If `tsscLibSourceUrl` is not supplied and `updateTsscLibrary` is `true` this will be passed to pip as `--index-url` for installing the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) and its dependencies. <br><br>:warning: **WARNING**: [PIP is indeterminate](https://pip.pypa.io/en/stable/reference/pip_install/#id48) whether it will pull packages from `--index-url` or `--extra-index-url`, therefor be sure to specify `tsscLibVersion` if trying to pull a specific version from a specific index.
| tsscLibExtraIndexUrl              | https://pypi.org/simple/    | If `tsscLibSourceUrl` is not supplied and `updateTsscLibrary` is `true` this will be passed to pip as `--extra-index-url` for installing the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) and its dependencies. <br><br>:warning: **WARNING**: [PIP is indeterminate](https://pip.pypa.io/en/stable/reference/pip_install/#id48) whether it will pull packages from `--index-url` or `--extra-index-url`, therefor be sure to specify `tsscLibVersion` if trying to pull a specific version from a specific index.
| tsscLibVersion                    |                             | If `tsscLibSourceUrl` is not supplied and `updateTsscLibrary` is `true` this will be passed to pip as the version of the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) to install. <br><br>:warning: **WARNING**: If not given and if `tsscLibSourceUrl` is not supplied and `updateTsscLibrary` is `true` then pip will install the latest from either `tsscLibIndexUrl` or `tsscLibExtraIndexUrl` [indeterminately]((https://pip.pypa.io/en/stable/reference/pip_install/#id48)).
| tsscLibSourceUrl                  |                             | If given and `updateTsscLibrary` is `true` this will be used as the source location to install the TSSC library](https://rhtconsulting.github.io/tssc-python-package/) from rather then from a PEP 503 compliant repository. <br><br>If given then `tsscLibIndexUrl`, `tsscLibExtraIndexUrl`, and `tsscLibVersion` are ignored. <br><br>**EXAMPLE 1:** `git+https://github.com/rhtconsulting/tssc-python-package.git@feature/NAPSSPO-1018`<br>Installs from the public `rhtconsulting` fork from the `feature/NAPSSPO-1018` branch. <br><br>**EXAMPLE 2:** `git+https://gitea.internal.example.xyz/tools/tssc-python-package.git@main`<br>Installs from an internal fork of the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) from the `main` branch. */
| credentialIDsopsPGPKey            | sops-pgp-key                | ID of [Jenkins Credential](https://www.jenkins.io/doc/book/using/using-credentials/#configuring-credentials) of type `Secret file` storing PGP key to install.<br><br>This PGP key will be imported so that the [TSSC library](https://rhtconsulting.github.io/tssc-python-package/) can decrypt any [SOPS](https://github.com/mozilla/sops) encrypted configuration in the given `configDir`.
| kubernetesServiceAccountForJenkinsWorkersPod | jenkins          | Kubernetes ServiceAccount that the Jenkins Worker Kubernetes Pod should be deployed with.<br><br>:bangbang: **IMPORTANT** This Kubernetes `ServiceAccount` needs to have access (via `RoleBinding` to `Role`) to a `SecurityContextConstraints` that can `runAsUser` the UID specified by `kubernetesPodRunAsUserUID`.
| kubernetesPodRunAsUserUID         | 1001                        | The UID to run the Jenkins Worker Kubernetes containers as. <br><br>:bangbang: **IMPORTANT** From experimentation this NEEDS be a UID that exists in the Jenkins workers images. This is due to limitations of how subuid, subgid, and namespaces work and their appropriate ranges not being created for random UID is not created with `useradd` and how that interacts with `buildah unshare` for rootless container builds within a container. <br><br>:notebook: **NOTE** The [tssc-base](https://quay.io/repository/tssc/tssc-base) image uses UID 1001 but if you don't like that UID then you can use https://github.com/rhtconsulting/tssc-containers to create custom versions of the containers and passing in the container ARG `TSSC_USER_UID` to change the UID.

### Example Uses
**Example Jenkinsfile**
```groovy
// Load the TSSC Jenkins Library
library identifier: 'tssc-jenkins-library@v0.12.0',
retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/rhtconsulting/tssc-jenkins-library.git'
])

// run the Maven pipeline
pipelineMaven(
    configDir: 'cicd/tssc/',
)
```

### Reference Projects
List of reference projects that use [this implementation](./vars/pipelineMaven.groovy) of the
[TSSC workflow](https://rhtconsulting.github.io/tsc-docs/#tssc-workflow-assembly-tssc).

* TODO
