#! /usr/local/bin/bash

sam package \
--template-file template.yaml \
--output-template-file packaged.yaml \
--s3-bucket yodata-solid-serverless-package-api-middleware
--profile solid
