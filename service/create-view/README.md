# solid-serverless/create-view

Applys a JSON transformation to event.object

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

```bash
jest
```
