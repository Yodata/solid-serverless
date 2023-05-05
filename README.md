# Solid Serverless
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FYodata%2Fsolid-serverless.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FYodata%2Fsolid-serverless?ref=badge_shield)


Solid Serverless is a multi-tenant, USER CENTRIC (aka decentralized) data hosting platform that has been in production use 
for over 2 years with accounts in the residential real estate domain serving over 100,000 agents
with 50+ vendor integrations to date.

SS serves a vendor agnostic API for your internal and external services will use to send and recieve messages with you (or any of your hosted pods).

## Benefits of the decentralized,open-source solution.

1. Realtime backup from all systems in one place
2. Protection in the event a vendor is compromised by ransoware
3. Reduces the time and cost of a CRM migration by 75%+
5. Reduces cost of related analytics projects by up to 80%
6. Reduces long-term maintenance fees
7. No restrictions on customization
8. Customizations can be proprietary

## What is Yodata Enterprise Solid Serverless for Real Estate(YESSRE)

Solid Serverless is an AWS cloud-native implementation of Linked-Data notification 1.0 spec.
In conjunction with [standard real estate events](https://github.com/Yodata/real-estate)
decentralized message bus that implements the Linked-Data family of standards.
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

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FYodata%2Fsolid-serverless.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2FYodata%2Fsolid-serverless?ref=badge_large)
