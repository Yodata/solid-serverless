const isWhiteListed = require('../data-policy-whitelist')

describe('data-policy-whitelist', () => {
	let stash, testenv

	beforeAll(() => {
		stash = {
			DATA_POLICY_SVC_HOST: process.env.DATA_POLICY_SVC_HOST,
			DATA_POLICY_WL: process.env.DATA_POLICY_WL,
			SOLID_HOST: process.env.SOLID_HOST
		}
		testenv = {
			DATA_POLICY_SVC_HOST: 'data.policy.svc.host',
			DATA_POLICY_WL: 'dps1.example.com,dps2.example.com',
			SOLID_HOST: 'solid.host'
		}

		Object.assign(process.env, testenv)
	})

	afterAll(() => {
		Object.assign(process.env, stash)
	})

	test('solid.host is whitelisted', () => {
		const agent = `https://${testenv.SOLID_HOST}/profile.card#me`
		return expect(isWhiteListed({ agent })).toBeTruthy()
	})

	test('data.policy.svc.host is whitelisted', () => {
		const agent = `https://${testenv.DATA_POLICY_SVC_HOST}/profile/card#me`
		return expect(isWhiteListed({ agent })).toBeTruthy()
	})

	test('data.policy.wl is whitelisted', () => {
		const host = String(testenv.DATA_POLICY_WL).split(',')[ 1 ]
		const agent = `https://${host}/profile/card#me`
		return expect(isWhiteListed({ agent })).toBeTruthy()
	})

	test('no agent returns false', () => {
		return expect(isWhiteListed({})).toBe(false)
	})

	test('child of solid.host returns false', () => {
		const agent = `https://child.${testenv.SOLID_HOST}/profile.card#me`
		return expect(isWhiteListed({ agent })).toBe(false)
	})

	test('parent of solid.host returns false', () => {
		const agent = 'https://host/profile.card#me'
		return expect(isWhiteListed({ agent })).toBe(false)
	})



})