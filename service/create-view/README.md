# solid-serverless/create-view

## deployment

```bash
# bundle the current code version
> sam build

# update the bundle to s3 using the given aws_profile
# <account_name> must match one of hsf | rl | solid
#
> source package <account_name>

# deploy the service
# <account_name> must match one of hsf | rl | solid
#
> source deploy <account_name>

```

## test

```bash
> jest create-view
```

todo: update readme on service scripts to this content.