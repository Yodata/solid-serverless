phony: prepare package deploy push

prepare:
	@ecr-login

package: prepare
	@./gradlew dockerBuildLatest
	@sudo docker tag solid-server-api-front:latest $(AWS_ACCOUNT_ID).dkr.ecr.$(AWS_REGION).amazonaws.com/solid-server-api-front:latest
	@sudo docker push $(AWS_ACCOUNT_ID).dkr.ecr.$(AWS_REGION).amazonaws.com/solid-server-api-front:latest
