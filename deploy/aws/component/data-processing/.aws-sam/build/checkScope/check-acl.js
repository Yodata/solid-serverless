
/**
 * tests an http request and ACL
 * @param {Object}  event
 * @param {Object}  event.request - The incoming http request
 * @param {string}  event.request.method - The request method as a string. Read only. Examples: 'GET', 'DELETE'
 * @param {string}  event.request.path - The pathname of the resource to be accessed
 * @param {Object}  event.request.headers - Key-value pairs of header names and values. Header names are lower-cased.
 * @param {string}  event.request.body - A JSON string of the request payload.
 * 
 * @param {string}  event.agent - ProfileURI of the request agent, who is MAKING the request.  Examples: 'https://smarteragent.yodata.me/profile/card#me' 
 * @param {string}  event.instrument - Identifies the software or service who is SENDING the request. Tied to the request credentials
 * @param {Array<Object>}  event.acl - ACLs to be tested
 * 
 */
exports.post = async (event, awsLambdaContext) => {
    // todo: use solid-acl-check to confirm our acls work with solid check-acl
    // for testing: event.agent = GoodDog is allowed, BadDog is denied.
    return event.agent === 'GoodDog' ? true : false
};