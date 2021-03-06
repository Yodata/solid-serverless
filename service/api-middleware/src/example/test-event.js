
module.exports = {
	'request': {
		'method': 'GET',
		'url': 'https: //testuser.dev.yodata.io/test/authorization/.acl',
		'body': 'e30=',
		'isBase64Encoded': true
	},
	'response': {
		'status': 200,
		'headers': {
			'content-type': 'application/json',
			'Content-Length': '220',
			'Content-Type': 'application/json'
		},
		'body': 'eyIjaHR0cHM6Ly90ZXN0YXBwLmRldi55b2RhdGEuaW8vcHJvZmlsZS9jYXJkI21lIjp7InR5cGUiOiJBdXRob3JpemF0aW9uIiwiYWdlbnQiOiJodHRwczovL3Rlc3RhcHAuZGV2LnlvZGF0YS5pby9wcm9maWxlL2NhcmQjbWUiLCJhY2Nlc3NUbyI6Imh0dHBzOi8vdGVzdHVzZXIuZGV2LnlvZGF0YS5pby90ZXN0L2F1dGhvcml6YXRpb24vLmFjbCIsIm1vZGUiOlsiUmVhZCJdfSwiI2h0dHBzOi8vdGVzdHVzZXIuZGV2LnlvZGF0YS5pby9wcm9maWxlL2NhcmQjbWUiOnsidHlwZSI6IkF1dGhvcml6YXRpb24iLCJhZ2VudCI6Imh0dHBzOi8vdGVzdHVzZXIuZGV2LnlvZGF0YS5pby9wcm9maWxlL2NhcmQjbWUiLCJhY2Nlc3NUbyI6Imh0dHBzOi8vdGVzdHVzZXIuZGV2LnlvZGF0YS5pby90ZXN0L2F1dGhvcml6YXRpb24vLmFjbCIsIm1vZGUiOlsiUmVhZCIsIldyaXRlIiwiQXBwZW5kIiwiQ29udHJvbCJdfX0=',
		'isBase64Encoded': true
	},
	'agent': 'https: //dave.dev.yodata.io/profile/card#me',
	'instrument': 'https: //dave.dev.yodata.io/profile/card#me',
	'scope': {},
	'policy': {
		'local': {
			'RedactPassword': {
				'type': 'DataPolicy',
				'processor': 'Yodata',
				'effect': 'Transform',
				'value': '{ "password": { "value": "[PASSWORD]" } }'
			}
		}
	}
}