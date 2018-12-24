const {transform, toLower} = require('lodash')

/**
 * transform httpMessage.rawHeaders -> headers
 * @param {object} httpMessage
 * @param {object} httpMessage.rawHeaders
 * @param {object} [httpMessage.headers]
 * @returns {object}
 */
module.exports = (httpMessage) => {
    let headers = httpMessage.headers || httpMessage.rawHeaders || {}
    return transform(headers,(object, value, key) => {
        let K = toLower(key)
        let V
        if (Array.isArray(value)) {
            if (value.length === 1) {
                V = value[0]
            } else {
                V = value.join(',')
            }
        }
        object[K] = V
    })
}