
describe('@yodata/solid-tools/client', () => {
	const getClient = require('../lib/client')
	const client = require('../lib/client')
	test('constructor', () => {
		expect(client).toBeInstanceOf(Function)
		expect(client('testuser')).toBeInstanceOf(Function)
	})

	test('get.json does not throw', async () => {
		let uri = 'https://dave.bhhs.dev.yodata.io/public/test-data-policy.json'
		let c = getClient('dave_admin')

		let data = await c(uri, { json: true })
			.then(res => (res.body))

		return expect(data).toHaveProperty('type', 'test')
	})

})
