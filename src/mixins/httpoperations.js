export default {
	getObjectParameters: function (queries) {
		let tabQueries = []
		for (var key in queries) {
			if (typeof queries[key] !== 'function' && typeof queries[key] !== 'object') {
				tabQueries.push(`${encodeURIComponent(key)}=${encodeURIComponent(queries[key])}`)
			} else if (typeof queries[key] !== 'function' && queries[key].constructor === Array) {
				queries[key].forEach(value => {
					tabQueries.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
				})
			}
		}
		return tabQueries
	},
	getQueriesParameters: function (queries) {
		let tabQueries = this.getObjectParameters(queries)
		let stringQueries = tabQueries.length > 0 ? '?' + tabQueries.join('&') : ''
		return stringQueries
	},
	getFormData: function (queries) {
		let tabQueries = this.getObjectParameters(queries)
		let stringQueries = tabQueries.length > 0 ? '' + tabQueries.join('&') : ''
		return stringQueries
	}
}
