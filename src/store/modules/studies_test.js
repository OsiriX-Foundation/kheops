import { HTTP } from '@/router/http'
import dicomoperations from '@/mixins/dicomoperations'
import httpoperations from '@/mixins/httpoperations'

// initial state
const state = {
	studies: []
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
		let defaultFlag = {
			is_selected: false,
			is_hover: false,
			is_favorite: false,
			is_commented: false
		}

		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(`${request}${queries.length > 0 ? '?' + queries.join('&') : ''}`, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
			const studies = dicomoperations.translateDICOM(res.data)
			let params = {}
			studies.forEach(study => {
				study.flag = JSON.parse(JSON.stringify(defaultFlag))
				study.flag.is_favorite = study.SumFavorites['Value'][0] > 0
				study.flag.is_commented = study.SumComments['Value'][0] > 0
			})
			params.studies = studies
			commit('SET_STUDIES_TEST', params.studies)
		})
	},
	setFlagByStudyUID ({ commit }, params) {
		let index = state.studies.findIndex(x => {
			return x.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		commit('SET_FLAG_TEST', { index: index, flag: params.flag, value: params.value })
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
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
