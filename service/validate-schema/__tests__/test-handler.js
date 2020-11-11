
const handler = require('../src').handler
const event = require('../example/event.json')
const response = require('../example/response.json')
const transactionreport = require('../example/transactionreport')
const events = {
	'realestate/franchise#transactionreport': transactionreport
}

// const topics = {
// 	'realestate/franchise#transactionreport': 'realestate/franchise#transactionreport'
// }

const schemas = {
	'realestate/franchise#transactionreport': 'https://raw.githubusercontent.com/Yodata/real-estate/master/www/schemas/franchise/franchise.transactionreport.yaml'
}

test('verifies successful response', async () => {
	const result = await handler(event)
	return expect(result).toMatchObject(response)
})

test('remote schema', async () => {
	let topic = 'realestate/franchise#transactionreport'
	let schema = schemas[ topic ]
	let object = events[ topic ]
	const event = { object, schema }
	const result = await handler(event)
	return expect(result).toHaveProperty('isValid', false)
})

test('transaction.report ', async () => {
	let topic = 'realestate/franchise#transactionreport'
	let schema = schemas[ topic ]
	const result = await handler({ object: transactionreport, schema })
	return expect(result).toHaveProperty('isValid', false)
})
