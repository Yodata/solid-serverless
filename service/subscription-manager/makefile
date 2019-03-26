phony: prepare package deploy push

prepare:
	@s3-bucket-create

package:
	@lambda-gradle-package

deploy:
	@lambda-gradle-deploy store-event-processor
	@lambda-gradle-deploy inbox-processor
	@lambda-gradle-deploy outbox-processor
	@lambda-gradle-deploy push-processor
	@lambda-gradle-deploy publish-processor

push: prepare package deploy
