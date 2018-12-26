const { handler, exampleEvent, exampleResponse, exampleErrorEvent, exampleErrorResponse } = require('..')

describe('API MIDDLEWARE', function () {
    let event, expectedResponse

    beforeEach(()=>{
        event = exampleEvent()
        expectedResposne = exampleResponse()
    })
    
    test('response matches example/response.json', async () => {
        let response = await handler(event)
        return expect(response).toMatchObject(expectedResponse)
    })

    test('no scope/policy returns event', async () => {
        delete event.scope
        delete event.policy
        let response = await handler(event)
        return expect(response).toEqual(event)
    })

})

