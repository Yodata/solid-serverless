
/**
 * Normalizes a lambda invocation to work like a regular async call
 * @param {string} FunctionName
 * @param {object} event - lambda Payload
 * @param {object} [lambdaConfig] - Aws.Lambda.InvokeParams
 * @returns {Promise<object>} returns the InvokeResponse.Payload
 */
async function invokeLambdaFunction(FunctionName, event, lambdaConfig = {}) {

	const { error, response } = lambdaConfig
	if (error) {
		return Promise.reject({error})
	}
	return Promise.resolve(response || event)
}


module.exports = jest.fn(invokeLambdaFunction)
