/* eslint-disable no-undef */
const applyPolicy = require('../apply-policy')

const redactPasswordPolicy = {
	local: {
		RedactPassword: {
			type: 'Transform',
			processor: 'Yodata',
			value: {
				password: {
					value: 'redacted'
				}
			}
		}
	}
}


describe('api-middleware.apply-policy', () => {
	test('response-data', async () => {
		let data = {password: 'secret'}
		let event = {
			stage: 'response',
			hasData: true,
			object: data,
			policy: redactPasswordPolicy
		}
		const result = await applyPolicy(event)
		expect(result).toHaveProperty('object')
		expect(result.object).toHaveProperty('password','redacted')
	})

	test('request-data', async () => {
		let data = {password: 'secret'}
		let event = {
			stage: 'request',
			hasData: true,
			object: data,
			policy: redactPasswordPolicy
		}
		const result = await applyPolicy(event)
		expect(result).toHaveProperty('object')
		expect(result.object).toHaveProperty('password','redacted')
	})



	test('empty policy returns event', async () => {
		let event = require('../example/event.json')
		let result
		// empty policy
		event.policy = {}
		result = await applyPolicy(event)
		expect(result).toEqual(event)
		// undefined policy
		event.policy = undefined
		result = await applyPolicy(event)
		expect(result).toEqual(event)
		// no policy
		delete event.policy
		result = await applyPolicy(event)
		return expect(result).toEqual(event)
	})
})
