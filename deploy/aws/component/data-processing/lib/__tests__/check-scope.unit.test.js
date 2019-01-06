const LambdaTester = require('lambda-tester')
const checkScope = require('../check-scope')

describe('check-scope', () => {
	test('exports handler function', () => {
		expect(checkScope).toHaveProperty('handler')
		expect(checkScope.handler).toBeInstanceOf(Function)
	})

	test('handler', () => {
		const handler = checkScope.handler
		const event = {
			object: {type: 'cat'},
			scope: {
				processor: 'Mingo',
				effect: 'Allow',
				condition: {type: 'cat'}
			}
		}
		return expect(handler(event)).resolves.toMatchObject({
			scopeIsAllowed: true
		})
	})
})
