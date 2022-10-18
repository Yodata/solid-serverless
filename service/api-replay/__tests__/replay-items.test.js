const SOLID_HOST = 'http://example.com'
const SVC_KEY = 'XXX'
const nock = require('nock')
let NODE_ENV

beforeAll(() => {
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
	const event = {
		target: `${SOLID_HOST}/inbox/`,
		items: [
			'1',
			'2',
			'3'
		]
	}
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
