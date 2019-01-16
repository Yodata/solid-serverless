/**
 * retrieves an item from event.context || process.env || defaultValue
 * @param {object} event - event.context if available can override process.env
 * @param {string} key - the environment key to retreive
 * @param {string} [defaultValue] - return if key was not found in event.context or process.env
 * @returns {string}
 */
module.exports = (event, key, defaultValue = '') => {
	const eventContext = event.context || {}
	return eventContext[key] || process.env[key] || defaultValue
}