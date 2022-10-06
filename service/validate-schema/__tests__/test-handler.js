
const { handler: validate } = require('../src/http/post-index/index')
const request = require('../example/request')
const response = require('../example/response')

test('example.request.valid returns example.response.valid', async () => {
	const result = await validate(request.valid)
	console.log(result)
	return expect(result).toMatchObject(response.valid)
})

test('example.request.invalid return example.response.invalid', async () => {
	const result = await validate(request.invalid)
	return expect(result).toMatchObject(response.invalid)
})
