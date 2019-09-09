const createView = require('..').handler

test('fetch-json-value', async () => {
	const object = {
		type: 'RealEstateOffice',
		id: 'https://ca301-001.bhhs.hsfaffiliates.com/profile/card#me',
		parentOrganization: [ 'https://ca301.bhhs.hsfaffiliates.com/profile/card#me' ]
	}
	const context = {
		'@view': {
			'type': 'type',
			'parentName': '"$fetchjsonvalue(" & parentOrganization[0] & ",name)"'
		}
	}
	const result = await createView({ object, context })
	expect(result).toHaveProperty('parentName', 'California Properties')
})