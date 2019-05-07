import { HTTP } from '@/router/http'

// initial state
const state = {
	providers: []
}

const getters = {
	providers: state => state.providers
}

const actions = {
	postProvider ({ dispatch }, params) {
		const albumID = params.albumID
		let query = ''
		for (var key in params.query) {
			query += `${key}=${params.query[key]}&`
		}
		return HTTP.post('/albums/' + albumID + '/reportprovider', query, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			console.log(res)
		}).catch(res => {
			console.log(res)
		})
	}
}

const mutations = {
}

export default {
	state,
	getters,
	actions,
	mutations
}
