const BASE_URI = process.env.BASE_URI || 'https://real-living.yodata.me'
const SCOPE_URI = process.env.SCOPE_URI || BASE_URI + '/public/yodata/scope/'
const ID = process.env.WHOAMI || 'testuser'

const client = require('./client')(ID)

const KNOWN_SCOPES = {
    'DogsOnly': {
        processor: 'Mingo',
        effect: 'Allow',
        condition: {
            type: 'Dog'
        }
    }
}

module.exports = async (scopeName) => {
    return KNOWN_SCOPES[scopeName] || client.get(SCOPE_URI + scopeName)
}