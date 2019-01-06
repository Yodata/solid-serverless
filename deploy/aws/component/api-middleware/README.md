# api-middleware

Receives an object representing the state of an http message { request, response, context }

Returns the state after processing (see src/index.handler)

## Example Input

```yaml
# http.IncomingMessage
request:
  headers:
    content-type: application/ld+json
    accept: application/ld+json,application/x-turtle
  method: POST
  url: 'https://user.example.com/inbox/'
  # http body string (unparsed)
  body: ''

# http.ServerResponse
response:
  headers:
    link: </context.json>; rel="http://www.w3.org/ns/json-ld#context"
    x-powered-by: yodata/solid-server

# stuff we need goes here
context:
  agent: 'https://max.example.com/profile/card#me'
  instrument: 'https://vendor-application.example.com/profile/card#me'
```

## Local Development

```bash
.
├── README.md                   <-- this file
├── src                         <-- Source code for a lambda function
│   ├── index.js                <-- Lambda function code
│   ├── package.json            <-- NodeJS dependencies
│   └── tests                   <-- Unit tests
│       └── unit
│           └── test_handler.js
└── template.yaml               <-- AWS SAM Deployment Template
```

### Requirements

* AWS CLI already configured with Administrator permission
* [NodeJS 8.10+ installed](https://nodejs.org/en/download/)
* [Docker installed](https://www.docker.com/community-edition)

### Setup

#### Install Dependencies

```bash
cd src
npm install
cd ../
```

#### Invoking function locally

```bash
 > sam local invoke --event example-event.json
```

## Packaging and deployment

Firstly, we need a `S3 bucket` where we can upload our Lambda functions packaged as ZIP before we deploy anything - If you don't have a S3 bucket to store code artifacts then this is a good time to create one:

```bash
aws s3 mb s3://yodata-solid-serverless-package-api-middleware
aws s3 mb s3://BUCKET_NAME
```

Next, run the following command to package our Lambda function to S3:

```bash
. package
```

Next, the following command will create a Cloudformation Stack and deploy your SAM resources.

```bash
. deploy
```

## Testing

```bash
> cd src
> npm run test
```