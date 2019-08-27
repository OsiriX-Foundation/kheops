import { HTTP } from '@/router/http'
import httpoperations from '@/mixins/httpoperations'
// initial state
const state = {
	album: {},
	users: []
}

// getters
const getters = {
	albumTest: state => state.album,
	usersTest: state => state.users
}

// actions
const actions = {
	getAlbumTest ({ commit }, params) {
		let request = `albums/${params.album_id}`
		return HTTP.get(request, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_ALBUM_TEST', res.data)
			return res
		}).catch(err => {
			return err
		})
	},
	removeStudyInAlbum ({ commit }, params) {
		let request = `studies/${params.StudyInstanceUID}/albums/${params.album_id}`
		return HTTP.delete(request).then(res => {
			if (res.status === 204) {
				commit('DELETE_STUDY_TEST', params)
			}
			return res
		}).catch(err => {
			return err
		})
	},
	removeSerieInAlbum ({ commit }, params) {
		let request = `studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/albums/${params.album_id}`
		return HTTP.delete(request).then(res => {
			if (res.status === 204) {
				commit('DELETE_SERIE_TEST', params)
			}
			return res
		}).catch(err => {
			return err
		})
	},
	getUsersAlbum ({ commit }, params) {
		const request = `albums/${params.album_id}/users`
		return HTTP.get(request, '', { headers: { 'Accept': 'application/json' } }).then(res => {
			if (res.status === 200) {
				commit('SET_ALBUM_USERS', res.data)
			}
		})
	},
	editAlbum ({ commit }, params) {
		const request = `albums/${params.album_id}`
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.patch(request + queries, '', { headers: { 'Accept': 'application/json' } }).then(res => {
			return res
		}).catch(err => {
			return err
		})
	},
	deleteAlbumTest ({ commit }, params) {
		const request = `albums/${params.album_id}`
		return HTTP.delete(request).then(res => {
			if (res.status === 204) {
				commit('INIT_ALBUM')
				commit('INIT_ALBUM_USERS')
			}
		})
	},
	addAlbumUser ({ commit }, params) {
		const request = `albums/${params.album_id}/users/${params.user}`
		return HTTP.put(request).then(res => {
			return res
		}).catch(err => {
			return err
		})
	},
	removeAlbumUser ({ commit }, params) {
		const request = `albums/${params.album_id}/users/${params.user}`
		return HTTP.delete(request).then(res => {
			return res
		}).catch(err => {
			return err
		})
	},
	manageAlbumUserAdmin ({ commit }, params) {
		const request = `albums/${params.album_id}/users/${params.user_name}/admin`
		let method = (params.user_is_admin) ? 'put' : 'delete'
		return HTTP[method](request).then(res => {
			return res
		}).catch(err => {
			return err
		})
	}
}

// mutations
const mutations = {
	INIT_ALBUM (state) {
		state.album = {}
	},
	SET_ALBUM_TEST (state, album) {
		state.album = album
	},
	INIT_ALBUM_USERS (state) {
		state.users = []
	},
	SET_ALBUM_USERS (state, users) {
		state.users = users
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
