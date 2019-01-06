const validateSchema = require('../validate-schema')

describe('validate-schema-invoke', () => {
	test('response', async () => {
		const event = {
			object: {
				type: 'test'
			},
			schema: {
				properties: {
					type: {
						type: 'string',
						enum: ['test']
					}
				}
			}
		}
		const response = await validateSchema(event)
		expect(response).toHaveProperty('isValid', true)
		return expect(response).toHaveProperty('errors', null)
	})
})
