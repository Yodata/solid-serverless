// @ts-check

const { reduce } = require('p-iteration')
const normalize = require('./normalize-event')
const checkScope = require('./check-scope')
const applyPolicy = require('./apply-policy')
const processContext = require('./process-context')
const validateSchema = require('./validate-schema')
const createBmsContact = require('./create-bms-contact')
const handlFranchiseTransactionReport = require('./handle-franchise-transactionreport')
const DEFAULT_MIDDLEWARES = [
	normalize,
	checkScope,
	applyPolicy,
	processContext,
	validateSchema,
	createBmsContact,
	handlFranchiseTransactionReport
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
