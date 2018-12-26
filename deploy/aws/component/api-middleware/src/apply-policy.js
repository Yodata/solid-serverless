const logger = require('./logger')
const createView = require('./create-view')

/**
 * apply data policies
 * @param {object} event
 * @param {object} event.object - the JSON representation of response.body
 * @param {string} event.stage - 'request' || 'response'
 * @param {boolean} event.hasData
 */
module.exports = async (event) => {
    try {
        if (event.stage === 'response' && event.hasData === true) {
            event.view = await createView(event)
            event.response.body = JSON.stringify(event.view)
        }
    } catch (error) {
        logger.error('error:middleware:apply-policy', error)
    }
    return event
}
