// @ts-check

const { reduce } = require('p-iteration')
const normalize = require('./normalize-event')
const checkScope = require('./check-scope')
const { applyDataPolicy } = require('./apply-policy')
const processContext = require('./process-context')
const validateSchema = require('./validate-schema')
const createBmsContact = require('./create-bms-contact')
const handlFranchiseTransactionReport = require('./handle-franchise-transactionreport')
const identityQueryHandler = require('./identity-query-handler')
const addSameAsEventHandler = require('./add-sameAs-event-handler')
const has = require('./lib/object-has')

const DEFAULT_MIDDLEWARES = [
	normalize,
	checkScope,
	applyDataPolicy,
	processContext,
	validateSchema,
	createBmsContact,
	handlFranchiseTransactionReport,
	identityQueryHandler,
	addSameAsEventHandler
]

const handler = async (event, fn) => {
	return responseTerminated(event) ? event : fn(event)
}

const responseTerminated = event => {
	return has(event,'response.end', true)
}

/**
 * @typedef MiddlewareResponse
 * @property {object}  request - http.request
 * @property {object}	 [response] - http.response
 * @property {string}  stage - request | response
 * @property {boolean} hasData - true if event has parsed data.object
 * @property {string}	 contentType - mapi.contenttype
 * @property {object}  event.headers
 * @property {object}	 [object] - parsed event body
 */

/**
 * @param {object} event
 * @param {object} event.request
 * @param {object} [event.response]
 * @param {array} [middlewares]
 *
 * @returns {Promise<MiddlewareResponse>}
 */
module.exports = async (event, middlewares = DEFAULT_MIDDLEWARES) => {
	return reduce(middlewares, handler, event)
}
