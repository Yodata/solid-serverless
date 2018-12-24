'use strict';

const logger = require('..')

describe('@yodata/solid-serverless-logger', () => {
    test('exports', () => {
        expect(logger).toHaveProperty('logger')
        expect(logger).toHaveProperty('createLogger')
    })
});
