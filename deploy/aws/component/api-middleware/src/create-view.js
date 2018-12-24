const AWS = require('aws-sdk')
const lambda = new AWS.Lambda()
const FUNCTION_NAME = 'data-processing-service-create-view'
const logger = require('./logger')


module.exports = async function createView(event) {
    return new Promise((resolve) => {
        lambda.invoke(
          {
            FunctionName: FUNCTION_NAME,
            Payload: JSON.stringify(event)
          }, 
          function ( err , lambdaResponse ) {
            if (err) {
              console.error(err)
            }
            let createViewResponse = JSON.parse(lambdaResponse.Payload)
            let view = createViewResponse.object
            logger.info('view created', {view})
            resolve(view)
          })
      })  
}



