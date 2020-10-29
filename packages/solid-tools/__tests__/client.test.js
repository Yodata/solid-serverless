describe('@yodata/solid-tools/client', () => {
	const client = require('../lib/client')
	test('constructor', () => {
		expect(client).toBeInstanceOf(Function)
		expect(client('testuser')).toBeInstanceOf(Function)
	})

	test('get.json does not throw', async () => {
		let getClient = require('../lib/client')
		let c = getClient('xxx')

		return await expect(c.get('https://dave.bhhs.dev.yodata.io/public/test/RemotePolicy.json', {json:true})).resolves.toHaveProperty('effect')
	})
})
