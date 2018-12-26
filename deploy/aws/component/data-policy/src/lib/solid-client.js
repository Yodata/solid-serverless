const solid = require('@yodata/solid-tools')
const token = process.env.CLIENT_ID_TOKEN
const client = solid.client(token)

module.exports = client