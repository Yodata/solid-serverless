const validateSchema = require('../validate-schema')

describe('validate-schema-invoke', () => {    
    test('response', async () => {
        let event = {
            object: {
                type: 'test'
            },
            schema: {
                properties: {
                    type: {
                        type: "string",
                        enum: ["test"]
                    }
                }
            }
        }
        let response = await validateSchema(event)
        expect(response).toHaveProperty('isValid', true)
        return expect(response).toHaveProperty('errors', null)
    }) 
})