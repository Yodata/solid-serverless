const {namedScopes} = require('./named-scope')
const got = require('got')

const createAcl = (agent, instrument, scopeName) => {
	const acl = namedScopes(scopeName)
		.agent = agent
			.instrument = instrument
}

const createAclGenerator = me => (scopeName, agent, instrument) => {
	const ns = name => me + name
	const acl = namedScopes[scopeName]
	acl.agent = agent
	acl.instrument = instrument
	acl.accessTo = ns(acl.accessTo)
	return acl
}

const createClient = baseUrl => got.extend({
	baseUrl,
	headers: {
		'x-api-key': 'dave-admin'
	}
})

exports.createAcl = createAcl
exports.createAclGenerator = createAclGenerator
exports.createClient = createClient
