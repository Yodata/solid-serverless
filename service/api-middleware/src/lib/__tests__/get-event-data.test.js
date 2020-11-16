/* eslint-disable no-undef */
const getEventData = require('../get-event-data')


const createEvent = (phase, contentType, contentTypeValue, data) => {
	const body = Buffer.from(JSON.stringify(data)).toString('base64')
	return ({
		[phase]: {
			headers: {
				[contentType]: contentTypeValue
			},
			body,
			isBase64Encoded: true
		}
	})
}

describe('get-event-data', () => {

	test('request-data', () => {
		const data = {type:'request-data'}
		const event = createEvent('request','content-type','application/json',data)
		expect(getEventData(event)).toEqual(data)
	})

	test('response-data', () => {
		const data = {type:'response-data'}
		const event = createEvent('response','Content-Type','application/json',data)
		expect(getEventData(event)).toEqual(data)
	})

	test('ld+json', () => {
		const data = {type:'jsonld-data'}
		const event = createEvent('request','CONTENT-TYPE','application/ld+json',data)
		expect(getEventData(event)).toEqual(data)
	})

	test('unencoded response.body', () => {
		const data = {type:'response-data'}
		const event = createEvent('response','Content-Type','application/json',data)
		event.response.body = JSON.stringify(data)
		event.response.isBase64Encoded = false
		expect(getEventData(event)).toEqual(data)
	})
})
