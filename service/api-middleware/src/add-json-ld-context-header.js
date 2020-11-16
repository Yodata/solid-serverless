/**
 * Adds jsonld context link to response.headers.Link
 * @param   {object} event
 * @param   {object} event.response
 * @param   {object} event.response.headers
 * @returns {Promise<object>}
 */
module.exports = async event => {
	// If response is JSON, add the json-ld link header per the standard
	if (event.response && event.response.headers && event.response.headers['Content-Type'] === 'application/json') {
		const DEFAULT_JSONLD_CONTEXT = process.env.DEFAULT_JSONLD_CONTEXT
		const linkValue = `<${DEFAULT_JSONLD_CONTEXT}>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"`
		const currentValue = event.response.headers.Link
		event.response.headers.Link = currentValue ? `${currentValue};${linkValue}` : linkValue
	}
	return event
}
