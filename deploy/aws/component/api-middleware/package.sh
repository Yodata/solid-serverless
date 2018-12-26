#!/bin/bash

sam build && sam package \
--output-template-file template-package.yaml \
--s3-bucket yodata-solid-serverless-api-middleware \
--profile solid
