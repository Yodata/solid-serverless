/* eslint-disable no-undef */
const getHeader = require('../get-header-value')

describe('get-header-value', () => {
	
	test('raw-headers', () => {
		const httpMessage = {
			rawHeaders: {
				'Content-Type': [
					'application/json'
				]
			}
		}
		expect(getHeader(httpMessage,'content-type')).toEqual('application/json')
	})

	test('handles response headers (with array values)', () => {
		const httpMessage = {
			headers: {
				'Content-Type': [
					'application/json'
				]
			}
		}
		expect(getHeader(httpMessage,'content-type')).toEqual('application/json')
	})

	test('handles response headers (with string values)', () => {
		const httpMessage = {
			headers: {
				'Content-Type': 'application/json'
			}
		}
		expect(getHeader(httpMessage,'content-type')).toEqual('application/json')
	})

})
