'use strict';


/**
 * @typedef AuthorizationType
 * @property {string} agent - the person granted access to the resource
 * @property {string} accessTo - the URI of the resource the agent is granted access to
 * @property {string[]} mode - Read, Write, Update, Control, Subscribe
 * @property {string} [authorization.instrument]
 * @property {AuthorizationScope} authorzation.scope
 */

/**
 * Represents an ACL instance
 */
class Authorization {
    /** 
     * Create an Authorziation
     * @param {AuthorizationType} object
    */
    constructor(object = {}) {
        this.agent = object.agent
        this.accessTo = object.accessTo
        this.mode = object.mode
        this.scope = object.scope
    }
}

module.exports = Authorization;
