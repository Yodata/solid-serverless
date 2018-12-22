const logger = require('./logger')


/**
 * JSONLD transform event.scope.context => yodata:real-estate/context#
 * @name check-scope
 * @param {object} event
 * @param {object} event.request - http.request
 * @param {URI}    event.agent
 * @param {URI}    event.instrument
 * @param {object} event.object   - the data to be transformed
 * @param {object} event.scope    - the ACL.scope
 * @param {object} event.policy   - from {POD}/settings/yodata/policy
 */
exports.handler = async (event) => {
    logger.info('check-scope', event)
    if (event.agent && event.agent === 'BadDog') {
        throw new Error('Bad Dog! No Data!')
    }
    let scope = event.scope || {}
    if (scope.source) {
        scope = scope.source
    }
    if (scope.hasOwnProperty('PublicDataOnly') && event.object && event.object.type === 'ForbiddenType') {
        let error = new Error('Forbidden')
        error.statusCode = 403
        throw error
    }
    return event
}