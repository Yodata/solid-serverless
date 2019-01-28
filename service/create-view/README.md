# solid-serverless/create-view

Applys a JSON transformation to event.object

## Lambda Payload

```json
// ./src/example/event.json
{
    "object": {
        "@context": "http://schema.org",
        "@type": "AskAction",
        "agent": {
            "@type": "Person",
            "name": "Bob",
            "email": "user@example.com"
        },
        "recipient": {
            "@type": "RealEstateAgent",
            "@id": "https://465156.ds.bhhsresource.com/profile/card#me"
        }
    },
    "context": {
        "@view": {
            "type": "'Lead'",
            "lead": "agent",
            "user": "recipient.'@id'"
        }
    }
}
```

## Result

```json
// ./src/example/response.json
{
    "type": "Lead",
    "lead": {
        "@type": "Person",
        "email": "user@example.com",
        "name": "Bob"
    },
    "user": "https://465156.ds.bhhsresource.com/profile/card#me"
}
```