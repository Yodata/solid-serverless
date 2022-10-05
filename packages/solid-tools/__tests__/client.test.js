
describe('@yodata/solid-tools/client', () => {
	const getClient = require('../lib/client')
	const client = require('../lib/client')
	test('constructor', () => {
		expect(client).toBeInstanceOf(Function)
		expect(client('testuser')).toBeInstanceOf(Function)
	})

	test('get.json does not throw', async () => {
		const uri = 'https://dave.bhhs.dev.yodata.io/public/test-data-policy.json'
		const c = getClient(process.env.SOLID_KEY)

		const data = await c(uri, { json: true })
			.then(res => (res.body))

		return expect(data).toHaveProperty('type', 'test')
	})
})
