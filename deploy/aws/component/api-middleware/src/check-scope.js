const AWS = require('aws-sdk')
const lambda = new AWS.Lambda()
const FUNCTION_NAME = 'data-processing-service-check-scope'
const logger = require('./logger')
const get = require('lodash/get')
const set = require('lodash/set')


module.exports = async function checkScope(event) {
    return new Promise((resolve) => {
        lambda.invoke(
          {
            FunctionName: FUNCTION_NAME,
            Payload: JSON.stringify(event),
          }, 
          function ( err , response ) {
            if (response.FunctionError) {
              console.log('Check scope lambda returned an error')
              set(event, 'response.status', 403)
              set(event, 'response.body', '{"error":"Request denied due to scope policy"}')
              set(event, 'response.end' , true)
            } else {
              console.log('Check scope lambda did not return an error')
            }

            console.log('Event after lambda: ' + JSON.stringify(event))
            resolve(event)
          })
      })  
}
