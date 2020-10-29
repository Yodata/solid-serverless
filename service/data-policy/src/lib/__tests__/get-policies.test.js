const getPolicies = require('../get-policies')

describe('get-policies unit tests', () => {
	let event; let object; let policy; let PolicyRef

	beforeEach(() => {
		// InlinePolicy = {
		// 	processor: 'Yodata',
		// 	effect: 'Transform',
		// 	value: JSON.stringify({password: '$redact'})
		// }
		PolicyRef = {
			RemotePolicy: 'https://dave.bhhs.dev.yodata.io/public/test/RemotePolicy.json'
		}
		policy = {}
		object = {
			type: 'Forbidden',
			password: 'secret',
			deleteme: 'now you see me'
		}
		event = {object, policy}
	})

	test('returns an array of policies', async () => {
		const local = {LocalPolicyName: {type: 'LocalPolicy'}}
		const global = {GlobalPolicyName: {type: 'GlobalPolicy'}}
		event.policy = {local, global}
		const arrayOfPolicies = [{type: 'LocalPolicy'}, {type: 'GlobalPolicy'}]
		const response = await getPolicies(event)
		return expect(response).toEqual(arrayOfPolicies)
	})

	test('fetches remote policies', async () => {
		const local = PolicyRef
		event.policy = {local}
		const response = await getPolicies(event)
		expect(response).toBeInstanceOf(Array)
		expect(response.length).toEqual(1)
		return expect(response[0]).toHaveProperty('type', 'DataPolicy')
	})
})
