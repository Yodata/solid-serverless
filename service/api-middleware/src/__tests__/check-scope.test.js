
jest.mock('../lib/invoke-lambda-function.js')
const invokeLambdaFunction = require('../lib/invoke-lambda-function.js')
const checkScope = require('../check-scope')

describe('api-middleware.check-scope', () => {

	test('allowed example/response', async () => {
		const event = {
			stage: 'request',
			object: { type: 'dog' },
			isAllowed: false,
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

		return checkScope(event).then(() => {
			return expect(invokeLambdaFunction).toHaveBeenCalledTimes(1)
		})
	})
})
