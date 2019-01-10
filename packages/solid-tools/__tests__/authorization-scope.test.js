/* eslint-disable no-undef */
'use strict'
const Scope = require('../lib/AuthorizationScope')

describe('@yodata/authorization-scope', () => {
	const processor = 'Mingo'
	const condition = {type: 'Person'}

	test('constructor.defaults', () => {
		const scope = new Scope({processor, condition})
		expect(scope).toHaveProperty('effect', 'Allow')
		expect(scope).toHaveProperty('processor', 'Mingo')
		expect(scope).toHaveProperty('condition', condition)
	})

	test('matches', () => {
		const condition = {type: 'Person'}
		const scope = new Scope({condition})
		expect(scope.matches(condition)).toBeTruthy()
		expect(scope.matches({type: 'error'})).toBeFalsy()
		expect(scope.matches({})).toBeFalsy()
	})

	test('isAllowed', () => {
		const scope = new Scope({condition})
		expect(scope.isAllowed(condition)).toBeTruthy()
		expect(scope.isAllowed({type: 'error'})).toBeFalsy()
	})

	test('effect', () => {
		const effect = 'Deny'
		const scope = new Scope({condition, effect})
		expect(scope.isAllowed(condition)).toBeFalsy()
		expect(scope.isAllowed({type: 'error'})).toBeTruthy()
	})

	test('empty scope is allowed', () => {
		const scope = new Scope({})
		return expect(scope.isAllowed({type: 'anything'})).toBeTruthy()
	})

	test('example', () => {
		const scope = new Scope({effect: 'Deny', condition: {
			object: {
				type: 'dog'
			}
		}})
		const event = {
			object: {
				type: 'dog'
			}
		}
		expect(scope.isAllowed(event)).toBeFalsy()
	})
})
