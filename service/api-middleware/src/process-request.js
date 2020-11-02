// @ts-check

const { reduce } = require('p-iteration')
const normalize = require('./normalize-event')
const checkScope = require('./check-scope')
const { applyDataPolicy } = require('./apply-policy')
const processContext = require('./process-context')
const validateSchema = require('./validate-schema')
const createBmsContact = require('./create-bms-contact')
const handlFranchiseTransactionReport = require('./handle-franchise-transactionreport')
const DEFAULT_MIDDLEWARES = [
	normalize,
	checkScope,
	applyDataPolicy,
	processContext,
	validateSchema,
	createBmsContact,
	handlFranchiseTransactionReport
]

const handler = async (event, fn) => {
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
 *
 * @returns {Promise<object>}
 */
module.exports = async (event, middlewares = DEFAULT_MIDDLEWARES) => {
	return reduce(middlewares, handler, event)
}
