/* eslint-disable no-undef */

const processRequest = require('../process-request')

describe('process-request', () => {
	test('returns a promise', () => {
		return expect(processRequest({})).toBeInstanceOf(Promise)
	})

	test('example request/response is valid', async () => {
		const event = require('../example/process-request-event.json')
		const expectedResponse = Object.assign(event, {foo: 'bar'})
		const mockMiddlewareFunction = jest.fn(event => {
			return Object.assign(event, {foo: 'bar'})
		})
		const middlewares = [mockMiddlewareFunction]
		const response = await processRequest(event, middlewares)
		return expect(response).toEqual(expectedResponse)
	})

	test('throws on middleware error', async () => {
		const event = require('../example/process-request-event.json')
		const expectedResponse = Object.assign(event, {foo: 'bar'})
		const error = new Error('test')
		const mockMiddlewareFunction = jest.fn().mockRejectedValue(error)
		const middlewares = [mockMiddlewareFunction]
		return expect(processRequest(event, middlewares)).rejects.toEqual(error)
	})
})
