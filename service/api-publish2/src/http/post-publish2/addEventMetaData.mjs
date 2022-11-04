
export async function addEventMetaData (request) {
	const { body: event, requestContext: { requestId, timeEpoch, domainName } } = request
	event.id = requestId
	event.agent = `https://${domainName}/profile/card#me`
	event.time = new Date(timeEpoch).toISOString()
	event.timestamp = timeEpoch
	if (event.recipient && !event.originalRecipient) {
		event.originalRecipient = event.recipient
	}
	return event
}
export default addEventMetaData
