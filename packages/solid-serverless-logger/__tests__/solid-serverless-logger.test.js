/* eslint-disable no-undef */
'use strict'

const logger = require('../')

describe('@yodata/solid-serverless-logger', () => {
	test('exports', () => {
		expect(logger).toHaveProperty('defaultLogger')
		expect(logger).toHaveProperty('createLogger')
	})
	test('doesn\'t crash', () => {
		const log = logger.defaultLogger
		expect(() => log.info('message', {})).not.toThrowError()
	})
})
