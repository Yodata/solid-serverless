const exampleDataPolicy = {
	type: 'DataPolicy',
	processor: 'Yodata',
	effect: 'Transform',
	value: JSON.stringify({
		password: {
			value: '[PASSWORD]'
		}
	})
}
