const url = require('url')

/**
 * takes a container url and returns the path of the timestamp index from the storage bucket root.
 * @param {string<uri>} target - url to target container @example https://bob.example.com/inbox/
 * @returns {string} container path (prefix) @example entities/bob.example.com/data/by-ts/inbox/
 */
function getPathFromTarget (target) {
	const { hostname, pathname } = new url.URL(target)
	return `entities/${hostname}/data/by-ts${pathname}`
}

module.exports = getPathFromTarget
