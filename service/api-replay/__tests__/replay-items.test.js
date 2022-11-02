const SOLID_HOST = 'http://example.com'
const SVC_KEY = 'XXX'
const { getEvent } = require('../src/queues/replay-items/__mocks__/event.js')
const nock = require('nock')
let NODE_ENV

beforeAll(async () => {
	NODE_ENV = process.env
	process.env.SOLID_HOST = SOLID_HOST
	process.env.SVC_KEY = SVC_KEY
	console.log({ SOLID_HOST, SVC_KEY })
})

afterAll(() => {
	process.env = NODE_ENV
})

test('handler returns an array', async () => {
	const handleEvent = require('../src/queues/replay-items/main')
	expect.assertions(6)
	const event = getEvent({
		target: `${SOLID_HOST}/inbox/`,
		items: [
			'1',
			'2',
			'3'
		]
	})
	const scope = nock(SOLID_HOST)
		.get('/inbox/1').reply(200, { id: 1 })
		.get('/inbox/2').reply(200, { id: 2 })
		.get('/inbox/3').reply(200, { id: 3 })
		.put('/inbox/1').reply(204)
		.put('/inbox/2').reply(204)
		.put('/inbox/3').reply(204)

	const response = await handleEvent(event)
	expect(scope.isDone).toBeTruthy()
	expect(response).toBeInstanceOf(Array)
	expect(response.length).toEqual(3)
	expect(response[0]).toEqual('1:204')
	expect(response[1]).toEqual('2:204')
	expect(response[2]).toEqual('3:204')
	return scope.isDone()
})

test('throws an error on read errors', async () => {
	const handleEvent = require('../src/queues/replay-items/index.js').test
	expect.assertions(1)
	const scope = nock(SOLID_HOST)
		.get('/inbox/found').reply(200, { id: 1 })
		.put('/inbox/found', { id: 1 }).reply(204)
		.get('/inbox/missing').reply(404, { error: 'the resource does not exist' })
	const event = getEvent({
		target: `${SOLID_HOST}/inbox/`,
		items: ['found', 'missing'],
		options: {
			STOP_REPLAY_ON_ERROR: true
		}
	})

	expect(handleEvent(event)).rejects.toHaveProperty('actionStatus', 'FailedActionStatus')
	return scope.isDone()
})
