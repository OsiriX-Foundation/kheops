import { HTTP } from '@/router/http'
// initial state
const state = {
	all: [],
	current: {
		username: null,
		fullname: null,
		permissions: [],
		jwt: null,
		tokens: [],
		email: null,
		sub: null
	}
}

// getters
const getters = {
	currentUser: state => state.current
}

// actions
const actions = {
	login ({ commit }, userData) {
		return new Promise((resolve) => {
			var loggedUser = {
				permissions: userData.permissions,
				tokens: []
			}
			localStorage.setItem('currentUser', JSON.stringify(loggedUser))
			commit('LOGIN', loggedUser)
			resolve(userData)
		})
	},

	getCredentials ({ commit }) {
		if (state.current.user_id) {
			return state.current
		} else {
			let user = localStorage.getItem('currentUser')
			if (user) {
				user = JSON.parse(user)
				commit('LOGIN', user)
				return true
			} else {
				return false
			}
		}
	},
	checkPermissions (context, params) {
		let permissionsToCheck = params.permissions
		let condition = params.condition
		if (condition !== 'all') condition = 'any'
		if (!state.current.permissions) return false
		if (condition === 'any') return state.current.permissions.some(v => permissionsToCheck.includes(v))
		else if (condition === 'all') return _.difference(permissionsToCheck, state.current.permissions).length === 0
		return false
	},
	logout ({ commit }) {
		localStorage.removeItem('currentUser')
		commit('LOGOUT')
	},
	checkUser (context, user) {
		return HTTP.get('users?reference=' + user, { headers: { 'Accept': 'application/json' } }).then(res => {
			if (res.status === 200) return res.data.sub
			return false
		}).catch(() => {
			return false
		})
	},
	getUserTokens ({ commit }, params) {
		return HTTP.get(`/capabilities?valid=${!params.showInvalid}`).then(res => {
			if (res.status === 200) {
				commit('SET_TOKENS', res.data)
			}
			return res.data
		}).catch(() => {
			return false
		})
	},
	createToken ({ commit }, params) {
		var query = ''
		_.forEach(params.token, (value, key) => {
			query += encodeURIComponent(key) + '=' + encodeURIComponent(value) + '&'
		})
		return HTTP.post('/capabilities', query, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 201) {
				let token = res.data
				commit('SET_TOKEN', token)
				if (token.scope_type === 'album') commit('SET_ALBUM_TOKEN', token)
			}
			return res
		})
	},
	revokeToken ({ commit }, params) {
		if (params.token_id === undefined) return
		return HTTP.post(`/capabilities/${params.token_id}/revoke`).then(res => {
			if (res.status === 200) {
				commit('REVOKE_TOKEN', res.data)
			}
			return res
		})
	}
}

// mutations
const mutations = {
	SET_USERS (state, users) {
		state.all = users
	},
	LOGIN (state, user) {
		state.current = user
	},
	LOGOUT (state) {
		state.current = {
			user_id: null,
			username: null,
			fullname: null,
			firstname: null,
			lastname: null,
			email: null,
			jwt: null,
			permissions: null,
			tokens: []
		}
	},
	SET_TOKENS (state, tokens) {
		state.current.tokens = tokens
	},
	SET_TOKEN (state, token) {
		state.current.tokens.push(token)
	},
	REVOKE_TOKEN (state, token) {
		let idx = _.findIndex(state.current.tokens, t => { return t.id === token.id })
		if (idx > -1) {
			state.current.tokens[idx] = token
		}
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
