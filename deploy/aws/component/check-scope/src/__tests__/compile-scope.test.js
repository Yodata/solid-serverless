/* eslint-disable no-undef */
const compileScope = require('../compile-scope')
jest.mock('../lib/solid-client.js')

describe('get-policies unit tests', () => {

	test('returns an array of scopes', async () => {
		const scope = {
			inline: {
				effect: 'Allow',
				condition: {}
			},
			remote: 'http://deny'
		}
		return expect(compileScope({scope})).resolves.toEqual([
			{
				effect: 'Allow',
				condition: {}
			},
			{
				effect: 'Deny',
				condition: {}
			}
		])
	})

	test('fetches remote scopes', () => {
		expect.assertions(1)
		const scope = {
			remote: 'http://allow'
		}
		return expect(compileScope({scope})).resolves.toEqual([
			{
				effect: 'Allow',
				condition: {}
			}
		])
	})

	test('remote errors return {}', () => {
		expect.assertions(1)
		const scope = {a: 'http://error'}
		return expect(compileScope({scope})).resolves.toEqual([{}])
	})
})

