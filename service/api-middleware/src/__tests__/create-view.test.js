/* eslint-disable no-undef */

const createView = require('../create-view')

describe('api-middleware.create-view', () => {

  test('if no context, data returned as-is', async () => {
    const event = {
      stage: 'request',
      hasData: true,
      object: {
        "type": 'test'
      }
    }
    const result = await createView(event)
    return expect(result).toEqual(event.object)
  })
})
