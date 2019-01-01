const setPolicy = require('../set-policy')
describe('set-policy', ()=>{
    let oldEnv
    beforeAll(()=>{
        oldEnv = process.env
        process.env.DOMAIN = 'dev.yodata.io'
    })
    afterAll(()=>{
        process.env = oldEnv
    })

    test('returns an iri', async () => {
        let name = 'TestPolicy'
        let value = {
            processor: 'Yodata',
            effect: 'Transform',
            value: {
                'firstName': 'givenName'
            }
        }
        let response = await setPolicy({name,value})
        expect(response).toEqual(expect.any(String))
    })
})
