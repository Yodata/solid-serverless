
const eventString = '{"request":{"method":"GET","headers":{"Content-Type":["application/json"]},"target":{"id":"https://dave.bhhs.dev.yodata.io/public/yodata/data-policy.json","host":"dave.bhhs.dev.yodata.io","path":"/public/yodata/data-policy.json","accessType":"Read"},"body":""},"response":{"headers":{"Content-Type":["application/json"]},"body":"{\\"password\\": \\"secret\\"}","isBase64Encoded":false},"scope":{"NoDogsAllowed":{"effect":"Deny","condition":{"object":{"type":"Dog"}}}},"policy":{"local":{"RedactPassword":{"type":"DataPolicy","processor":"Yodata","effect":"Transform","value":"{ \\"password\\": {\\"value\\": \\"[PASSWORD]\\"} }"}}}}'

/**
* @typedef NormalizedEventResponse
* @property {object}  request - http.request
* @property {string}  request.method = http request method
* @property {object}  request.target = http request taret
* @property {object}	object - parsed event body
* @property {string}  stage - request | response
* @property {boolean} hasData - true if event has parsed data.object
* @property {string}	contentType - mapi.contenttype
* @property {object}	[response] - http.response
*/

module.exports = {
	createNormalizedEvent: createNormalizedEvent
}


/**
 *
 * @param {string} method
 * @param {string} path
 * @param {object} [data]
 * @returns {NormalizedEventResponse}
 */
function createNormalizedEvent(method = 'POST', path = '/outbox/', data = {}) {
	const event = JSON.parse(eventString)
	event.stage = 'request'
	event.hasData = true
	event.request.method = method
	event.request.target.path = path
	event.object = data
	event.contentType = 'application/json'
	return event
}