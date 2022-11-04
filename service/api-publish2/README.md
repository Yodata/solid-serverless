# PUBLISH2 (FAST PUBLISH FOR TRUSTED DATA)


## deploy procedures

  0. arc env -a -p PUBLISH2_AUTH_[NAME]:[KEY]:[CIDR]

  key is the reflex key to be authorized
  optionally add CIDR to restrict requests to a specific host IP range.

  1. run npm run deploy / deploy:production

  2. create loadbalancer triggers (in [lambda function config](https://us-west-2.console.aws.amazon.com/lambda/home?region=us-west-2#/functions))
      2a. get /publish2/status -> get-publish2-status
      2b. post /publish2/ -> post-publish2


## API

```http
POST /publish2/ HTTP 1.1
content-type: application/json
x-api-key: [your reflex key]

{
  topic: realestate/listsing#update
  ...
}

RESPONSE
HTTP 202 (ACCEPTED)

{
  id: [message id]
}
```