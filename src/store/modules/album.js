import { HTTP } from '@/router/http'
// initial state
const state = {
	album: {
		album_id: '',
		name: '',
		description: '',
		modalities: [],
		created_time: '',
		last_event_time: '',
		number_of_users: '',
		number_of_comments: 0,
		number_of_studies: 0,
		add_user: false,
		download_series: true,
		send_series: true,
		delete_series: false,
		add_series: true,
		write_comments: true,
		is_favorite: false,
		notification_new_series: true,
		notification_new_comment: true,
		is_admin: true,
		users: []
	},
	users: [],
	comments: []

}

// getters
const getters = {
	album: state => state.album,
	users: state => state.users,
	albumComments: state => state.comments
}

// actions
const actions = {

	getAlbum ({ commit }, params) {
		return HTTP.get('album/' + params.album_id, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_ALBUM', res.data)
		})
	},
	toggleFavorite () {
	},
	patchAlbum ({ commit }, params) {
		var query = ''
		_.forEach(params, (value, key) => {
			query += encodeURIComponent(key) + '=' + encodeURIComponent(value) + '&'
		})
		return HTTP.patch('/album/' + state.album.album_id, query, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 200) {
				commit('PATCH_ALBUM', params)
			}
		})
	},
	getUsers ({ commit }) {
		return HTTP.get('/album/' + state.album.album_id + '/users', { headers: { 'Accept': 'application/json' } }).then(res => {
			if (res.status === 200) {
				commit('SET_USERS', res.data)
			}
		})
	},
	add_user_to_album ({ commit }, params) {
		return HTTP.put('/album/' + state.album.album_id + '/users/' + params.user_name).then(res => {
			if (res.status === 201) {
				commit('ADD_USER', { user_name: params.user_name, is_admin: false })
			}
		})
	},
	remove_user_from_album ({ commit }, params) {
		return HTTP.delete('/album/' + state.album.album_id + '/users/' + params.user_name).then(res => {
			if (res.status === 204) {
				commit('DELETE_USER', { user_name: params.user_name })
			} else console.log(res.status)
		})
	},
	toggleAlbumUserAdmin ({ commit }, user) {
		let method = (user.is_admin) ? 'put' : 'delete'
		return HTTP[method]('/album/' + state.album.album_id + '/users/' + user.user_name + '/admin').then(res => {
			if (res.status === 204) {
				commit('TOGGLE_USER_ADMIN', user)
			}
		})
	},
	deleteAlbum ({ commit }) {
		return HTTP.delete('/album/' + state.album.album_id).then(res => {
			if (res.status === 204) {
				commit('DELETE_ALBUM')
			}
		})
	},
	getAlbumComments ({ commit }, params) {
		let query = ''
		if (params.type) query = '?types=' + params.type

		return HTTP.get('/album/' + state.album.album_id + '/events' + query).then(res => {
			if (res.status === 200) {
				commit('SET_COMMENTS', res.data)
			}
		})
	},
	postAlbumComment ({ dispatch }, params) {
		var query = ''
		_.forEach(params, (value, key) => {
			if (value) query += encodeURIComponent(key) + '=' + encodeURIComponent(value) + '&'
		})
		return HTTP.post('/album/' + state.album.album_id + '/comments', query, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 204) {
				dispatch('getAlbumComments')
			} else return res
		})
	}

}

// mutations
const mutations = {
	SET_ALBUM (state, data) {
		state.album = data
	},
	PATCH_ALBUM (state, params) {
		_.forEach(params, (v, k) => {
			state.album[k] = v
		})
	},
	SET_USERS (state, users) {
		state.users.splice(0)
		_.forEach(users, u => {
			state.users.push(u)
		})
	},
	ADD_USER (state, user) {
		state.users.push(user)
	},
	DELETE_USER (state, params) {
		let idx = _.findIndex(state.users, function (u) {
			return u.user_name === params.user_name
		})
		if (idx > -1) {
			state.users.splice(idx, 1)
		}
	},
	TOGGLE_USER_ADMIN (state, user) {
		let idx = _.findIndex(state.users, function (u) {
			return u.user_name === user.user_name
		})
		if (idx > -1) {
			state.users.splice(idx, 1, user)
		}
	},
	DELETE_ALBUM (state) {
		state.album.album_id = ''
		state.album.name = ''
		state.album.description = ''
		state.album.modalities = []
		state.album.created_time = ''
		state.album.last_event_time = ''
		state.album.number_of_users = ''
		state.album.number_of_comments = 0
		state.album.number_of_studies = 0
		state.album.add_user = false
		state.album.download_series = true
		state.album.send_series = true
		state.album.delete_series = false
		state.album.add_series = true
		state.album.write_comments = true
		state.album.is_favorite = false
		state.album.notification_new_series = true
		state.album.notification_new_comment = true
		state.album.is_admin = true
		state.users = []
		state.comments = []
		state.mutations = []
	},
	SET_COMMENTS (state, data) {
		state.comments.splice(0)
		_.forEach(data, d => {
			state.comments.unshift(d)
		})
	}

}

export default {
	state,
	getters,
	actions,
	mutations
}
