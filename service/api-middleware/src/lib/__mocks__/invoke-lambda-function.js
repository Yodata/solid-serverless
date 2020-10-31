
/**
 * Normalizes a lambda invocation to work like a regular async call
 * @param {string} FunctionName
 * @param {object} event - lambda Payload
 * @param {object} [lambdaConfig] - Aws.Lambda.InvokeParams
 * @returns {Promise<object>} returns the InvokeResponse.Payload
 */
const invokeLambdaFunction = async (FunctionName, event, lambdaConfig = {}) => {
	const { error, response } = lambdaConfig
	if (error) {
		throw new Error(error)
	}
	return response ? response : event
}


module.exports = invokeLambdaFunction
