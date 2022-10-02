
/**
 * @description takes two path segments and returns the last common segment as a path string
 * @param {*} startPath the start path
 * @param {*} endPath - the end path
 */
function getCommonPrefix (startPath, endPath) {
	const start = startPath.split('/')
	const end = endPath.split('/')
	const segments = start.length - 1
	let index = 0
	let done = false
	const result = []
	while (!done) {
		done = (index > segments || (start[index] !== end[index]))
		if (done) {
			break
		} else {
			result.push(start[index])
			index++
		}
	}
	return result.length > 0 ? result.join('/') + '/' : ''
}

module.exports = getCommonPrefix
