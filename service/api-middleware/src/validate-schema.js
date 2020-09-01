// @ts-nocheck


const logger = require('./lib/logger')
const request = require('request');
// @ts-ignore
const { SCHEMA_VALIDATOR_URL: API_URL = 'http://localhost:3333' } = process.env;
const POST = "post";
const path = "/publish/";
const topic = "realestate/franchise#transactionreport";
const fs = require("fs");
// @ts-ignore
const schemaURLs = JSON.parse(fs.readFileSync('./schemaByTopic.json'));
const getConfig = ({ topic }) => ({
	schemaURL: schemaURLs[topic]
})
const preparePayload = ({ object }) => ({
		event: object,
		config: getConfig(object)
})
const validateSchema = (data) => {
    return new Promise((resolve) => {
        request.post(API_URL, {
            json: data
        }, (error, res, body) => {
            return resolve({error, res, body});
        });
    })
}
const matchPath = (str = '', subStr) => str.includes(subStr);
const isOkay = ({ request, hasData, object }) => ( request && (request.method.toLowerCase() == POST) && 
				matchPath(request.url, path) && hasData && object && object.topic == topic);
/**
 * checks event using event.scope, adds event.isAllowed {boolean}
 * @param {object} 	event
 * @param {object} 	event.object
 * @param {object} 	event.scope
 * @param {boolean}	event.isAllowed
 * @param {object}	[event.request]
 * @param {object}	[event.response]
 * @returns {Promise<object>}
 */

module.exports = async (event) => {
	// @ts-ignore
	if (isOkay(event)) {
		logger.debug('api-middleware:validate-schema:received', {event});
		let { body: result, error } = await validateSchema(preparePayload(event));
		if (error || !result.isValid) {
			event.object = error || result.error
			event.response = {
				status: '400',
				headers: {
					"Content-Type": "application/json"
				},
				statusCode: 400,
				end: true,
				body: Buffer.from(JSON.stringify(event.object)).toString('base64')
			}
		}
	}
	logger.debug('api-middleware:schema-validation:result', {event})
	return event
}