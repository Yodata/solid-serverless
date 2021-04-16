const logger = require('./lib/logger')
const getEnvValue = require('./lib/get-env-value')
const invokeLambdaFunction = require('./lib/invoke-lambda-function')

const graphIdentity = async (event) => {
    const stage = getEnvValue(event, 'NODE_ENV', 'staging')
    const functionName = getEnvValue(event, 'IDENTITY_RESOLVE_FUNCTION_NAME', `${stage}-graph-identity`)
    return invokeLambdaFunction(functionName, {
        headers: { 'Content-Type': 'application/json' },
        body: event.object
    })
}
const matchPath = (url = '', subStr) => new URL(url).pathname == subStr
const POST = 'post'
const path = '/api/identity/graph/'
const isGraphIdentityRequest = (event = {}) => {
    const { request } = event
    return (
        request
        && ((request.method || '').toLowerCase() === POST)
        && matchPath(request.url, path)
    )
}


/**
* calls graph-identity service if request is a post to /api/identity/graph/
* @param {object} event
* @param {object} event.object
* @param {object} event.scope
* @param {boolean} event.isAllowed
* @param {object} [event.event]
* @param {object} [event.response]
* @returns {Promise<object>}
*/
async function identityQueryHandler(event) {
    if (isGraphIdentityRequest(event)) {
        logger.debug('graphidentity:received', event)
        await graphIdentity(event)
            .then((response = {}) => {
                logger.debug('identity-graph-result', { response })
                let object = (typeof response.body === 'string') ? JSON.parse(response.body) : response.body
                event.object = Object.assign(object, {
                    actionStatus: object.actionStatus || ((response.status < 300) ? 'CompletedActionStatus' : 'FailedActionStatus')
                })
                let status = response.status || response.statusCode || 201
                // event.response = Object.assign(response, {
                //     status: Number(status),
                //     statusCode: String(status),
                //     end: true
                // })
                event.response = Object.assign(object, {
                    status: Number(status),
                    statusCode: String(status),
                    end: true
                })
                event.response = Object.assign(response, {
                    status: Number(status),
                    statusCode: String(status),
                    end: true
                })
            })
            .catch(error => {
                event.object = Object.assign(event.object, {
                    actionStatus: 'FailedActionStatus',
                    error: {
                        message: error.message,
                        stack: error.stack
                    }
                })
                event.response = {
                    status: 400,
                    statusCode: '400',
                    end: true,
                    headers: {
                        'content-type': 'application/json'
                    }
                }
            })
    }
    logger.debug('identity:graph:response', { event })
    return event
}


module.exports = identityQueryHandler