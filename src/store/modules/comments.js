import { HTTP } from '@/router/http'
import dicomoperations from '@/mixins/dicomoperations'
import httpoperations from '@/mixins/httpoperations'
import SRImage from '@/assets/SR_2.png'
import PDFImage from '@/assets/pdf-240x240.png'
import VideoImage from '@/assets/video.png'
import DicomLogo from '@/assets/dicom_logo.png'
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
	getCommentsStudy ({ commit, dispatch }, params) {
		const request = `studies/${params.StudyInstanceUID}/comments`
		let queries = ''

		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}

		return HTTP.get(`${request}${queries}`, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_COMMENTS_TEST', { StudyInstanceUID: params.StudyInstanceUID, comments: res.data.reverse() })
		}).catch(err => {
			console.log(err)
		})
	},
	postComment ({ dispatch }, params) {
		const request = `studies/${params.StudyInstanceUID}/comments`
		let queries = ''

		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		console.log(queries)
		return HTTP.post(request, 'comment=' + params.queries.comment, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			if (res.status === 204) {
				dispatch('getStudiesComments', { StudyInstanceUID: params.StudyInstanceUID })
			} else return res
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
