const { get } = require('lodash')
const getHeaders = require('./lib/get-headers')

/**
 * sets event.object (JSON) if req/res body is valid JSON
 * @param {object} event
 * @param {object} event.request
 * @param {object} event.response
 * @param {object} event.scope
 * @param {object} event.policy
 */
module.exports = async (event) => {
    event.requestMethod = event.request.method
    switch(event.requestMethod) {
        case 'POST':
        case 'PUT':
        case 'PATCH':
        if (hasData(event.request)) {
            event.contentType = getContentType(event.request)
            event.object = getData(event.request)
        }
        break
        case 'GET':
        if (event.response && hasData(event.response)) {
            event.contentType = getContentType(event.response)
            event.object = getData(event.response)
        }
        break
        case 'DELETE':
        break
        default:
        throw new Error(`Unexpected request.method ${requestMethod}`)
    }
    return event
}



const getContentType = (message) => {
    return getHeaders(message)['content-type']
}

const hasData = (httpMessage) => {
    let contentType = getContentType(httpMessage)
    if (contentType === 'application/json' || contentType === 'application/ld+json') {
        return true
    }
}

const getData = (httpMessage) => {
    if (hasData(httpMessage)) {
        return JSON.parse(httpMessage.body)
    }
}