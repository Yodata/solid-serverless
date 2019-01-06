/* eslint-disable no-undef */
const finalize = require('../finalize-event')

const createEvent = (phase, contentType, contentTypeValue, data) => ({
	[phase]: {
		headers: {
			[contentType]: contentTypeValue
		},
		body: JSON.stringify(data)
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
		expect(result.request).toHaveProperty('body',JSON.stringify(afterData))
	})
})