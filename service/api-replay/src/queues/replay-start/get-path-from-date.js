
const year = date => date.getUTCFullYear().toString()
const month = date => (date.getUTCMonth() + 1).toString().padStart(2, '0')
const day = date => date.getUTCDate().toString().padStart(2, '0')
const hour = date => date.getUTCHours().toString().padStart(2, '0')
const minute = date => date.getUTCMinutes().toString().padStart(2, '0')

function getPathFromDate (dateTimeString) {
	const date = new Date(dateTimeString)
	const path = [
		year(date),
		month(date),
		day(date),
		hour(date),
		minute(date) + '/'
	].join('/')
	return path
}

module.exports = getPathFromDate
