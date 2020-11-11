/* eslint-disable no-undef */

jest.mock('../lib/invoke-lambda-function')

describe('api-middleware.apply-policy', () => {
	const { applyDataPolicy, hasData, hasPolicy, isDataPolicyRequest, isJSON, isWhiteListed, isProfileReadEvent } = require('../apply-policy')
	let event, invokeLambdaFunction

	beforeEach(() => {
		event = require('../example/apply-policy-event')
		invokeLambdaFunction = require('../lib/invoke-lambda-function')
	})

	test('example event', () => {
		expect.assertions(5)
		expect(isJSON(event)).toBeTruthy()
		expect(hasData(event)).toBeTruthy()
		expect(hasPolicy(event)).toBeTruthy()
		expect(isDataPolicyRequest(event)).toBeFalsy()
		return expect(isWhiteListed(event)).toBeFalsy()
	})
	test('whitelisting', () => {
		process.env.DATA_POLICY_WL = 'profile-'
		event.agent = 'https://profile-processor.example.com/profile/card#me'
		return expect(isWhiteListed(event)).toBeTruthy()
	})

	test('calls invoke-lambda-function', async () => {
		expect.assertions(7)
		event.agent = null
		expect(isProfileReadEvent(event)).toBeTruthy()
		expect(isJSON(event)).toBeTruthy()
		expect(hasData(event)).toBeTruthy()
		expect(hasPolicy(event)).toBeTruthy()
		expect(isDataPolicyRequest(event)).toBeFalsy()
		expect(isWhiteListed(event)).toBeFalsy()
		return applyDataPolicy(event).then(() => {
			return expect(invokeLambdaFunction).toHaveBeenCalledTimes(1)
		})
	})

	test('does not invoke when reading/writing to the data policy path', () => {
		jest.resetAllMocks()
		expect.assertions(1)
		event.request.target.path = '/public/yodata/data-policy.json'
		return applyDataPolicy(event).then(() => {
			return expect(invokeLambdaFunction).toHaveBeenCalledTimes(0)
		})
	})

	test('does not invoke when the agent is the data policy service', () => {
		expect.assertions(1)
		jest.resetAllMocks()
		const DATA_POLICY_SVC_HOST = 'https://example.com/profile/card#me'
		process.env[ 'DATA_POLICY_SVC_HOST' ] = DATA_POLICY_SVC_HOST
		event.agent = DATA_POLICY_SVC_HOST
		event.request.target.path = '/public/yodata/data-policy.json'
		return applyDataPolicy(event).then(() => {
			return expect(invokeLambdaFunction).toHaveBeenCalledTimes(0)
		})
	})

	test('does not invoke if there is no policy', () => {
		jest.resetAllMocks()
		expect.assertions(1)
		delete event.policy
		return applyDataPolicy(event).then(() => {
			return expect(invokeLambdaFunction).toHaveBeenCalledTimes(0)
		})
	})

	test('does not invoke if policy is an empty object', () => {
		jest.resetAllMocks()
		expect.assertions(1)
		event.policy = {}
		return applyDataPolicy(event).then(() => {
			return expect(invokeLambdaFunction).toHaveBeenCalledTimes(0)
		})
	})

	test('does not invoke if content-type is not application/json or appliation/ld+json', () => {
		jest.resetAllMocks()
		expect.assertions(1)
		event.contentType = 'application/x-yaml'
		return applyDataPolicy(event).then(() => {
			return expect(invokeLambdaFunction).toHaveBeenCalledTimes(0)
		})
	})

	test('invokes on json-ld application/ld+json', async () => {
		jest.resetAllMocks()
		const event = require('../example/apply-policy-event')
		const DATA_POLICY_SVC_HOST = 'jsonld.test.example.com'
		const DATA_POLICY_PATH = '/public/yodata/data-policy.json'
		const SOLID_HOST = 'bhhs.dev.yodata.io'

		Object.assign(process.env, {
			'DATA_POLICY_SVC_HOST': DATA_POLICY_SVC_HOST,
			'DATA_POLICY_PATH': DATA_POLICY_PATH,
			'SOLID_HOST': SOLID_HOST
		})

		expect.assertions(7)

		// content type is json
		event.contentType = 'application/ld+json'
		expect(isJSON(event)).toBeTruthy()

		// not a data policy
		event.request.target.path = '/profile/card'
		process.env.DATA_POLICY_SVC_HOST = DATA_POLICY_SVC_HOST
		expect(event.request.target.path).not.toBe(DATA_POLICY_PATH)
		expect(isDataPolicyRequest(event)).toBeFalsy()

		// has data
		event.hasData = true
		expect(hasData(event)).toBeTruthy()

		// has policies
		event.policy = {
			'local': {
				'removegolivedate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"goLiveDate":{"@remove":true}}'
				},
				'removeoriginalaffiliationdate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"originalAffiliationDate":{"@remove":true}}'
				},
				'@id': 'https://dave.bhhs.dev.yodata.io/public/yodata/data-policy.json',
				'id': 'https://dave.bhhs.dev.yodata.io/public/yodata/data-policy.json'
			},
			'global': {
				'removegolivedate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"goLiveDate":{"@redact":true}}'
				},
				'removeoriginalaffiliationdate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"originalAffiliationDate":{"@remove":true}}'
				}
			}
		}
		expect(hasPolicy(event)).toBeTruthy()
		expect(event.response.headers).toHaveProperty('content-type', 'application/ld+json')

		applyDataPolicy(event)
		return expect(invokeLambdaFunction).toHaveBeenCalledWith('apply-policy', event)
	})

})
