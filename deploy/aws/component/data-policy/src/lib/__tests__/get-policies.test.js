const getPolicies = require('../get-policies')

describe('get-policies unit tests', () => {
    let event,object,policy,PolicyRef,InlinePolicy

    beforeEach(()=>{
        InlinePolicy = {
            'processor': 'Yodata',
            'effect': 'Transform',
            'value': JSON.stringify({password:'$redact'})
        }
        PolicyRef = {
            'RemotePolicy': 'https://dev.yodata.io/public/test/RemotePolicy.json'
        }
        policy = {}
        object = {
            'type': 'Forbidden',
            'password': 'secret',
            'deleteme': 'now you see me'
        }
        event = {object,policy}
        
    })

    test('returns an array of policies', async () => {
        let local = {LocalPolicyName: {type: 'LocalPolicy'}}
        let global = {GlobalPolicyName: {type: 'GlobalPolicy'}}
        event.policy = {local,global}
        let arrayOfPolicies = [{type: 'LocalPolicy'}, {type:'GlobalPolicy'}]
        let response = await getPolicies(event)
        expect(response).toEqual(arrayOfPolicies)
    })

    test('fetches remote policies', async () => {
        let local = PolicyRef
        event.policy = {local}
        let response = await getPolicies(event)
        expect(response).toBeInstanceOf(Array)
        expect(response.length).toEqual(1)
        return expect(response[0]).toEqual({
            "effect": "Transform",
            "processor": "Yodata",
            "type": "DataPolicy",
            "value": "{\"deleteme\":{\"@remove\":true}}"
        })
    })

})

