#!/bin/bash

sam deploy \
--capabilities CAPABILITY_IAM \
--template-file template-package.yaml \
--stack-name api-middleware \
--profile solid
