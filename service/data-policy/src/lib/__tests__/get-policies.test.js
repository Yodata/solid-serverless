
const getPolicies = require('../get-policies')
const nock = require('nock')

describe('get-policies', () => {
	let event; let object; let policy

	beforeEach(() => {
		// InlinePolicy = {
		// 	processor: 'Yodata',
		// 	effect: 'Transform',
		// 	value: JSON.stringify({password: '$redact'})
		// }
		// PolicyRef = {
		// 	RemotePolicy: 'https://dave.bhhs.dev.yodata.io/public/test/RemotePolicy.json'
		// }
		policy = {}
		object = {
			type: 'Forbidden',
			password: 'secret',
			deleteme: 'now you see me'
		}
		event = { object, policy }
	})

	test('returns.arrayOfPolicies', async () => {
		const local = { LocalPolicyName: { type: 'LocalPolicy' } }
		const global = { GlobalPolicyName: { type: 'GlobalPolicy' } }
		event.policy = { local, global }
		const arrayOfPolicies = [ { type: 'LocalPolicy' }, { type: 'GlobalPolicy' } ]
		const response = await getPolicies(event)
		return expect(response).toEqual(arrayOfPolicies)
	})

	test('fetches remote policies', async () => {
		const remoteid = 'https://example.com/datapolicy'
		event.policy = {
			local: {
				remoteid
			}
		}
		const scope = nock('https://example.com')
			.get('/datapolicy')
			.reply(200, {
				type: 'DataPolicy'
			})
		const response = await getPolicies(event)
		expect(response).toBeInstanceOf(Array)
		expect(response.length).toEqual(1)
		scope.done()
		return expect(response[ 0 ]).toHaveProperty('type', 'DataPolicy')
	})

	test('does not fetch id uris', async () => {
		const host = 'http://example.com'
		const patha = '/a'
		const pathb = '/b'
		const scope = nock(host)
			.get(patha).reply(200, { type: 'a' })
			.get(pathb).reply(200, { type: 'b' })
		event.policy = {
			local: {
				id: host + patha,
				'@id': host + pathb
			},
			remote: {
				'@id': host + patha
			}
		}
		expect.assertions(3)
		const response = await getPolicies(event)
		expect(response).toBeInstanceOf(Array)
		expect(response.length).toEqual(0)
		expect(scope.isDone()).toBeFalsy()
	})
})
