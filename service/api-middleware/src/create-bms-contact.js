// @ts-nocheck


const logger = require('./lib/logger')
const request = require('request');
// @ts-ignore
const { CREATE_BMS_CONTACT: API_URL } = process.env;
const POST = "post";
const path = "/api/contact/create";
const createContact = (data) => {
    return new Promise((resolve) => {
        request.post(API_URL, {
            json: data
        }, (error, res, body) => {
			return resolve({error, res, body});
        });
    })
}
const matchPath = (url = '', subStr) => new URL(url).pathname == subStr;
const isOkay = ({ request }) => ( request && ((request.method || '').toLowerCase() == POST) && 
				matchPath(request.url, path) );
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
	try {
		if (isOkay(event)) {
			logger.debug('api-middleware:create-bms-contact:received', {event});
			let { body: result = {}, error } = await createContact(event);
			if (!result.id) {
				event.object = error
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
		logger.debug('api-middleware:create-bms-contact:result', {event})
	} catch (error) {
		logger.debug("error::creating:contact", JSON.stringify(error));
	}
	return event;
}