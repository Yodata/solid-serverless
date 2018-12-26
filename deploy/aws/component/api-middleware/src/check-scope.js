const AWS = require('aws-sdk')
const lambda = new AWS.Lambda({region:'us-west-2'})
const logger = require('./logger')
const set = require('lodash/set')


module.exports = async function checkScope(event) {
  try {
    const FunctionName = process.env.CHECK_SCOPE_FUNCTION_NAME
    const Payload = JSON.stringify(event)
    const response = await lambda.invoke({FunctionName, Payload, }).promise()
    logger.debug('checkScope invoke response', response)
    if (response.isAllowed === false) {
      set(event, 'response.status', 403)
      set(event, 'response.body', JSON.stringify({error: 'Request denied due to scope policy.'}))
      set(event, 'response.end' , true)
    }
  } catch (error) {
    logger.error('checkScope error', error)
  }
  return event
}
