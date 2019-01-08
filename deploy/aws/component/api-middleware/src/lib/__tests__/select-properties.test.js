/* eslint-disable no-undef */
const select = require('../select-properties')
test('selects, properties', () => {
	const data = {a: {b: 1, c: 1, d:1}, b: 1}
	const selector = select('a','b,d')
	const result = selector(data)
	expect(result).toEqual({
		a: {
			b: 1,
			d: 1
		},
		b: 1
	})
})

test('select.deep', () => {
	const data = {a: {b: {c:1, d:1, e:1}}}
	const key = 'a.b'
	const props = 'c'
	const fn = select(key,props)
	const result = fn(data)
	expect(result).toEqual({
		a: {
			b: {
				c: 1
			}
		}
	})
})

test('missing.props do not mutate data', () => {
	const data = {a: {b: {c:1, d:1, e:1}}}
	const key = 'a.b.f'
	const props = 'a'
	const fn = select(key,props)
	const result = fn(data)
	expect(result).toEqual(data)
})

test('does not mutate source', () => {
	const data = {a: {b: {c:1,d:1}}}
	const fn = select('a.b', 'c')
	const result = fn(data)
	expect(data).toEqual({a: {b: {c:1,d:1}}})
	expect(result).toEqual({a: {b: {c:1}}})
})

