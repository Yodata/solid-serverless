
const valid = { isValid: true }
const invalid = {
	isValid: false,
	error: {
		message: 'Invalid email address',
		items: []
	}
}

module.exports = {
	valid,
	invalid
}
