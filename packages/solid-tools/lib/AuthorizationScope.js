const mingo = require('mingo')

const Processors = {
    'Mingo': function(condition) {
        let query = new mingo.Query(condition)
        return {
            test: (value) => query.test(value)
        }
    }
}

/**
 * @typedef AuthorizationScope
 * @property {string} effect - Allow, Deny
 * @property {string} processor
 * @property {object} value
 */

/** Respresents and AuthorizationScope */
class AuthorizationScope {
    /**
     * Create an AuthorizationScope
     * @param {object} object
     * @param {string} object.processor - Mingo (default), JsonSchema
     * @param {string} object.effect - Allow (default), Deny
     * @param {object} object.condition - object passed to the processor
     */
    constructor(object = {}) {
        this.effect = object.effect || 'Allow'
        this.processor = object.processor || 'Mingo'
        if (!Processors.hasOwnProperty(this.processor)) {
            throw new Error('INVALID_SCOPE_PROCESSOR', this.processor)
        }
        if (!object.condition) {
            throw new Error ('SCOPE_CONDITION_REQUIRED')
        }
        this.condition = object.condition
        this.validator = Processors[this.processor](this.condition)
    }

    /** 
     * true if the value matches the condition
     * @param {*} value
     * @returns {boolean} - true if the value matches the condition
     */
    matches(value) {
        return this.validator.test(value)
    }

    /**
     * tests the value against the condition & effect
     * @param {object} value - the value to check
     * @returns {boolean}
     */
    isAllowed(value) {
        let matches = this.matches(value)
        return ( this.effect === 'Allow' ) ? matches : !matches
    }
}

module.exports = AuthorizationScope


