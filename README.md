# Solid Serverless

Solid Serverless is a cloud-native, decentralized message bus that implements the Linked-Data family of standards.
It's currently in production use by several companies with the largest hosting over 100,000 PODs.

## Resources

- [Developer guide](dev.md) **<-- START HERE**
- [AWS Deployment guide](deploy/aws/README.md)

## Services

### API

- [Front](service/api-front/README.md)
- [Key Service](service/api-key-service/README.md)
- [Middleware](service/api-middleware/README.md)

### Middleware

- [check-scope](service/check-scope/README.md)
- [create-view](service/create-view/README.md)
- [data-policy](service/data-policy/README.md)
- [data-processing](service/data-processing/README.md)
- [echo-service](service/echo-service/README.md)
- [validate-schema](service/validate-schema/README.md)

### Subscription

- [Manager](service/subscription-manager/README.md) made of:
  - Event processor
  - Inbox processor
  - Outbox processor
  - Push processor
