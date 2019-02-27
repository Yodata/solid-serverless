phony: prepare package deploy push

prepare:
	@s3-bucket-create

package:
	@lambda-sam-package

deploy:
	@lambda-sam-deploy

push: prepare package deploy
