phony: docker-image-build, docker-shell, service-push

docker-image-build:
	@docker build -t yodata/solid-serverless-build .

docker-shell:
	@docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $(shell pwd):/home/builder/src -v /var/tmp/yodata/solid-serverless/build/cache:/var/tmp/cache yodata/solid-serverless-build:latest

service-push:
	@service-push all
