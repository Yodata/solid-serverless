
const { handler: validate } = require('../index')
const event = require('../example/event.json')
const response = require('../example/response.json')
const transactionreport = require('../example/transactionreport')
const events = {
	'realestate/franchise#transactionreport': transactionreport
}

const schemas = {
	'realestate/franchise#transactionreport': 'https://validate-schema.bhhs.hsfaffiliates.com/public/schema/realestate/franchise/transactionreport.json'
}

test('verifies successful response', async () => {
	const result = await validate(event)
	return expect(result).toMatchObject(response)
})

test('remote schema', async () => {
	const topic = 'realestate/franchise#transactionreport'
	const schema = schemas[topic]
	const object = events[topic]
	const event = { object, schema }
	const result = await validate(event)
	return expect(result).toHaveProperty('isValid', false)
})

test('transaction.report ', async () => {
	const topic = 'realestate/franchise#transactionreport'
	const schema = schemas[topic]
	const result = await validate({ object: transactionreport, schema })
	return expect(result).toHaveProperty('isValid', false)
})
