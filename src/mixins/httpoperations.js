export default {
	getQueriesParameters: function (queries) {
		let tabQueries = []
		for (var key in queries) {
			if (typeof queries[key] !== 'function' && typeof queries[key] !== 'object') {
				tabQueries.push(`${key}=${queries[key]}`)
			} else if (typeof queries[key] !== 'function' && queries[key].constructor === Array) {
				queries[key].forEach(value => {
					tabQueries.push(`${key}=${value}`)
				})
			}
		}
		let stringQueries = tabQueries.length > 0 ? '?' + tabQueries.join('&') : ''
		return stringQueries
	}
}
