#!/usr/bin/env bash


sam build && sam package \
--output-template-file template-package.yaml \
--s3-bucket yodata-$AWS_PROFILE-data-policy \
