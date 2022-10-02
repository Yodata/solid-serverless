const getCommonPrefix = require('../get-common-prefix')
const getPathFromDate = require('../get-path-from-date')

const path = dateString => getPathFromDate(new Date(dateString).toJSON())

test('different years returns empty string', () => {
	const start = path('2015/12/31')
	const end = path('2016/01/01')
	expect(getCommonPrefix(start, end)).toEqual('')
})

test('month', () => {
	const start = path('2015/01/01')
	const end = path('2015/02/01')
	expect(getCommonPrefix(start, end)).toEqual('2015/')
})

test('day', () => {
	const start = path('2015/01/01')
	const end = path('2015/01/02')
	expect(getCommonPrefix(start, end)).toEqual('2015/01/')
})

test('hour', () => {
	const start = path('2015-01-01T00:00')
	const end = path('2015-01-01T01:00')
	expect(getCommonPrefix(start, end)).toEqual('2015/01/01/')
})

test('minutes', () => {
	const start = path('2015-01-01T00:00:00.000Z')
	const end = path('2015-01-01T00:30:00.000Z')
	expect(getCommonPrefix(start, end)).toEqual('2015/01/01/00/')
})
