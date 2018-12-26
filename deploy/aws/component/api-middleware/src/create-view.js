const AWS = require('aws-sdk')
const lambda = new AWS.Lambda({region: 'us-west-2'})
const logger = require('./logger')
const CREATE_VIEW_FUNCTION_NAME = process.env.CREATE_VIEW_FUNCTION_NAME || 'create-view'

module.exports = async function createView(event) {
  let response
  try {
    const FunctionName = CREATE_VIEW_FUNCTION_NAME
    const Payload = JSON.stringify(event)
    let lambdaResponse = await lambda.invoke({FunctionName, Payload }).promise()
    logger.debug('middleware:create-view:response', response)
    response = lambdaResponse.hasOwnProperty('Payload') ? lambdaResponse.Payload : lambdaResponse
    response = JSON.parse(response) 
  } catch (error) {
    logger.error('middleware:create-view:error', error)
    response = error
  }
  return response
}
