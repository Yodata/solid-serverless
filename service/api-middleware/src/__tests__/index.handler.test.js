/* eslint-disable no-undef */
const handler = require('..').handler
const getData = require('../lib/get-event-data')

describe('api-middleware', () => {

	test('returns a Promise', () => {
		expect(handler).toBeInstanceOf(Function)
		const event = require('../example/response')
		const response = handler(event, {})
		return expect(response).toBeInstanceOf(Promise)
	})

	test('example event/response', () => {
		const event = require('../example/event')
		const response = require('../example/response')
		expect(handler(event, {})).resolves.toEqual(response)
	})

	test('parses uri object keys', async () => {
		const event = require('../example/test-event')
		const data = getData(event)
		const response = await handler(event, {})
		return expect(response.object).toEqual(data)
	})

	test('get request with content-type header', async () => {
		const event = module.exports = {
			"request": {
				"method": "GET",
				"headers": {
					"Content-Type": [
						"application/json"
					]
				},
				"body": ""
			},
			"scope": [],
			"policy": []
		}
		const result = await handler(event, {})
		console.log({ result })
		return expect(result).toHaveProperty('hasData', false)
	})
})

