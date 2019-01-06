const applyPolicy = require('../apply-policy')

describe('data-policy-apply-policy', () => {
	test('response', async () => {
		let event = {
			object: {
				password: 'test'
			},
			policy: {
				local: {
					RedactPassword: {
						processor: 'Yodata',
						effect: 'Transform',
						value: JSON.stringify({password: {value: '[PASSWORD]'}})
					}
				}
			}
		}
		const result = await applyPolicy(event)
		expect(result).toHaveProperty('object')
		expect(result).toHaveProperty('policy')
		expect(result.object).toHaveProperty('password','[PASSWORD]')
	})
})
