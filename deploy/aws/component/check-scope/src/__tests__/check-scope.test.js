/* eslint-disable no-undef */
const checkScope = require('../check-scope')
jest.mock('../lib/solid-client.js')

describe('check-scope', () => {

	test('example event/response', () => {
		const event = require('../example/event.json')
		const response = require('../example/response.json')
		return expect(checkScope(event)).resolves.toEqual(response)
	})

	test('middleware params', async () => {
		const middlewareEvent = {
			object: {
				stage: 'request',
				object: {
					type: 'dog'
				}
			},
			scope: {
				noDogsAllowed: {
					'processor': 'Mingo',
					'effect': 'Deny',
					'condition': {
						object: {
							type: 'dog'
						}
					}}
			}
		}
		const result = await checkScope(middlewareEvent)
		return expect(result).toHaveProperty('isAllowed', false)
	})

	test('deny', async () => {
		const object = {type:'test'}
		const scope = {test: {
			effect: 'Deny',
			condition: {
				type: 'test'
			}
		}}
		let result = await checkScope({object, scope})
		return expect(result).toHaveProperty('isAllowed', false)
	})

	test('allow', async () => {
		const object = {type:'test'}
		const scope = {test: {
			effect: 'Allow',
			condition: {
				type: 'test'
			}
		}}
		let result = await checkScope({object, scope})
		return expect(result).toHaveProperty('isAllowed', true)
	})

	test('remote.allow', async () => {
		const object = {type:'test'}
		const scope = {'allow': 'http://allow'}
		let result = await checkScope({object, scope})
		return expect(result).toHaveProperty('isAllowed', true)
	})

	test('remote.deny', async () => {
		const object = {type:'test'}
		const scope = {'allow': 'http://deny'}
		let result = await checkScope({object, scope})
		return expect(result).toHaveProperty('isAllowed', false)
	})

	test('remote.error', async () => {
		const object = {type:'test'}
		const scope = {'error': 'http://error'}
		let result = await checkScope({object, scope})
		expect(result).toHaveProperty('error')
		return expect(result).toHaveProperty('isAllowed', false)
	})

	test('remote.error + allow', async () => {
		const object = {type:'test'}
		const scope = {'error': 'http://error', allow: 'http://allow'}
		let result = await checkScope({object, scope})
		return expect(result).toHaveProperty('isAllowed', true)
	})

	test('remote.error + deny', async () => {
		const object = {type:'test'}
		const scope = {'error': 'http://error', allow: 'http://deny'}
		let result = await checkScope({object, scope})
		return expect(result).toHaveProperty('isAllowed', false)
	})

})