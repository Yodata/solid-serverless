#!/usr/bin/env bash

sam deploy \
--capabilities CAPABILITY_IAM \
--template-file template-package.yaml \
--stack-name {{cookiecutter.stackName}} \
--profile {{cookiecutter.awsProfileName}}
--s3-bucket yodata-$AWS_PROFILE-api-middleware \

