const handler = require('..').handler
const event = require('../example/event.json')
const response = require('../example/response.json')

// test('verifies successful response', async () => {
// 	const result = await handler(event)
// 	return expect(result).toMatchObject(response)
// })

test('remote schema', async () => {
	const object = {
		topic: 'realestate/thisbetternotwork'
	}
	const schema = 'https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.transactionreport.yaml'
	const event = { object, schema }
	const result = await handler(event)
	return expect(result).toHaveProperty('isValid', false)
})
