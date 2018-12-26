const applyPolicy = require('../apply-policy')
const stringify = require('fast-json-stable-stringify')

describe('data-policy-apply-policy', ()=>{
    test('response', async () => {
        let event = {
            object: {
                password: 'test'
            },
            policy: {
                local: {
                    "RedactPassword": {
                        "processor": "Yodata",
                        "effect": "Transform",
                        "value": stringify({password: '$redact'})
                    }
                }
            }
        }
        let response = await applyPolicy(event)
        return expect(response.object).toMatchObject({password: '**REDACTED**'})
    }) 
})