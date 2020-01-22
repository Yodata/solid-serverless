/* eslint-disable no-undef */
const finalize = require('../finalize-event')

const encode = (data) => Buffer.from(JSON.stringify(data)).toString('base64')

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
		const next = finalize(event)
		expect(next).toHaveProperty('request')
		expect(next.request).toHaveProperty('body',encode(afterData))
		expect(next.request).toHaveProperty('isBase64Encoded', true)
	})
})