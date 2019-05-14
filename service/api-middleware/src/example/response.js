module.exports = {
    "contentType": "application/json",
    "hasData": true,
    "isAllowed": true,
    "object": {
        "password": "[PASSWORD]"
    },
    "policy": {
        "local": {
            "RedactPassword": {
                "effect": "Transform",
                "processor": "Yodata",
                "type": "DataPolicy",
                "value": "{ \"password\": {\"value\": \"[PASSWORD]\"} }"
            }
        }
    },
    "request": {
        "method": "GET"
    },
    "response": {
        "body": "eyJwYXNzd29yZCI6IltQQVNTV09SRF0ifQ==",
        "headers": {
            "Content-Type": [
                "application/json"
            ]
        },
        "isBase64Encoded": true
    },
    "scope": {
        "NoDogsAllowed": {
            "condition": {
                "object": {
                    "type": "Dog"
                }
            },
            "effect": "Deny"
        }
    },
    "stage": "response"
}