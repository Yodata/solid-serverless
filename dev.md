# Developer guide

## Purpose

This document will guide you into creating a working developer environment that can be used to build and deploy Solid Serverless.

The project preferred environment is its own docker image, to be run as an interactive shell with all the tools needed installed and configured. It is also possible to prepare your local environment, in which case you will need to infer the various needed packages from the Docker file build steps and the `makefile`.

## Requirements

Regardless of the way you will setup your environment, you need the following installed:

- `make`, which should be part of any Linux/OSX system
- [Docker](https://docs.docker.com/install/)

## Preparation

### Build

The first step is to build your local Docker image that will be used in the rest of the guide and all the other deployment documents.

Clone the repo if you haven't done so, and cd into it:

```bash
git clone git@github.com:Yodata/solid-serverless.git
cd solid-serverless
```

Then run the following to build your local docker image (This will take some time):

```bash
make docker-image-build
```

A new image `yodata/solid-serverless-build` will be available.

### Configure

At the root of the repository, you will find a file called `env.sample`. This file contains the various fundamental environment variables used to compute the others when running the build docker container.

Copy it to `env` like so:

```bash
cp env.sample env
```

Then edit the file and adapt to your needs - The required values to be set are at the beginning, labelled as such.

## Usage

At the root of the repository, run:

```bash
make docker-shell
```

You are now in the developer environment, directly at the root of your repository, which is mounted from the host and thus directly editable. You can now proceed with the other documentation. All commands should happen in this environment.

### Commands

The directory `scripts/` is included in the `PATH` and thus all scripts are directly accessible and executable as commands. **You normally do not need to call them directly** as they are called in the dedicated `makefile` of each service in the right order.

The following scripts are available if manual use is needed:

- `ecr-login` - Used to log you in ECR to push images.
- `lambda-gradle-package` - Package a Java/Gradle-based service and send its artifact to its S3 bucket.
- `lambda-gradle-deploy` - Update the AWS Lambda of a service, based on a previously uploaded artifact.
- `lambda-sam-deploy` - Package a Node/SAM-based service and send its artifact to its S3 bucket.
- `lambda-sam-package`  - Update the AWS Lambda of a service, based on a previously uploaded artifact.
- `s3-bucket-compute-name` - Compute a S3 bucket name given the environment and the current working directory.
- `s3-bucket-create` Create a S3 bucket, using the value returned by `s3-bucket-compute-name` if no argument is given.
- `service-compute-name` - Compute a service name given the environment and the current working directory.
- `setEnv` - Set all the required environment variables using the `env` file at the base of the repository.

If no argument is given, the needed parameter will be inferred from the environment and current working directory.

### Service

Each service is located in its dedicated directory in `/service`, and has a dedicated `makefile` with a set of target names common to all services:

- `prepare` - Create required local and/or remote resource(s), like a S3 bucket, if not existing yet
- `package` - Build the artifacts and uploaded them to the remote environment, if applicable
- `deploy` - Deploy the service on the remote environment, if applicable
- `push` - All of the above - This is the short-hand command for "build, upload and deploy my changes".

The typical command you would run after changing some code to deploy it in the environment, with your working directory being the service directory:

```bash
make push
```

