const validate = require('../validate-input')
const { getEvent, getOptions } = require('../__mocks__/event')

test('tests required environment variables', async function () {
	expect.assertions(4)
	let event = getEvent()
	expect(validate(event)).resolves.toEqual(event)
	event = getEvent()
	delete event.target
	expect(validate(event)).rejects.toHaveProperty('message', 'VALIDATION_ERROR')
	event = getEvent()
	delete event.options
	const expectedErrors = Object.keys(getOptions()).map(key => `missing required environment variable ${key}`)
	expect(validate(event)).rejects.toHaveProperty('errors.length', expectedErrors.length + 1)
	expect(validate(event)).rejects.toHaveProperty('errors', expect.arrayContaining(expectedErrors))
})
