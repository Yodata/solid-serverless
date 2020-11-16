

async function processRequest(event, context = {}) {
	return context.response || event
}


module.exports = jest.fn(processRequest)