/* eslint-disable no-undef */

const checkScope = require('../check-scope')

describe('api-middleware.check-scope', () => {

	test('allowed example/response', async () => {
		const event = {
			stage: 'request',
			object: {type: 'dog'},
			scope: {
				noDogsAllowed: {
					effect: 'Deny',
					condition: {
						object: {
							type: 'dog'
						}
					}
				}
			}
		}
		const result = await checkScope(event)
		expect(result).toHaveProperty('isAllowed', false)
	})
})
