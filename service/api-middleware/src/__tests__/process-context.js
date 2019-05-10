/* eslint-disable no-undef */

const processContext = require('../process-context')

describe('api-middleware.process-context', () => {

  test('if no context, data returned as-is', async () => {
    const event = {
      stage: 'request',
      hasData: true,
      object: {
        "type": 'test'
      }
    }
    const result = await processContext(event)
    return expect(result.object).toEqual(event.object)
  })
})
