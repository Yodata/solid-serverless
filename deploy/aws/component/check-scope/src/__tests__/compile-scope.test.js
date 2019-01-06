/* eslint-disable no-undef */
const compileScope = require('../compile-scope')

describe('get-policies unit tests', () => {
	let event; let object; let scope; let ScopeRef

	beforeEach(() => {
		ScopeRef = {
			RemoteScope: 'https://dev.yodata.io/public/test/RemoteScope.json'
		}
		scope = {
			catsOnly: {
				effect: 'Allow',
				condition: {
					object: {
						type: 'cat'
					}
				}
			}
		}
		object = {
			type: 'dog'
		}
		event = {object, scope}
	})

	test('returns an array of scopes', async () => {
		const result = await compileScope(event)
		expect(result).toBeInstanceOf(Array)
		return expect(result.length).toEqual(1)
	})

	test('fetches remote scopes', async () => {
		event.scope = ScopeRef
		const response = await compileScope(event)
		expect(response).toBeInstanceOf(Array)
		expect(response.length).toEqual(1)
		return expect(response[0]).toEqual({
			'effect': 'Allow',
			'processor': 'Mingo',
			'condition': {
				'object': {
					'type': 'cat'
				}
			}
		})
	})
})

