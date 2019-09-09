const createView = require('../lib/create-view')

describe('create-view', () => {
	test('example.v1 event.context + event.object', async () => {
		const event = {
			'object': {
				'@context': 'http://schema.org',
				'@type': 'AskAction',
				'agent': {
					'@type': 'Person',
					'name': 'Bob',
					'email': 'user@example.com'
				},
				'recipient': {
					'@type': 'RealEstateAgent',
					'@id': 'https://465156.ds.bhhsresource.com/profile/card#me'
				}
			},
			'context': {
				'@view':'{ "type": "Lead", \'lead\': agent, \'user\': recipient.\'@id\' }'
			}
		}
		const expectedResult = {
			'type': 'Lead',
			'lead': {
				'@type': 'Person',
				'email': 'user@example.com',
				'name': 'Bob'
			},
			'user': 'https://465156.ds.bhhsresource.com/profile/card#me'
		}
		const result = await createView(event)
		expect(result).toEqual(expectedResult)
	})

	test('example.v2 event.scope + event.topic', async () => {
		const event = {
			'object': {
				'topic': 'realestate/contact#add',
				'data': {
					'type': 'AddAction',
					'object': {
						'name': 'Bruce Wayne',
						'type': 'Contact'
					}
				}
			},
			'scope': {
				'realestate/contact#add': 'https://subscriber.dev.yodata.io/public/context/stage/testcontext.cdef.yaml'
			}
		}
		const result = await createView(event)
		expect(result).toHaveProperty('type', 'Contact')
		return expect(result).toHaveProperty('name', 'TEST_CONTEXT_WAS_HERE')
	})

	test('remote event.context', async () => {
		const event = {
			context: 'https://subscriber.dev.yodata.io/public/context/stage/testcontext.cdef.yaml',
			object: { type: 'test' }
		}
		const result = await createView(event)
		return expect(result).toHaveProperty('name', 'TEST_CONTEXT_WAS_HERE')
	})
})