const OLD_ENV = process.env
const TEST_ENV = {
	SOLID_STORE: 'yodata-dev-solid-serverless-storage',
	SOLID_HOST: 'example.com'
}
beforeAll(function () {
	Object.assign(process.env, TEST_ENV)
})
afterAll(() => {
	Object.assign(process.env, OLD_ENV)
})
const validateReplayInput = require('../validate-replay-input')
const exampleInput = require('../example-input')
const testInput = exampleInput
const getTestInput = overrides => {
	return {
		...testInput,
		...overrides
	}
}

test('returns true when input is valid', () => {
	const input = getTestInput()
	expect(validateReplayInput(input)).resolves().toBeTruthy()
})

test('throws an error when input is invalid', () => {
	const input = getTestInput({ startDate: '2022-09-29T22:59:53.288' })
	expect(() => validateReplayInput(input)).toThrowError()
})
