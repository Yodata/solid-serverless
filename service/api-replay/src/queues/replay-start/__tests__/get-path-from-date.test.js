const getPathFromDate = require('../get-path-from-date')

test('getPathFromDate', () => {
	expect(getPathFromDate('2017-01-01T05:31:00.000Z')).toBe('2017/01/01/05/31/')
})
