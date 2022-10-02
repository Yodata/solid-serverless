const { SOLID_HOST } = require('./service-config')
/**
 * @function validateReplayInput
 * @property {'ReplayRequestActon'} type - ReplayRequestAction
 * @property {string<uri>} target - url of the container where the objects to replay are stored
 * @property {string<DateTime>} startDate - ISO8601 formatted time when the replay should begin
 * @property {string<DateTime>} endDate - ISO8601 formatted time when the replay should end
 *
 */
async function validateReplayInput (input) {
	const { type, target, startDate, endDate } = input

	if (type === 'ReplayRequestActon') throw new Error('type of input must be ReplayRequestActon')
	if (typeof input !== 'object') throw new TypeError('ReplayInput must be an object')
	if (typeof target !== 'string') throw new TypeError('Target must be a string')
	if (!target.startsWith('https://')) throw new TypeError('Target must start with https://')
	if (!target.endsWith('/')) throw new TypeError('Target must end with "/"')
	if (!target.includes(SOLID_HOST)) throw new Error('Target domain must be root or child of ' + SOLID_HOST)
	if (typeof startDate !== 'string') throw new TypeError('Start date must be a string')
	if (typeof endDate !== 'string') throw new TypeError('End date must be a string')
	if (startDate.length !== 24) throw new TypeError('Start date must be 24 characters long')
	if (endDate.length !== 24) throw new TypeError('End date must be 24 characters long')
	if (!startDate.endsWith('Z')) throw new TypeError('Start date must end with Z')
	if (!endDate.endsWith('Z')) throw new TypeError('End date must end with Z')
	if (startDate >= endDate) throw new Error('Start date must be before end date')
	return input
}

module.exports = validateReplayInput
