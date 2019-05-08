const createView = require('../lib/create-view')
const event = require('../example/event.json')
const response = require('../example/response.json')

describe('create-view', () => {
	test('example.v1 event.context', () => {
		const event = {
			"object": {
				"@context": "http://schema.org",
				"@type": "AskAction",
				"agent": {
					"@type": "Person",
					"name": "Bob",
					"email": "user@example.com"
				},
				"recipient": {
					"@type": "RealEstateAgent",
					"@id": "https://465156.ds.bhhsresource.com/profile/card#me"
				}
			},
			"context": {
				"@view": {
					"type": "'Lead'",
					"lead": "agent",
					"user": "recipient.'@id'"
				}
			}
		}
		const expectedResult = {
			"type": "Lead",
			"lead": {
				"@type": "Person",
				"email": "user@example.com",
				"name": "Bob"
			},
			"user": "https://465156.ds.bhhsresource.com/profile/card#me"
		}
		return expect(createView(event)).resolves.toEqual(expectedResult)
	})

	test('example.v2 event.scope/topic', async () => {
		const event = {
			"object": {
				"id": "https://subscriber.dev.yodata.io/inbox/c886f44a13bb4ae2b8fd9ff47175927a",
				"agent": "https://user.dev.yodata.io/profile/card#me",
				"instrument": "https://subscriber.dev.yodata.io/profile/card#me",
				"time": "2019-05-07T22:34:49.074Z",
				"timestamp": 1557268489074,
				"topic": "realestate/contact#add",
				"data": {
					"type": "AddAction",
					"object": {
						"name": "Bruce Wayne",
						"type": "Contact"
					}
				}
			},
			"scope": {
				"realestate/contact#add": "https://subscriber.dev.yodata.io/public/context/stage/testcontext.cdef.yaml"
			}
		}
		const result = await createView(event)
		expect(result).toHaveProperty('type', 'Contact')
		expect(result).toHaveProperty('name', 'Bruce Wayne')
	})
})