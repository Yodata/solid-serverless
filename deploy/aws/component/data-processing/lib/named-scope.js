const mode = {
	append: 'Append',
	read: 'Read',
	write: 'Write',
	control: 'Control',
	subscribe: 'Subscribe'
}

const NAMED_SCOPES = {
	DogManager: {
		accessTo: '/data/dogs',
		mode: [mode.read, mode.write, mode.control],
		scope: [
			{
				type: 'DataPolicy',
				name: 'DogManager',
				processor: 'JsonSchema',
				object: {
					properties: {
						type: {
							const: 'Dog'
						}
					}
				}

			}
		]
	},
	RealEstateAgentPublic: {
		allow: [
			{
				processor: 'JsonSchema',
				value: {
					properties: {
						object: {
							properties: {
								type: {
									const: 'RealEstateAgent'
								}
							}
						}
					}
				}
			}
		]
	}
}

exports.namedScopes = NAMED_SCOPES
