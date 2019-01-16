const http = require('http')

module.exports = request => {
	if (!request.headers && request.rawHeaders) {
		request.headers = request.rawHeaders
	}
	return http.request(request)
}
