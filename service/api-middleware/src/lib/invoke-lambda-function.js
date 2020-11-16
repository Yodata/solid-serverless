// @ts-check
const logger = require('./logger')
const { Lambda } = require('aws-sdk')
const stringify = require('fast-safe-stringify').default
const has = require('../lib/object-has')

const defaultLambdaConfig = {
	region: 'us-west-2'
}

/**
 * Normalizes a lambda invocation to work like a regular async call
 * @param {string} FunctionName
 * @param {object} event - lambda Payload
 * @param {object} [lambdaConfig] - Aws.Lambda.InvokeParams
 * @returns {Promise<object>} returns the InvokeResponse.Payload
 */
const invokeLambdaFunction = async (FunctionName, event, lambdaConfig) => {
	const config = Object.assign(defaultLambdaConfig, lambdaConfig)
	const lambda = new Lambda(config)
	const Payload = stringify(event)
	const lambdaResponse = await lambda.invoke({ FunctionName, Payload }).promise()
	if (has(lambdaResponse, 'FunctionError', false)) {
		let message = lambdaResponse.Payload.toString()
		let name = `INVOKE_ERROR:${FunctionName}`
		logger.error(name, { message })
		throw new Error(name)
	} else {
		try {
			return JSON.parse(lambdaResponse.Payload.toString())
		} catch (e) {
			return lambdaResponse.Payload.toString()
		}
	}
}

module.exports = invokeLambdaFunction
