const addHeaders = require('../add-json-ld-context-header')

const DEFAULT_JSONLD_CONTEXT = 'https://dev.yodata.io/public/real-estate/context.json'
const OLD_ENV = process.env

beforeEach(() => {
	jest.resetModules()
	process.env = Object.assign({}, OLD_ENV)
	process.env.DEFAULT_JSONLD_CONTEXT = DEFAULT_JSONLD_CONTEXT
})
afterEach(() => {
	process.env = OLD_ENV
})
test('adds json-ld link header when response.Content-Type = application/json', async () => {
	let event = {
		response: {
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({type: 'test'})
		}
	}
	const expectedValue = `<${DEFAULT_JSONLD_CONTEXT}>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"`
	event = await addHeaders(event)
	expect(event).toHaveProperty('response')
	expect(event.response).toHaveProperty('headers')
	return expect(event.response.headers).toHaveProperty('Link', expectedValue)
})
