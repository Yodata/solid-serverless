/* eslint-disable no-undef */
const getHeaders = require('../get-headers')

describe('getHeaders', () => {
	test('raw-headers', () => {
		const httpMessage = {
			rawHeaders: {
				'Content-Type': [
					'application/json'
				]
			}
		}
		expect(getHeaders(httpMessage)).toHaveProperty('content-type', 'application/json')
	})

	test('handles response headers (with array values)', () => {
		const httpMessage = {
			headers: {
				'Content-Type': [
					'application/json'
				]
			}
		}
		expect(getHeaders(httpMessage)).toHaveProperty('content-type', 'application/json')
	})

	test('handles response headers (with string values)', () => {
		const httpMessage = {
			headers: {
				'Content-Type': 'application/json'
			}
		}
		expect(getHeaders(httpMessage)).toHaveProperty('content-type', 'application/json')
	})
})
