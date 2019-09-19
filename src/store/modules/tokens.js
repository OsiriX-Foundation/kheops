import { HTTP } from '@/router/http'

// initial state
const state = {
	token: {}
}

const getters = {
	token: state => state.token
}

const actions = {
	initToken ({ commit }, params) {
		commit('INIT_TOKEN')
	},
	getToken ({ commit }, params) {
		params.providers = []
		const capabilityId = params.capabilityId
		const request = `capabilities/${capabilityId}`
		return HTTP.get(request, '', { headers: { 'Accept': 'application/json' } }).then(res => {
			if (res.status === 200) {
				commit('CREATE_TOKEN', res.data)
			}
			return res
		}).catch(err => {
			return Promise.reject(err)
		})
	}
}

const mutations = {
	INIT_TOKEN (state) {
		state.token = {}
	},
	UPDATE_TOKEN (state, params) {
		state.provider.stateURL = params.provider.stateURL
		state.provider.data = params.provider.data
	},
	CREATE_TOKEN (state, token) {
		state.token = token
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
