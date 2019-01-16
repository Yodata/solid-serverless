const RESPONSE = 'response'
const REQUEST = 'request'

const requestOrResponse = (event) => {
	return (typeof event[RESPONSE] === 'object') ? RESPONSE : REQUEST
}

module.exports= requestOrResponse