// @ts-check
const logger = require('./logger')
const {Lambda} = require('aws-sdk')

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
	let response
	try {
		const config = Object.assign(defaultLambdaConfig, lambdaConfig)
		const lambda = new Lambda(config)
		const Payload = JSON.stringify(event)
		const lambdaResponse = await lambda.invoke({FunctionName, Payload}).promise()
		if (functionError(lambdaResponse)) {
			logger.error('InvokeLambdaFunction:Error', lambdaResponse)
		}
		response = lambdaResponse.Payload
		response = JSON.parse(response)
	} catch (error) {
		logger.error('InvokeLambdaFunction:UnhandledError', {error, event})
		response = Object.assign(event, {error: error.message})
	}
	logger.debug(`response type = ${typeof response}`)
	logger.debug('InvokeLambdaFunction:result', {response})
	return response
}

const functionError = response => {
	return (typeof response.FunctionError !== 'undefined')
}

module.exports = invokeLambdaFunction
