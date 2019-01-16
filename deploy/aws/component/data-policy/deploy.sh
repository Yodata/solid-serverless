#!/bin/bash

sam deploy \
--capabilities CAPABILITY_IAM \
--template-file template-package.yaml \
--stack-name data-policy \
--parameter-overrides clientid=$CLIENT_ID
