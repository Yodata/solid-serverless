/* eslint-disable no-undef */
const checkScope = require('../check-scope')

describe('check-scope', () => {

	test('example event/response', () => {
		const event = require('../example/event.json')
		const response = require('../example/response.json')
		return expect(checkScope(event)).resolves.toEqual(response)
	})

	test('middleware params', async () => {
		const middlewareEvent = {
			object: {
				stage: 'request',
				object: {
					type: 'dog'
				}
			},
			scope: {
				noDogsAllowed: {
					'processor': 'Mingo',
					'effect': 'Deny',
					'condition': {
						object: {
							type: 'dog'
						}
					}}
			}
		}
		const result = await checkScope(middlewareEvent)
		return expect(result).toHaveProperty('isAllowed', false)
	})

	test('checkScope', async () => {
		let event = {
			object: {
				object: {
					type: 'ForbiddenType',
					comment: 'Expect result.isAllowed to be false'
				}
			},
			scope: {
				PublicDataOnly: {
					effect: 'Deny',
					processor: 'Mingo',
					condition: {
						object: {
							type: 'ForbiddenType'
						}
					}
				}
			}
		}
		let result = await checkScope(event)
		return expect(result).toHaveProperty('isAllowed', false)
	})

})