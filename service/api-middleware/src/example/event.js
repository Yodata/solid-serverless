module.exports = {
	'request': {
		'method': 'GET',
		'headers': {
			'Content-Type': [
				'application/json'
			]
		},
		'body': ''
	},
	'response': {
		'headers': {
			'Content-Type': [
				'application/json'
			]
		},
		'body': '{"password": "secret"}',
		'isBase64Encoded': false
	},
	'scope': {
		'NoDogsAllowed': {
			'effect': 'Deny',
			'condition': {
				'object': {
					'type': 'Dog'
				}
			}
		}
	},
	'policy': {
		'local': {
			'RedactPassword': {
				'type': 'DataPolicy',
				'processor': 'Yodata',
				'effect': 'Transform',
				'value': '{ "password": {"value": "[PASSWORD]"} }'
			}
		}
	}
}