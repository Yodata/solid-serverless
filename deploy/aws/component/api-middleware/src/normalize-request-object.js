const logger = require('./logger')
const getHeaders = require('./get-headers')

/**
 * sets event.object (JSON) if req/res body is valid JSON
 * @param {object} event
 * @param {object} event.request
 * @param {object} event.response
 * @param {object} event.scope
 * @param {object} event.policy
 * 
 * @returns {object} event
 * @returns {object} event.object - JSON response.body || request.body (if no event.response)
 */
module.exports = async (event) => {
    event.stage = event.response ? 'response' : 'request'
    let httpMessage = event[event.stage]
    
    if (hasData(httpMessage)) {
        event.hasData = true
        event.contentType = getContentType(httpMessage)
        event.object = getData(httpMessage)
    }
    logger.debug('normalized event request', event)
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