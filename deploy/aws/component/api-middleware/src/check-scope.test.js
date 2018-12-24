const checkScope = require('./check-scope')
const createContext = require('./create-context')
const event = require('../event.json')
test('check-scope', async () => {
    let input = {
        request: {
            method: 'POST',
            rawHeaders: {
                "content-type": ['application/json']
            },
            body: JSON.stringify({type:'ForbiddenType'})
        },
        object: {type:'ForbiddenType'},
        scope: {},
        policy: {},
    }
    let result = await checkScope(input)
    expect(result).toHaveProperty('response')
    expect(result.response).toHaveProperty('status', '403')
    expect(result.response).toHaveProperty('statusCode', 403)
    // expect(result.response).toHaveProperty('body', 'Forbidden')
    return expect(result.response).toHaveProperty('end', true)
})