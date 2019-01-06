// @ts-check

const {reduce} = require('p-iteration')
const normalizeRequest = require('./normalize-event')
const checkScope = require('./check-scope')
const applyPolicy = require('./apply-policy')

const DEFAULT_MIDDLEWARES = [
	normalizeRequest,
	checkScope,
	applyPolicy
]

const handler = (event, fn) => {
	return responseTerminated(event) ? event : fn(event)
}

const responseTerminated = event => {
	return (event && event.response && event.response.end === true)
}

/**
 * @param {object} event
 * @param {object} event.request
 * @param {object} [event.response]
 * @param {array} [middlewares]
 */
module.exports = async (event, middlewares) => {
	middlewares = middlewares || DEFAULT_MIDDLEWARES
	return reduce(middlewares, handler, event)
}
