const solid = require('@yodata/solid-tools')
const logger = require('@yodata/logger')

const token = process.env.CLIENT_ID || process.env.SOLID_KEY || process.env.SERVICE_KEY

if (!token) {
	const message = 'MISSING_REQUIRED_ENV_VAR_CLIENT_ID'
	logger.error({ message })
	throw new Error(message)
}

const client = solid.client(token)
module.exports = client
