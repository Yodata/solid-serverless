const event = require('./event.json')
const policy = require('./policy')
const remotePolicy = require('./RemotePolicy.json')
const response = require('./response.json')

const createEvent = () => (JSON.parse(JSON.stringify(event)))

const createRequest = (method = 'GET', path = '/profile/card', contentType = 'application/json') => {
	const AccessType = {
		'GET': 'Read',
		'POST': 'Append',
		'PUT': 'Write'
	}

	return {
		id: 'requestid',
		timestamp: {
			'seconds': Date.now(),
			'nanos': 123000000
		},
		method: String(method).toUpperCase(),
		headers: {
			'Content-Type': [
				contentType
			]
		},
		target: {
			id: `https://user.example.com/${path}`,
			host: 'user.example.com',
			path: String(path).toLowerCase(),
			accessType: AccessType[ method ]
		},
		body: '',
		security: {
			agent: 'https://user.example.com/profile/card#Me',
			instrument: 'https://user.example.com/profile/card#Me',
			isAdmin: false,
			isDefaultAllowed: false
		},
		destination: {
			'id': 'https://buyside.bhhs.hsfaffiliates.com/publish/f0f813b4132f4a9782aab61710186412',
			'host': 'buyside.bhhs.hsfaffiliates.com',
			'path': '/publish/f0f813b4132f4a9782aab61710186412'
		},
		acl: {
			default: {
				'modes': [],
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
		'rawHeaders': {
			'content-length': [
				'470'
			],
			'x-amzn-trace-id': [
				'Root=1-5fb906ea-756617df609e02274fd3b644'
			],
			'x-forwarded-proto': [
				'https'
			],
			'x-api-key': [
				'57jp06h30WMwgXXUgPuyNOuTsB6zoxA1TS33VYa9JD'
			],
			'host': [
				'buyside.bhhs.hsfaffiliates.com'
			],
			'x-forwarded-port': [
				'443'
			],
			'content-type': [
				'application/json'
			],
			'x-forwarded-for': [
				'13.58.73.59'
			],
			'accept-encoding': [
				'gzip,deflate'
			],
			'user-agent': [
				'HTTPie/0.9.9'
			],
			'accept': [
				'*/*'
			]
		},
		'parameters': {
			'*': [
				'publish/'
			]
		},
		policy: {
			'global': {
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
				}
			}
		},
		'isBase64Encoded': true
	}
}

module.exports = {
	event,
	policy,
	remotePolicy,
	response,
	createEvent,
	createRequest,
}