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
					provider.stateURL = {}
					provider.stateURL.loading = true
					provider.stateURL.checkURL = false
					params.providers.push(provider)
				})
				commit('SET_PROVIDERS', params)
				dispatch('setCheckURLProviders', params)
			}
			return res
		}).catch(err => {
			return err
		})
	},
	getProvider ({ commit, dispatch }, params) {
		params.providers = []
		const albumID = params.albumID
		const clientID = params.clientID
		return HTTP.get(`/albums/${albumID}/reportproviders/${clientID}`, '', { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 200) {
				params.provider = res.data
				params.provider.stateURL = {}
				params.provider.stateURL.loading = true
				params.provider.stateURL.checkURL = false
				commit('SET_PROVIDER', params)
				dispatch('setCheckURLProvider', { provider: params.provider, commit: 'UPDATE_PROVIDER' })
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
			return res
		}).catch(err => {
			return err
		})
	},
	deleteProvider ({ dispatch }, params) {
		const albumID = params.albumID
		const clientID = params.clientID
		return HTTP.delete(`/albums/${albumID}/reportproviders/${clientID}`, '', { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
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
			return res
		}).catch(err => {
			return err
		})
	},
	setCheckURLProvider ({ commit }, params) {
		let query = `url=${params.provider.url}`
		return HTTP.post('/reportproviders/testuri', query, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			params.provider.stateURL.loading = false
			params.provider.stateURL.checkURL = (res.status === 200)
			commit(params.commit, { provider: params.provider })
			return res
		}).catch(err => {
			params.provider.stateURL.loading = false
			params.provider.stateURL.checkURL = false
			commit(params.commit, { provider: params.provider })
			return err
		})
	},
	setCheckURLProviders ({ commit, dispatch }, params) {
		params.providers.forEach(provider => {
			dispatch('setCheckURLProvider', { provider: provider, commit: 'UPDATE_PROVIDERS' })
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
	INIT_PROVIDERS (state) {
		state.providers = []
	},
	UPDATE_PROVIDER (state, params) {
		state.provider.stateURL.loading = params.provider.stateURL.loading
		state.provider.stateURL.checkURL = params.provider.stateURL.checkURL
	},
	UPDATE_PROVIDERS (state, params) {
		state.providers.forEach(provider => {
			if (provider.client_id === params.provider.client_id) {
				provider.stateURL.loading = params.provider.stateURL.loading
				provider.stateURL.checkURL = params.provider.stateURL.checkURL
			}
		})
	},
	ADD_PROVIDER (state, params) {
		state.providers.push(params.provider)
	},
	SET_PROVIDER (state, params) {
		state.provider = params.provider
	},
	SET_PROVIDERS (state, params) {
		state.providers = params.providers
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
