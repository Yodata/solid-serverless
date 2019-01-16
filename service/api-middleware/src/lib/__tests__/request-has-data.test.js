/* eslint-disable no-undef */
const hasData = require('../request-has-data')

const createRequest = (contentTypeKey, contentTypeValue) => ({
	headers: {
		[contentTypeKey]: contentTypeValue
	}
})

test('content-type', () => {
	const request = createRequest('content-type', 'application/json') 
	expect(hasData(request)).toBe(true)
})

test('Content-Type', () => {
	const request = createRequest('Content-Type', 'application/json') 
	expect(hasData(request)).toBe(true)
})

test('json-ld', () => {
	const request = createRequest('content-type', 'application/ld+json') 
	expect(hasData(request)).toBe(true)
})

test('ContentType', () => {
	const request = createRequest('Content-Type', 'text/turtle') 
	expect(hasData(request)).toBe(false)
})
