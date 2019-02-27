phony: prepare package deploy push

prepare:
	@s3-bucket-create

package:
	@lambda-gradle-package

deploy:
	@lambda-gradle-deploy

push: prepare package deploy
