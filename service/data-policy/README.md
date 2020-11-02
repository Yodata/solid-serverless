# data-policy service

manage and applies data policies to events

```bash
.
├── README.md
├── event.json
├── response.json
├── script
│   ├── create-bucket
│   ├── deploy
│   └── package
├── src
│   ├── __tests__
│   │   └── unit
│   │       └── test-handler.js
│   ├── index.js
│   └── package.json
└── template.yaml
```

## Requirements

* AWS CLI already configured with Administrator permission
* [NodeJS 8.10+ installed](https://nodejs.org/en/download/)
* [Docker installed](https://www.docker.com/community-edition)

## Setup process

### Building the project

```bash
. script/build
```

By default, this command writes built artifacts to `.aws-sam/build` folder.

### Local development

```bash
. script/invoke
```

## Packaging and deployment

If you need to create the bucket for this package...

```bash
. script/create-bucket
```

Run the following command to package our Lambda function to S3:

```bash
. script/package
```

Next, the following command will create a Cloudformation Stack and deploy your SAM resources.

```bash
. script/deploy
```

## Testing

1. put a file on your pod for testing policies

```bash
# /data-policy/test-data-policy.json

HTTP PUT /public/test-data-policy.json
content-type: application/json
x-api-key:  xxxx

{
  "type": "test",
  "description": "this object had two fields that should have policies applied, additionalProperty.originalAffiliationDate and additionalProperty.",
  "testdate": "2020-10-21T19:49:01Z"
}
```

2. get the file and note the value of "testdate"

```bash
HTTP GET /public/test-data-policy.json

{
  "type": "test",
  "description": "this object had two fields that should have policies applied, additionalProperty.originalAffiliationDate and additionalProperty.",
  "testdate": "2020-10-21T19:49:01Z"
}

```

3.  put a @redact data policy on that field in your pod data-policiy file `/public/yodata/data-policy.json`


```bash
# /data-policy/data-policy.json

HTTP PUT /public/yodata/data-policy.json
content-type: application/json
x-api-key: xxx
{
	"redacttestdate": {
		"effect": "Transform",
		"processor": "Yodata",
		"type": "DataPolicy",
		"value": "{\"testdate\":{\"@redacted\":true}}"
	}
}

```


4. get the file again and confirm the value of "testdate" has been redacted

```bash
HTTP GET /public/test-data-policy.json

{
  "type": "test",
  "description": "this object had two fields that should have policies applied, additionalProperty.originalAffiliationDate and additionalProperty.",
  "testdate": "@redacted"
}

```
