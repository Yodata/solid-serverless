const { getItemId, createItemReducer } = require('../process-replay-request')

test('getItemId', () => {
	const item = {
		Key: 'entities/bob.example.com/data/by-id/inbox/1000/01/01/xxx'
	}
	expect(getItemId(item)).toEqual('xxx')
})

test('createItemReducer', () => {
	const a = '/entities/bob.example.com/data/by-id/inbox/2020/01/01/00/00/00/00/000/a'
	const b = '/entities/bob.example.com/data/by-id/inbox/2020/01/01/00/00/01/000/b'
	const c = '/entities/bob.example.com/data/by-id/inbox/2020/01/01/00/00/02/000/c'
	const d = '/entities/bob.example.com/data/by-id/inbox/2020/01/01/00/00/03/000/d'
	const contents = [
		{ Key: a },
		{ Key: b },
		{ Key: c },
		{ Key: d }
	]
	const endPath = c
	const reducer = createItemReducer(endPath)
	expect(reducer).toBeInstanceOf(Function)
	const result = contents.reduce(reducer, [])
	expect(result).toHaveLength(2)
	expect(result[0]).toEqual('a')
	expect(result[1]).toEqual('b')
})
