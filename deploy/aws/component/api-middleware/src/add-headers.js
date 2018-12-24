/**
 * todo: add-headers
 * @param {object} event
 * @async
 * @returns {object} event
 */
module.exports = async (event) => {
    // set(response,['headers','link'], '<https://yodata.me/yodata/real-estate/context.json>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"')
    // set(response,['headers','x-powered-by'], 'yodata.me <https://yodata.me>')
    return event
}