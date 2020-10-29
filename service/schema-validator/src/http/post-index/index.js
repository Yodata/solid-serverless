const arc = require('@architect/functions')
const Ajv = require('ajv')
const schemaParser = require('json-schema-ref-parser')
const buildResponse = (statusCode, body) => {
	console.log(JSON.stringify(body))
	return { statusCode, body }
}
const parseBody = (request, res, next) => {
	request.body = arc.http.helpers.bodyParser(request)
	next()
}
const route = async (req, res) => {
	try {
		const { event, config: { schemaURL } } = req.body
		const dRef = await schemaParser.dereference(schemaURL)
		// @ts-ignore
		const { payload: jsonSchema = dRef } = dRef
		console.log(JSON.stringify(jsonSchema))
		const ajv = new Ajv({ schemaId: 'auto' })
		const result = ajv.validate(jsonSchema, event)
		const response = Object.assign(event, {
			isValid: result
		})
		return result
			? res(buildResponse(200, JSON.stringify(response)))
			: res(buildResponse(400, JSON.stringify(Object.assign(response, {
				error: {
					errors: ajv.errors,
					message: `validationError : ${ajv.errorsText()}`
				}
			}))))
	} catch (error) {
		return res(buildResponse(500, JSON.stringify({
			error: {
				message: error.message || 'Internal Server Error',
				stack: error.stack
			}
		})))
	}
}

exports.handler = arc.http(parseBody, route)