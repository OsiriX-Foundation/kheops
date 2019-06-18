import { HTTP } from '@/router/http'
import dicomoperations from '@/mixins/dicomoperations'
import httpoperations from '@/mixins/httpoperations'

// initial state
const state = {
	studies: [],
	defaultFlag: {
		is_selected: false,
		is_hover: false,
		is_favorite: false,
		is_commented: false
	}
}

// getters
const getters = {
	studiesTest: state => state.studies
}

// actions
const actions = {
	getStudiesTest ({ commit, dispatch }, params) {
		const request = 'studies'
		let queries = []

		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(`${request}${queries.length > 0 ? '?' + queries.join('&') : ''}`, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
			if (res.data !== '') {
				const studies = dicomoperations.translateDICOM(res.data)
				let params = {}
				studies.forEach(study => {
					study.flag = JSON.parse(JSON.stringify(state.defaultFlag))
					study.flag.is_favorite = study.SumFavorites['Value'][0] > 0
					study.flag.is_commented = study.SumComments['Value'][0] > 0
				})
				params.studies = studies
				commit('SET_STUDIES_TEST', params.studies)
			}
		})
	},
	setFlagByStudyUID ({ commit }, params) {
		let index = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		commit('SET_FLAG_TEST', { index: index, flag: params.flag, value: params.value })
	},
	favoriteStudy ({ commit }, params) {
		let index = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})

		let queriesTab = []
		if (params.queries !== undefined) {
			queriesTab = httpoperations.getQueriesParameters(params.queries)
		}
		let request = `/studies/${params.StudyInstanceUID}/favorites`
		let queries = queriesTab.length > 0 ? '?' + queriesTab.join('&') : ''
		return HTTP.put(request + queries).then(res => {
			commit('SET_FLAG_TEST', { index: index, flag: 'is_favorite', value: params.value })
			return true
		}).catch(err => {
			console.log(err)
			return false
		})
	},
	deleteStudyTest ({ commit }, params) {
		let queriesTab = []
		if (params.queries !== undefined) {
			queriesTab = httpoperations.getQueriesParameters(params.queries)
		}
		const request = `/studies/${params.StudyInstanceUID}`
		let queries = queriesTab.length > 0 ? '?' + queriesTab.join('&') : ''
		return HTTP.delete(request + queries).then(res => {
			commit('DELETE_STUDY_TEST', { StudyInstanceUID: params.StudyInstanceUID })
			return true
		}).catch(err => {
			console.log(err)
			return false
		})
	}
}

// mutations
const mutations = {
	INIT_STUDIES_TEST (state) {
		state.studies = []
	},
	SET_STUDIES_TEST (state, studies) {
		state.studies = studies
	},
	SET_FLAG_TEST (state, params) {
		state.studies[params.index].flag[params.flag] = params.value
	},
	UPDATE_STUDIES_TEST (state, params) {
		state.studies = params.studies
	},
	DELETE_STUDY_TEST (state, params) {
		let studyIdx = _.findIndex(state.studies, s => { return s.StudyInstanceUID[0] === params.StudyInstanceUID })
		if (studyIdx > -1) state.studies.splice(studyIdx, 1)
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
