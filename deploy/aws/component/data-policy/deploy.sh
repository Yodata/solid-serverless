#!/bin/bash

sam deploy \
--capabilities CAPABILITY_IAM \
--template-file template-package.yaml \
--stack-name data-policy \
--profile solid \
--parameter-overrides clientid=dave-admin
