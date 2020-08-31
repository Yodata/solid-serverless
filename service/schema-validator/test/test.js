var assert = require('assert');
const data = require('./testData')
const request = require("request");
const API_URL = 'http://localhost:3333';

const validateSchema = (dataKey) => {
    return new Promise((resolve) => {
        request.post(API_URL, {
            json: data[dataKey]
        }, (error, res, body) => {
            return resolve({error, res, body});
        });
    })
}

describe('With All Required Keys\n', function () {
    describe('With all match for validate\n', function () {
        it('should give 200 OK', async function () {
            let result = await validateSchema('successData');
            assert.equal(JSON.parse(result.res.statusCode), 200);
        });
    });
    describe('With invalid type\n', function () {
        it('should give validation error', async function () {
            let result = await validateSchema('wrongTopic');
            assert.equal(result.body.error.message, "validationError : data.topic should be equal to one of the allowed values");
        });
    });
    describe('With wrong data dot type\n', function () {
        it('should give validation error ', async function () {
            let result = await validateSchema('wrongDataDotType');
            assert.equal(result.body.error.message, "validationError : data.data.type should be equal to one of the allowed values");
        });
    });

});
describe('With some missing keys\n', function () {
    describe('Without topic\n', function () {
        it('should give validation error ', async function () {
            let result = await validateSchema('withoutTopic');
            assert.equal(result.body.error.message, "validationError : data should have required property 'topic'");
        });
    });
    describe('Without source\n', function () {
        it('should give validation error ', async function () {
            let result = await validateSchema('withoutSource');
            assert.equal(result.body.error.message, "validationError : data should have required property 'source'");
        });
    });
    describe('Without data\n', function () {
        it('should give validation error ', async function () {
            let result = await validateSchema('withoutData');
            assert.equal(result.body.error.message, "validationError : data should have required property 'data'");
        });
    });
    describe('Without data dot type\n', function () {
        it('should give validation error ', async function () {
            let result = await validateSchema('withoutDataDotType');
            assert.equal(result.body.error.message, "validationError : data.data should have required property 'type'");
        });
    });
});
