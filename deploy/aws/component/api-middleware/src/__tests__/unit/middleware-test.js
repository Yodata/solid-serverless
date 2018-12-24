const service = require('../../middleware')

describe('API MIDDLEWARE', function () {
    const event = require('./test-event.json')
    test('applies policy', async (done) => {
        const result = await service.handler(event)
        expect(result.response).toBeInstanceOf(Object);
    
        const response = result.response
        expect(response.status).toEqual(200);
        expect(response.headers).toHaveProperty('Content-Type', 'application/json')
        expect(response.body).toHaveLength > 0
        let object = JSON.parse(response.body);

        expect(object).toBeInstanceOf(Object)
        expect(object).toHaveProperty('password', '**REDACTED**')
        done()
    });
});

