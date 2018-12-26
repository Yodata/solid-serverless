const checkScope = require('../check-scope')
const OLD_ENV = process.env

beforeEach(()=>{
    jest.resetModules()
    process.env = {...OLD_ENV}
    process.env.CHECK_SCOPE_FUNCTION_NAME = 'check-scope'
})
afterEach(()=>{
    process.env = OLD_ENV
})
test('check-scope', async () => {
    let event = require('../example/scope-reject-event.json')
    let expectedResponse = require('../example/scope-reject-response.json')
    let result = await checkScope(event)
    expect(result).toHaveProperty('response')
    expect(result.response).toHaveProperty('status', '403')
    return expect(result.response).toHaveProperty('end', true)
})