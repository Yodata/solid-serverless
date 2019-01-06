// @ts-check

const solid = require('@yodata/solid-tools')

const token = process.env.CLIENT_ID
if (!token) {
	const error = new Error('MISSING_REQUIRED_ENV_VAR_CLIENT_ID_TOKEN')
	throw error
}

const client = solid.client(token)

module.exports = client
