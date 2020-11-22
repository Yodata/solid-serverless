
const { allClear, checkEvent, hasAgent, hasPolicy, policyRequired } = require('../check-event')
const { createRequest, policy } = require('../example')

const createEvent = (method, path, object) => {
	const request = createRequest(method, path)
	return {
		agent: request.security.agent,
		request,
		object: object,
		policy
	}
}


describe('check-event', () => {
	let postevent, message, stash, testenv

	beforeEach(() => {
		postevent = createEvent()
		message = 'foo'

	})


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


	describe('check-event:response', () => {
		test('returns a valid check-event response', () => {
			expect(allClear(message, postevent)).toMatchObject({
				object: postevent.id,
				result: {
					message: message,
					policyExecutionRequired: false
				}
			})
		})

		test('policy.required.response', () => {
			expect(policyRequired(message, postevent)).toMatchObject({
				object: postevent.id,
				result: {
					message: message,
					policyExecutionRequired: true
				}
			})
		})

	})


	describe('hasAgent', () => {

		test('returns false if no event.agent', () => {
			postevent = createEvent('POST', '/outbox/')
			expect.assertions(2)
			delete postevent.agent
			expect(postevent).not.toHaveProperty('agent')
			return expect(hasAgent(postevent)).toBeFalsy()
		})

		test('false if event.agent is null', () => {
			postevent = createEvent('POST', '/outbox/')
			expect.assertions(2)
			postevent.agent = null
			expect(postevent).toHaveProperty('agent', null)
			return expect(hasAgent(postevent)).toBeFalsy()
		})


	})

	describe('hasPolicy', () => {

		test('policies exist = true', () => {
			postevent = createEvent()
			expect(postevent).toHaveProperty('policy')
			expect(hasPolicy(postevent)).toBeTruthy()
		})

		test('policies do not exist = true', () => {
			postevent = createEvent()
			postevent.policy = undefined
			expect(postevent).toHaveProperty('policy', undefined)
			delete postevent.policy
			expect(hasPolicy(postevent)).toBeFalsy()
		})

	})


	describe('checkEvent', () => {

		test('schema', () => {
			postevent = createEvent()
			const result = checkEvent(postevent)
			expect(result).toHaveProperty('object', postevent.id)
			expect(result).toHaveProperty('result.policyExecutionRequired', true)
		})

		test('no policy returns policyExecutionRequired: false', () => {
			postevent = createEvent()
			delete postevent.policy
			const result = checkEvent(postevent)
			expect(result).toHaveProperty('object', postevent.id)
			expect(result).toHaveProperty('result.policyExecutionRequired', false)
			expect(result).toHaveProperty('result.message', expect.stringContaining('policy'))
		})

		test('policy should execute if there is no agent', () => {
			postevent = createEvent()
			delete postevent.agent
			const result = checkEvent(postevent)
			expect(result).toHaveProperty('object', postevent.id)
			expect(result).toHaveProperty('result.policyExecutionRequired', true)
			expect(result).toHaveProperty('result.message', expect.stringContaining('agent'))
		})

		test('white-listed agent should return policyExecutionRequired false', () => {
			postevent = createEvent()
			//DATA_POLICY_WL = dps1.example.com,dps2.example.com
			const whitelistedhost = 'whitelistedhost.example.com'
			process.env.DATA_POLICY_WL = whitelistedhost
			postevent.agent = `https://${whitelistedhost}`
			const result = checkEvent(postevent)
			expect(result).toHaveProperty('object', postevent.id)
			expect(result).toHaveProperty('result.policyExecutionRequired', false)
			expect(result).toHaveProperty('result.message', expect.stringContaining('white'))
		})

		test('data policy execute on profile reads', () => {
			postevent = createEvent('GET', '/profile/card')
			expect(postevent.request.target).toHaveProperty('path', '/profile/card')
			expect(postevent.request.target).toHaveProperty('accessType', 'Read')
			const result = checkEvent(postevent)
			expect(result).toHaveProperty('object', postevent.id)
			expect(result).toHaveProperty('result.policyExecutionRequired', true)
			expect(result).toHaveProperty('result.message', expect.stringContaining('profile-read'))
		})

		test('data policy execute on outbox post or put', () => {
			postevent = createEvent('POST', '/outbox/')
			expect(postevent.request.target).toHaveProperty('path', '/outbox/')
			expect(postevent.request.target).toHaveProperty('accessType', 'Append')
			const result = checkEvent(postevent)
			expect(result).toHaveProperty('object', postevent.id)
			expect(result).toHaveProperty('result.policyExecutionRequired', true)
			expect(result).toHaveProperty('result.message', expect.stringContaining('outbox:append'))
		})

	})

})