import { HTTP } from '@/router/http'
import httpoperations from '@/mixins/httpoperations'
import Vue from 'vue'

// initial state
const state = {
	comments: {}
}

// getters
const getters = {
	comments: state => state.comments,
	getCommentsByUID: state => (uid) => {
		if (state.comments[uid] !== undefined) {
			return state.comments[uid]
		} else {
			return []
		}
	}
}

// actions
const actions = {
	getStudyComments ({ commit, dispatch }, params) {
		const request = `studies/${params.StudyInstanceUID}/comments`
		let queries = ''

		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}

		return HTTP.get(`${request}${queries}`, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_COMMENTS_TEST', { StudyInstanceUID: params.StudyInstanceUID, comments: res.data.reverse() })
			return res
		}).catch(err => {
			console.log(err)
		})
	},
	postStudyComment ({ dispatch }, params) {
		const request = `studies/${params.StudyInstanceUID}/comments`
		let queries = ''

		if (params.queries !== undefined) {
			queries = httpoperations.getFormData(params.queries)
		}
		return HTTP.post(request, queries, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			return res
		}).catch(err => {
			return err
		})
	},
	getAlbumComments ({ commit, dispatch }, params) {
		const request = `albums/${params.album_id}/events`
		let queries = ''

		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(`${request}${queries}`, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_COMMENTS_TEST', { StudyInstanceUID: params.album_id, comments: res.data.reverse() })
			return res
		}).catch(err => {
			console.log(err)
		})
	},
	postAlbumComment ({ commit, dispatch }, params) {
		const request = `albums/${params.album_id}/comments`
		let queries = ''

		if (params.queries !== undefined) {
			queries = httpoperations.getFormData(params.queries)
		}
		return HTTP.post(request, queries, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			return res
		}).catch(err => {
			return err
		})
	}
}

// mutations
const mutations = {
	INIT_COMMENTS_TEST (state) {
		state.comments = {}
	},
	SET_COMMENTS_TEST (state, params) {
		Vue.set(state.comments, params.StudyInstanceUID, params.comments)
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
