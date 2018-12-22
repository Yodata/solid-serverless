#!/bin/bash

## create/update api-middleware stack

sam deploy \
--capabilities CAPABILITY_IAM \
--template-file packaged.yaml \
--stack-name api-middleware