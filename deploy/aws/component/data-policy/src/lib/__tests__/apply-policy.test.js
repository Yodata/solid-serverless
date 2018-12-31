const applyPolicy = require('../apply-policy')

describe('data-policy-apply-policy', () => {    
    test('response', async () => {
        let event = {
            object: {
                password: 'test'
            },
            policy: {
                local: {
                    'RedactPassword': {
                        'processor': 'Yodata',
                        'effect': 'Transform',
                        'value': JSON.stringify({password: {value: '[PASSWORD]'}})
                    }
                }
            }
        }
        let response = await applyPolicy(event)
        return expect(response.object).toMatchObject({password: '[PASSWORD]'})
    }) 
})