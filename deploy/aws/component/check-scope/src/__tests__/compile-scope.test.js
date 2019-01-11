/* eslint-disable no-undef */
const compileScope = require('../compile-scope')
jest.mock('../lib/solid-client.js')

describe('compile-scope', () => {

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
		return expect(compileScope({scope})).resolves.toEqual([{
			id: 'http://error',
			error: {
				name: 'FETCH_REMOTE_SCOPE_ERROR',
				message: 'http://error'
			},
			effect: 'Deny',
			condition: {}
		}])
	})
})

