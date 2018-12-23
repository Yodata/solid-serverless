'use strict';
const Scope = require('../lib/AuthorizationScope')

describe('@yodata/authorization-scope', () => {
    const processor = 'Mingo'
    const condition = {type:"Person"}

    test('constructor.defaults', () => {
        let scope = new Scope({processor, condition})
        expect(scope).toHaveProperty('effect', 'Allow')
        expect(scope).toHaveProperty('processor', 'Mingo')
        expect(scope).toHaveProperty('condition', condition)
    })

    test('matches', () => {
        let condition = {type: 'Person'}
        let scope = new Scope({condition})
        expect(scope.matches(condition)).toBeTruthy()
        expect(scope.matches({type:'error'})).toBeFalsy()
        expect(scope.matches({})).toBeFalsy()
    })

    test('isAllowed', () => {
        let scope = new Scope({condition})
        expect(scope.isAllowed(condition)).toBeTruthy()
        expect(scope.isAllowed({type:'error'})).toBeFalsy()
    })

    test('effect', () => {
        let effect = 'Deny'
        let scope = new Scope({condition, effect})
        expect(scope.isAllowed(condition)).toBeFalsy()
        expect(scope.isAllowed({type:'error'})).toBeTruthy()
    })

});
