/* eslint-disable no-undef */
const hasData = require('../request-has-data')

const createRequest = (contentType, body = JSON.stringify({ type: 'test' })) => ({
	headers: {
		'content-type': contentType
	},
	body: body
})

test('json', () => {
	const request = createRequest('application/json')
	expect(hasData(request)).toBe(true)
})

test('json-ld', () => {
	const request = createRequest('application/ld+json')
	expect(hasData(request)).toBe(true)
})

test('turtle', () => {
	const request = createRequest('text/turtle')
	expect(hasData(request)).toBe(false)
})

test('yaml', () => {
	const request = createRequest('application/x-yaml')
	expect(hasData(request)).toBe(true)
})


test('no body = false', () => {
	const request = createRequest('application/x-yaml')
	delete request.body
	expect(hasData(request)).toBe(false)
})

test('empty body = false', () => {
	const request = createRequest('application/x-yaml')
	request.body = ''
	expect(hasData(request)).toBe(false)
})
