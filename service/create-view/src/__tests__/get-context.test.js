const { getContext, Context, plugin } = require('@yodata/transform')

test('get-context', async () => {
	const url = 'https://subscriber.dev.yodata.io/public/context/stage/testcontext.cdef.yaml'
	const ctx = await getContext(url)
	ctx.use(plugin.defaultValues)
	expect(ctx).toBeInstanceOf(Context)
	expect(ctx.has('@view')).toBeTruthy()
	expect(ctx.plugins).toHaveProperty('size', 1)
})