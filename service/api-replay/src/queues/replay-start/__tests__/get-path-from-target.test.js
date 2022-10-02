const getPathFromTarget = require('../get-path-from-target')
test('returns the prefix from the target host and path', () => {
	const target = 'https://example.com/event/topic/realestate/listing/'
	expect(getPathFromTarget(target)).toBe('entities/example.com/data/by-ts/event/topic/realestate/listing/')
})
