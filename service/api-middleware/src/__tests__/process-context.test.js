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


  test('if context, apply it', async () => {
    const event = require('../example/process-context-event')
    const eventData = require('../example/process-context-data')
    const resultData = require('../example/process-context-result')
    expect(event).toHaveProperty('hasData', true)
    expect(event).toHaveProperty('object', eventData)
    expect(event).toHaveProperty('stage', 'request')
    const result = await processContext(event)
    expect(result).toHaveProperty('request')
    expect(result).toHaveProperty('hasData', true)
    expect(result).toHaveProperty('stage', 'request')
    expect(result).toHaveProperty('object')
    expect(result).toHaveProperty('object.recipient')
  })

})
