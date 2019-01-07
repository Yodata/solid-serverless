/* eslint-disable no-undef */
const finalize = require('../finalize-event')

const encode = (data) => new Buffer(JSON.stringify(data)).toString('base64')

const createEvent = (phase, contentType, contentTypeValue, data) => ({
	[phase]: {
		headers: {
			[contentType]: contentTypeValue
		},
		body: encode(data)
	}
})


describe('finalize-event', () => {
	test('finalize.request.body', () => {
		const beforeData = {type:'before'}
		const afterData = {type:'after'}
		const event = createEvent('request','content-type','application/json', beforeData)
		event.hasData = true
		event.object = afterData
		const result = finalize(event)
		expect(result).toHaveProperty('request')
		expect(result.request).toHaveProperty('body',encode(afterData))
	})
})