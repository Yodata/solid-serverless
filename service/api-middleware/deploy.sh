#!/usr/bin/env bash

sam deploy \
--capabilities CAPABILITY_IAM \
--template-file template-package.yaml \
--stack-name api-middleware \
--profile $AWS_PROFILE