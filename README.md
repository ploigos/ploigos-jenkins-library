# TSSC Reference Jenkins Library

This repository contains reference implementations of TSSC pipelines, provided as groovy functions within a Jenkins Library.

The following pipelines are provided:
* **[vars/pipelineJava8.groovy](vars/pipelineJava8.groovy)** - A Java 8 Jenkins pipeline that can be used to deploy Maven based, executable uber JAR Java applications

## Creating a Jenkins Library Repository

While this repository can be used directly, organizations may wish to create their own Jenkins library of pipelines to suit their needs. Simply create a source code repository using the structure of this repository as a guide. The vars directory must be located at the root of the repository along with an optional `src` directory containing any supporting groovy code.  Refer to the [Jenkins library documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/) for further details on constructing your library.


## Deploying a pipeline
The pipelines defined here can be deployed by importing this library and making the appropriate Groovy call to the pipeline function.

Consider the following typical directory structure for an existing Apache Maven based Java application:
```
.
├── pom.xml
├── README.md
├── src
    ├── main
    │   ├── java
    │   └── resources
    └── test
        ├── java
        └── resources
```

In order to add a TSSC pipeline from this library, a new `cicd` subdirectory is added with some key files. Updated directory structure example below:

```
.
├── cicd
|   ├── tssc-config.yml
│   ├── Deployment
│   │   └── values.yaml.j2
│   ├── Jenkins
│       └── Jenkinsfile
├── pom.xml
├── README.md
├── sonar-project.properties
├── src
    ├── main
    │   ├── java
    │   └── resources
    └── test
        ├── java
        └── resources
```

* **tssc-config.yml** Defines the configuration parameters required for each stage of the pipeline. Refer to the [TSSC Python Package Documentation](https://rhtconsulting.github.io/tssc-python-package/) for further information including config file details. [tssc-config example](examples/tssc-config-java8.yml)

* **values.yaml.j2** Jinja template used as an input for the the ArgoCD based deployment of the application. [values.yaml.j2 Example](examples/values.yaml.j2)

* **Jenkinsfile** Calls this library with appropriate parameters to deploy a pipeline. [Jenkinsfile Example](examples/Jenkinsfile.java8)

* **sonar-project.properties** SonarQube configuration file used for static code analysis. [sonar-project.properties Example](examples/sonar-project-sonar-java8.yml)

