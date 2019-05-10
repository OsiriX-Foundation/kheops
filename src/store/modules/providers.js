import { HTTP } from '@/router/http'

// initial state
const state = {
	providers: [],
	provider: {}
}

const getters = {
	providers: state => state.providers,
	provider: state => state.provider
}

const actions = {
	initProvider ({ commit }, params) {
		commit('INIT_PROVIDER')
	},
	getProviders ({ commit, dispatch }, params) {
		params.providers = []
		const albumID = params.albumID
		return HTTP.get('/albums/' + albumID + '/reportproviders', '', { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 200) {
				res.data.forEach(provider => {
					params.providers.push(provider)
				})
			}
			commit('SET_PROVIDERS', params)
			return res
		}).catch(err => {
			return err
		})
	},
	getProvider ({ commit }, params) {
		params.providers = []
		const albumID = params.albumID
		const clientID = params.clientID
		return HTTP.get(`/albums/${albumID}/reportproviders/${clientID}`, '', { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 200) {
				params.provider = res.data
				commit('SET_PROVIDER', params)
			}
			return res
		}).catch(err => {
			return err
		})
	},
	postProvider ({ dispatch }, params) {
		const albumID = params.albumID
		let queries = ''
		for (var key in params.query) {
			queries += `${key}=${params.query[key]}&`
		}
		return HTTP.post('/albums/' + albumID + '/reportproviders', queries, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			console.log(res)
			if (res.status === 201) {
				dispatch('getProviders', { albumID: albumID })
				return res
			}
			return res
		}).catch(err => {
			return err
		})
	},
	deleteProvider ({ dispatch }, params) {
		const albumID = params.albumID
		const clientID = params.clientID
		return HTTP.delete(`/albums/${albumID}/reportproviders/${clientID}`, '', { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 204) {
				dispatch('getProviders', { albumID: albumID })
			}
			return res
		}).catch(err => {
			return err
		})
	},
	updateProvider ({ dispatch }, params) {
		const albumID = params.paramsURL.albumID
		const clientID = params.paramsURL.clientID
		let queries = ''
		for (var key in params.query) {
			queries += `${key}=${params.query[key]}&`
		}
		return HTTP.patch(`/albums/${albumID}/reportproviders/${clientID}`, queries, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 200) {
				dispatch('getProviders', { albumID: albumID })
			}
			return res
		}).catch(err => {
			return err
		})
	},
	testURLProvider ({ dispatch }, params) {
		let query = `url=${params.url}`
		return HTTP.post('/reportproviders/testuri', query, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			return res
		}).catch(err => {
			return err
		})
	}
}

const mutations = {
	INIT_PROVIDER (state) {
		state.provider = {}
	},
	SET_PROVIDERS (state, params) {
		state.providers = params.providers
	},
	SET_PROVIDER (state, params) {
		state.provider = params.provider
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
