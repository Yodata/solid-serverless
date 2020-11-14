module.exports = {
	name: 'redactPasswordDataPolicy',
	description: 'changes any password keys to [PASSWORD]',
	type: 'DataPolicy',
	processor: 'Yodata',
	effect: 'Transform',
	value: JSON.stringify({
		password: {
			value: '[PASSWORD]'
		}
	})
}
