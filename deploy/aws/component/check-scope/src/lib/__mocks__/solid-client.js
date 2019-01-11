module.exports = {
	get: jest.fn(async (uri, options) => {
		switch(uri) {
		case 'http://error':
			throw {
				statusCode: 500,
				message: uri,
				options: options
			}
		case 'http://allow': 
			return {
				body: {
					effect: 'Allow',
					condition: {}
				}
			}
		case 'http://deny':
			return {
				body: {
					effect: 'Deny',
					condition: {}
				}
			}
		default:
			return {}
		}
	})
}
