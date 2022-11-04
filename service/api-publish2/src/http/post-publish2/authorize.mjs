
/**
 *
 * @param {AWSHttpRequest} request
 * @returns
 */
export async function authorize (request) {
	const authorized = true
	// TODO: validate on...
	// 1. request host is white listed
	// 2. request IP matches allowed value for host
	// 3. x-api-key matches host key
	/** AWSHTTPRequest */
	if (authorized) { return request } else { throw new Error('NOT_AUTHORIZED') }
}

export default authorize
