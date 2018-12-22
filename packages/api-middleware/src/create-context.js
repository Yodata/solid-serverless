const {get} = require('lodash')
const getHeaders = require('./lib/get-headers')

/**
 * sets event.object (JSON) if req/res body is valid JSON
 * @param {object} event
 * @param {object} event.request
 * @param {object} event.response
 * @param {object} event.scope
 * @param {object} event.policy
 */
module.exports = async (event) => {
    event.requestMethod = event.request.method
    switch(event.requestMethod) {
        case 'POST':
        case 'PUT':
        case 'PATCH':
        if (hasData(event.request)) {
            event.contentType = getContentType(event.request)
            event.object = getData(event.request)
        }
        break
        case 'GET':
        if (event.response && hasData(event.response)) {
            event.contentType = getContentType(event.response)
            event.object = getData(event.response)
        }
        break
        case 'DELETE':
        break
        default:
        throw new Error(`Unexpected request.method ${requestMethod}`)
    }
    return event
}



const getContentType = (message) => {
    return getHeaders(message)['content-type']
}

const hasData = (httpMessage) => {
    let contentType = getContentType(httpMessage)
    if (contentType === 'application/json' || contentType === 'application/ld+json') {
        return true
    }
}

const getData = (httpMessage) => {
    if (hasData(httpMessage)) {
        return JSON.parse(httpMessage.body)
    }
}

const defaultEvent = {
    "request": {
        "id": "fb3f5276-232e-40dc-b0c2-f8ac063d4010",
        "timestamp": {
            "seconds": 1545313643,
            "nanos": 790000000
        },
        "security": {
            "agent": "https://testuser.real-living.yodata.me/profile/card#me",
            "isAdmin": false,
            "isDefaultAllowed": false,
            "allowedModes": [
                "Read",
                "Write",
                "Append",
                "Control"
            ]
        },
        "target": {
            "id": "https://testuser.real-living.yodata.me/test/data-policies/test-password.json",
            "host": "testuser.real-living.yodata.me",
            "path": "/test/data-policies/test-password.json",
            "accessType": "Read"
        },
        "acl": {
            "default": {
                "modes": [
                    "Read",
                    "Write",
                    "Append",
                    "Control"
                ]
            },
            "entities": {
                "https://testapp.real-living.yodata.me/profile/card#me": {
                    "modes": [
                        "Read",
                        "Write",
                        "Append",
                        "Subscribe"
                    ]
                }
            },
            "patterns": {
                "%BASE_URL%/profile/card#me": {
                    "modes": [
                        "Read",
                        "Write",
                        "Append",
                        "Control"
                    ]
                }
            }
        },
        "method": "GET",
        "rawHeaders": {
            "x-amzn-trace-id": [
                "Root=1-5c1b9d6b-d29a152ea68dbbf6449a168c"
            ],
            "x-forwarded-proto": [
                "https"
            ],
            "x-api-key": [
                "testuser"
            ],
            "host": [
                "testuser.real-living.yodata.me"
            ],
            "x-forwarded-port": [
                "443"
            ],
            "content-type": [
                "application/json"
            ],
            "x-forwarded-for": [
                "73.189.108.108"
            ],
            "cache-control": [
                "no-cache"
            ],
            "accept-encoding": [
                "gzip, deflate"
            ],
            "user-agent": [
                "PostmanRuntime/7.4.0"
            ],
            "accept": [
                "*/*"
            ]
        },
        "parameters": {
            "*": [
                "test/data-policies/test-password.json"
            ]
        },
        "body": "",
        "policy": {
            "local": {
                "RedactEmailAddress": {
                    "type": "DataPolicy",
                    "kind": "Transformation",
                    "policy": {
                        "password": "$redact"
                    }
                }
            }
        }
    },
    "response": {
        "status": 200,
        "headers": {
            "Content-Length": "131",
            "Content-Type": "application/json"
        },
        "body": "{\n\t\"type\": \"Person\",\n\t\"name\": \"Bruce Wayne\",\n\t\"email\": \"batman@example.com\",\n\t\"password\": \"same-password-i-use-for-every-account\"\n}"
    },
    "agent": "https://testuser.real-living.yodata.me/profile/card#me",
    "scope": {},
    "policy": {
        "local": {
            "RedactEmailAddress": {
                "type": "DataPolicy",
                "kind": "Transformation",
                "policy": {
                    "password": "$redact"
                }
            }
        }
    }
}