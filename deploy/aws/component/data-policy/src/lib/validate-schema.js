const {Lambda} = require('aws-sdk')

const lambda = new Lambda({region: 'us-west-2'})

module.exports = async event => {
	try {
		const FunctionName = process.env.VALIDATE_SCHEMA_FUNCTION_NAME || 'validate-schema'
		const Payload = JSON.stringify(event)
		const response = await lambda.invoke({FunctionName, Payload}).promise()
		console.log({response})
	} catch (error) {
		logger.error('checkScope error', error)
	}
	return response
}
