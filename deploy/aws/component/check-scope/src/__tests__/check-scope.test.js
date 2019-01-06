/* eslint-disable no-undef */
const checkScope = require('../check-scope')

describe('check-scope', () => {
	test('example event/response', async () => {
		const event = require('../example/event.json')
		const response = require('../example/response.json')
		const result = await checkScope(event)
		return expect(result).toEqual(response)
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

})