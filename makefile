docker-image-build:
	docker build -t yodata/solid-serverless-build .

docker-shell:
	@docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $(PWD):/home/builder/src --name solid-serverless yodata/solid-serverless-build

service-push:
	@service-push all
