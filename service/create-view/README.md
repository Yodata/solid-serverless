# solid-serverless/create-view

## deployment

```bash
# bundle the current code version
> sam build

# update the bundle to s3 using the given aws_profile
# <aws_profile_name> must match the one used to create the bucket
# hsf | rl | solid
> source package <aws_profile_name>

# deploy the service
# <aws_profile_name> must match the one used to create the bucket
# hsf | rl | solid
> source deploy <aws_profile_name>
```

## test

```bash
> jest create-view
```