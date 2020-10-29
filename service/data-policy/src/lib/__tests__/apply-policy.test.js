/* eslint-disable quotes */
const applyPolicy = require('../apply-policy')

describe('data-policy-apply-policy', () => {
	test('response', async () => {
		let event = {
			object: {
				password: 'test'
			},
			policy: {
				local: {
					RedactPassword: {
						processor: 'Yodata',
						effect: 'Transform',
						value: JSON.stringify({ password: { value: '[PASSWORD]' } })
					}
				}
			}
		}
		const result = await applyPolicy(event)
		expect(result).toHaveProperty('object')
		expect(result).toHaveProperty('policy')
		return expect(result.object).toHaveProperty('password', '[PASSWORD]')
	})

	test('uri object keys', async () => {
		let id = '#https://testapp.dev.yodata.io/profile/card#me'
		let event = {
			object: {
				[ id ]: {
					password: 'secret'
				}
			},
			policy: {
				local: {
					RedactPassword: {
						type: 'DataPolicy',
						processor: 'Yodata',
						effect: 'Transform',
						value: {
							password: {
								value: '[PASSWORD]'
							}
						}
					}
				}
			}
		}
		const result = await applyPolicy(event)
		expect(result).toHaveProperty('object')
		expect(result).toHaveProperty('policy')
		return expect(result.object[ id ]).toHaveProperty('password', '[PASSWORD]')
	})


	test('new event format', async () => {
		let event = {
			"policy": {
				"global": {
					"removegolivedate": {
						"effect": "Transform",
						"processor": "Yodata",
						"type": "DataPolicy",
						"value": "{\"goLiveDate\":{\"@remove\":true}}"
					},
					"removeoriginalaffiliationdate": {
						"effect": "Transform",
						"processor": "Yodata",
						"type": "DataPolicy",
						"value": "{\"originalAffiliationDate\":{\"@redact\":true}}"
					}
				}
			},
			"object": {
				"type": "test",
				"description": "this object had two fields that should have policies applied, additionalProperty.originalAffiliationDate and additionalProperty.",
				"additionalProperty": {
					"originalAffiliationDate": "2020-10-21T19:49:01Z"
				},
				"goLiveDate": "2020-10-21T19:49:01Z"
			}
		}

		let result = await applyPolicy(event)
		expect(result).toHaveProperty('object.type','test')
		expect(result).toHaveProperty('policy.global.removegolivedate.effect', 'Transform')
		expect(result).toHaveProperty('object.additionalProperty.originalAffiliationDate', "@redact")
		return expect(result).not.toHaveProperty('object.goLiveDate')
	})


})
