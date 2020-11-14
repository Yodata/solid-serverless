/* eslint-disable no-undef */

const { EventBridge } = require('aws-sdk')
const normalizeEvent = require('../normalize-event')
let event

beforeEach(() => {
	event = require('../example/event')
})

const createRequest = (contentType, data) => {
	return {
		headers: {
			'content-type': contentType
		},
		body: Buffer.from(JSON.stringify(data)).toString('base64'),
		isBase64Encoded: true
	}
}

test('normalize-event rejects missing request', async () => {
	expect.assertions(1)
	// @ts-ignore
	return expect(() => normalizeEvent({})).rejects.toThrow('normalize-event:error:missing-request')
})

test('normalize-event rejects undefined event', async () => {
	expect.assertions(1)
	// @ts-ignore
	return expect(() => normalizeEvent()).rejects.toThrow('normalize-event:error:event-undefined')
})

test('request-event', async () => {
	expect.assertions(4)
	const eventData = { 'foo': 'bar' }
	const event = {
		request: createRequest('application/json', eventData)
	}
	// @ts-ignore
	const result = await normalizeEvent(event)
	expect(result).toHaveProperty('stage', 'request')
	expect(result).toHaveProperty('hasData', true)
	expect(result).toHaveProperty('contentType', 'application/json')
	return expect(result).toHaveProperty('object', eventData)
})

test('response-event', async () => {
	const eventData = { 'foo': 'bar' }
	const event = {
		request: createRequest('application/json', eventData),
		response: createRequest('application/json', eventData)
	}
	// @ts-ignore
	const result = await normalizeEvent(event)
	expect(result).toHaveProperty('stage', 'response')
	expect(result).toHaveProperty('hasData', true)
	expect(result).toHaveProperty('contentType', 'application/json')
	return expect(result).toHaveProperty('object', eventData)
})

test('adds event.agent when request.solidService is true', () => {
	delete event.agent
	event.request.solidService = true
	// @ts-ignore
	return expect(normalizeEvent(event)).resolves.toHaveProperty('agent', expect.stringContaining('service-'))
})

test('does not overwrite event.agent when request.solidService is true', () => {
	const agent = 'https://example.com/profile/card#me'
	event.agent = agent
	event.request.solidService = true
	// @ts-ignore
	return expect(normalizeEvent(event)).resolves.toHaveProperty('agent', agent)
})