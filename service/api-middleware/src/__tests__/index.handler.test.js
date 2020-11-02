// @ts-nocheck

/* eslint-disable no-undef */
jest.mock('../process-request.js')
const { handler } = require('../index')

describe('index.handler', () => {
	let pr

	beforeEach(() => {
		pr = require('../process-request')
	})


	test('returns a Promise', async () => {
		expect(handler).toBeInstanceOf(Function)
		const event = require('../example/response')
		const response = handler(event, {})
		return expect(response).toBeInstanceOf(Promise)
	})

	test('calls process request', async () => {
		const event = require('../example/event')
		const id = Date.now()
		event.id = id
		expect.assertions(2)
		return handler(event).then(res => {
			expect(res).toHaveProperty('id', id)
			expect(pr).toHaveBeenCalledWith(event)
		})
	})

})
