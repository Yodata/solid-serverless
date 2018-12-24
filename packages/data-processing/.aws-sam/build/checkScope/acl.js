const { AUTHORIZATION, DEFAULT_CONTEXT} = require('./constants')

/** @enum {string} */
 const ACL_MODE = {
     READ: 'Read',
     WRITE: 'Write',
     CONTROL: 'Control',
     SUBSCRIBE: 'Subscribe'
 }


/** 
 * Class for working with ACLs
 * @namespace ACL
 * @typedef {string} URI
 * @typedef Authorization
 * @property {string} type - 
 * @property {URI} id - the URI of the Authorization.  @example https://dave.yodata.me/share/.acl#max
 * @property {ACL_MODE[]} - the level of access being granted {@link ACL_MODE}
 * @property {URI} accessTo -the URI of the resource being shared. @example https://dave.yodata.me/share/
 * @property {URI} agent - the URI of the entity receiving access to the resource.
 * @property {URI} agentClass - the URI of a resource that hosts the group (Role) with access.
 * @property {URI} [default] - the URI on an LdpContainer on which this ACL will be used for resources that do not have an .acl
 * 
 */
exports.ACL = class ACL {
    /**
     * An acl:Authorization class
     * @constructor
     * @param {string} accessTo - the URI of the resource on which the ACL will be applied
     * @param {ACL_MODE[]} mode - must be a valid ACL_NODE {@link ACL_MODE}
     * @returns {Authorization}
     */
    constructor(accessTo, mode) {
        this.type = AUTHORIZATION
        this.accessTo = accessTo
        this.mode = mode
    }

    static fromJSON(string) {
        return this.fromObject(JSON.parse(string))
    }

    static fromObject(object) {
        return Object.assign(new ACL(), object)
    }

    toString() {
        return JSON.stringify({...this})
    }

    toJSON() {
        return {...this}
    }
    /**
     * adds the provided '@context' or {@link DEFAULT_CONTEXT}
     * @param {URI} context
     * @returns {object} - a JSON-LD object.
     */
    toJSONLD(context = DEFAULT_CONTEXT) {
        return {'@context': context, ...this}
    }
}
