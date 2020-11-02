module.exports = {
	'request': {
		'id': 'be08d91a-07a4-4850-96be-d78ecb4d8714',
		'timestamp': {
			'seconds': 1604104720,
			'nanos': 100000000
		},
		'security': {
			'agent': 'https://dave.dev.env.yodata.io/profile/card#me',
			'instrument': 'https://dave.dev.env.yodata.io/profile/card#me',
			'isAdmin': true,
			'isDefaultAllowed': false
		},
		'url': 'https://dave.bhhs.dev.yodata.io/event/data/topic/realestate/profile/xxx',
		'target': {
			'id': 'https://dave.bhhs.dev.yodata.io/event/data/topic/realestate/profile/xxx',
			'host': 'dave.bhhs.dev.yodata.io',
			'path': '/publish/',
			'accessType': 'Read'
		},
		'acl': {
			'default': {
				'modes': [
					'Read'
				],
				'scope': []
			},
			'entities': {},
			'patterns': {
				'%BASE_URL%/profile/card#me': {
					'modes': [
						'Read',
						'Write',
						'Append',
						'Control'
					],
					'scope': []
				}
			}
		},
		'method': 'GET',
		'rawHeaders': {
			'x-amzn-trace-id': [
				'Root=1-5f9cb1e6-3b1eb33b55e5b6b97c9e5e90'
			],
			'x-forwarded-proto': [
				'https'
			],
			'x-api-key': [
				'dave_admin'
			],
			'host': [
				'dave.bhhs.dev.yodata.io'
			],
			'x-forwarded-port': [
				'443'
			],
			'x-forwarded-for': [
				'54.245.158.224'
			],
			'accept-encoding': [
				'gzip, deflate'
			],
			'user-agent': [
				'got/9.6.0 (https://github.com/sindresorhus/got)'
			],
			'accept': [
				'application/json'
			]
		},
		'parameters': {
			'*': [
				'public/yodata/data-policy.json'
			]
		},
		'body': '',
		'policy': {
			'local': {
				'removegolivedate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"goLiveDate":{"@remove":true}}'
				},
				'removeoriginalaffiliationdate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"originalAffiliationDate":{"@remove":true}}'
				},
				'@id': 'https://dave.bhhs.dev.yodata.io/public/yodata/data-policy.json',
				'id': 'https://dave.bhhs.dev.yodata.io/public/yodata/data-policy.json'
			},
			'global': {
				'removegolivedate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"goLiveDate":{"@redact":true}}'
				},
				'removeoriginalaffiliationdate': {
					'effect': 'Transform',
					'processor': 'Yodata',
					'type': 'DataPolicy',
					'value': '{"originalAffiliationDate":{"@remove":true}}'
				}
			}
		},
		'isBase64Encoded': true
	},
	'response': {
		contentType: 'application/ld+json',
		'status': 200,
		'headers': {
			'content-type': 'application/ld+json',
		},
		'body': 'eyJyZW1vdmVnb2xpdmVkYXRlIjp7ImVmZmVjdCI6IlRyYW5zZm9ybSIsInByb2Nlc3NvciI6IllvZGF0YSIsInR5cGUiOiJEYXRhUG9saWN5IiwidmFsdWUiOiJ7XCJnb0xpdmVEYXRlXCI6e1wiQHJlbW92ZVwiOnRydWV9fSJ9LCJyZW1vdmVvcmlnaW5hbGFmZmlsaWF0aW9uZGF0ZSI6eyJlZmZlY3QiOiJUcmFuc2Zvcm0iLCJwcm9jZXNzb3IiOiJZb2RhdGEiLCJ0eXBlIjoiRGF0YVBvbGljeSIsInZhbHVlIjoie1wib3JpZ2luYWxBZmZpbGlhdGlvbkRhdGVcIjp7XCJAcmVtb3ZlXCI6dHJ1ZX19In0sIkBpZCI6Imh0dHBzOi8vZGF2ZS5iaGhzLmRldi55b2RhdGEuaW8vcHVibGljL3lvZGF0YS9kYXRhLXBvbGljeS5qc29uIiwiaWQiOiJodHRwczovL2RhdmUuYmhocy5kZXYueW9kYXRhLmlvL3B1YmxpYy95b2RhdGEvZGF0YS1wb2xpY3kuanNvbiJ9',
		'isBase64Encoded': true,
		object: {
			foo: 'bar'
		}
	},
	'agent': 'https://dave.dev.env.yodata.io/profile/card#me',
	'instrument': 'https://dave.dev.env.yodata.io/profile/card#me',
	'scope': [],
	'policy': {
		'local': {
			'removegolivedate': {
				'effect': 'Transform',
				'processor': 'Yodata',
				'type': 'DataPolicy',
				'value': '{"goLiveDate":{"@remove":true}}'
			},
			'removeoriginalaffiliationdate': {
				'effect': 'Transform',
				'processor': 'Yodata',
				'type': 'DataPolicy',
				'value': '{"originalAffiliationDate":{"@remove":true}}'
			},
			'@id': 'https://dave.bhhs.dev.yodata.io/public/yodata/data-policy.json',
			'id': 'https://dave.bhhs.dev.yodata.io/public/yodata/data-policy.json'
		},
		'global': {
			'removegolivedate': {
				'effect': 'Transform',
				'processor': 'Yodata',
				'type': 'DataPolicy',
				'value': '{"goLiveDate":{"@redact":true}}'
			},
			'removeoriginalaffiliationdate': {
				'effect': 'Transform',
				'processor': 'Yodata',
				'type': 'DataPolicy',
				'value': '{"originalAffiliationDate":{"@remove":true}}'
			}
		}
	},
	'stage': 'response',
	'hasData': true,
	'contentType': 'application/json',
	'object': {
		foo: 'bar'
	}
}