const logger = require('./logger')
const get = require('lodash/get')
const set = require('lodash/set')
const createView = require('./create-view')

/**
 * apply data policies
 * @param {object} event
 */
module.exports = async (event) => {
    if (hasResponseData(event)) {
        const view = await createView({
            object: getResponseData(event),
            scope: event.scope,
            policy: event.policy
        })
        let body = JSON.stringify(view)
        logger.info('setting view', body)
        set(event,'response.body',body)
        return event
    } else {
        return event
    } 
}

const hasResponseData = event => {
    let contentType = get(event,['response','headers','Content-Type'])
    return contentType === 'application/json'
}

const getResponseData = event => {
    let data
    try {
        data = JSON.parse(event.response.body)
    } catch (error) {
        logger.error(error)
        data = {}
    }
    return data
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
