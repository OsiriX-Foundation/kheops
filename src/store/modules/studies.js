import { HTTP } from '@/router/http'
import dicomoperations from '@/mixins/dicomoperations'
import httpoperations from '@/mixins/httpoperations'
import SRImage from '@/assets/SR_2.png'
import PDFImage from '@/assets/pdf-240x240.png'
import VideoImage from '@/assets/video.png'
import DicomLogo from '@/assets/dicom_logo.png'
import Vue from 'vue'
import axios from 'axios'

// initial state
const state = {
	studies: [],
	defaultFlagStudy: {
		is_selected: false,
		is_hover: false,
		is_favorite: false,
		is_commented: false,
		is_indeterminate: false,
		view: ''
	},
	defaultFlagSerie: {
		is_selected: false,
		is_favorite: false
	}
}

// getters
const getters = {
	studies: state => state.studies,
	getFlagOfStudy: state => {
		let obj = {}
		state.studies.forEach(study => {
			obj[study.StudyInstanceUID.Value[0]] = study.flag
		})
		return obj
	},
	getStudyByUID: state => (uid) => {
		let idx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === uid })
		if (idx > -1) {
			return state.studies[idx]
		}
		return {}
	},
	getSerieByUID: state => (studyUID, serieUID) => {
		let idx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === studyUID })
		if (idx > -1) {
			return state.studies[idx].series[serieUID]
		}
		return {}
	}
}

// actions
const actions = {
	initStudies ({ commit, dispatch }, params) {
		commit('INIT_STUDIES')
	},
	getStudies ({ commit, dispatch }, params) {
		const request = 'studies'
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(`${request}${queries}`, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
			if (res.data !== '') {
				const studies = dicomoperations.translateDICOM(res.data)
				studies.forEach(study => {
					study.flag = JSON.parse(JSON.stringify(state.defaultFlagStudy))
					study.flag.is_favorite = study.SumFavorites['Value'][0] > 0
					study.flag.is_commented = study.SumComments['Value'][0] > 0
					// https://bootstrap-vue.js.org/docs/components/table/
					// chapter - Row details support
					study._showDetails = false
				})
				if (params.queries.offset === 0) {
					commit('INIT_STUDIES')
				}
				commit('SET_STUDIES', studies)
			}
			if (res.status === 204) {
				commit('SET_STUDIES', [])
			}
			return res
		}).catch(err => {
			return Promise.reject(err)
		})
	},
	setShowDetails ({ commit }, params) {
		let index = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		commit('SET_STUDY_SHOW_DETAILS', { index: index, value: params.value })
	},
	setFlagByStudyUID ({ commit }, params) {
		let index = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		commit('SET_STUDY_FLAG', { index: index, flag: params.flag, value: params.value })
	},
	favoriteStudy ({ commit, dispatch }, params) {
		let index = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		let request = `/studies/${params.StudyInstanceUID}/favorites`
		if (params.value === true) {
			return dispatch('addFavorite', { request: (request + queries) }).then(res => {
				commit('SET_STUDY_FLAG', { index: index, flag: 'is_favorite', value: params.value })
				return true
			})
		} else {
			return dispatch('removeFavorite', { request: (request + queries) }).then(res => {
				commit('SET_STUDY_FLAG', { index: index, flag: 'is_favorite', value: params.value })
				return true
			})
		}
	},
	addFavorite ({ commit }, params) {
		return HTTP.put(params.request).then(res => {
			return res
		}).catch(err => {
			return Promise.reject(err)
		})
	},
	removeFavorite ({ commit }, params) {
		return HTTP.delete(params.request).then(res => {
			return res
		}).catch(err => {
			return Promise.reject(err)
		})
	},
	deleteStudy ({ commit }, params) {
		const request = `/studies/${params.StudyInstanceUID}`
		return HTTP.delete(request).then(res => {
			commit('DELETE_STUDY', { StudyInstanceUID: params.StudyInstanceUID })
			commit('DELETE_SERIE_STUDY', { StudyInstanceUID: params.StudyInstanceUID })
			return true
		}).catch(err => {
			console.log(err)
			return false
		})
	},
	sendStudy ({ commit }, params) {
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		const request = `/studies/${params.StudyInstanceUID}/users/${params.userSub}`
		return HTTP.put(request + queries).then(res => {
			return res
		}).catch(err => {
			return Promise.reject(err)
		})
	},
	selfAppropriateStudy ({ commit }, params) {
		let request = 'studies'
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		let promises = []
		params.data.forEach(d => {
			if (d.serie_id) {
				promises.push(HTTP.put(`${request}/${d.study_id}/series/${d.serie_id}${queries}`)
					.then(res => {
						return {
							res: res,
							studyId: d.study_id,
							serieId: d.serie_id,
							albumId: d.album_id
						}
					})
					.catch(err => {
						return {
							res: err,
							studyId: d.study_id,
							serieId: d.serie_id,
							albumId: d.album_id
						}
					})
				)
			} else {
				promises.push(HTTP.put(`${request}/${d.study_id}${queries}`)
					.then(res => {
						return {
							res: res,
							studyId: d.study_id,
							albumId: d.album_id
						}
					})
					.catch(err => {
						return {
							res: err,
							studyId: d.study_id,
							albumId: d.album_id
						}
					})
				)
			}
		})
		return axios.all(promises)
			.then(res => {
				return res
			})
	}
}

// mutations
const mutations = {
	INIT_STUDIES (state) {
		state.studies = []
	},
	SET_STUDIES (state, studies) {
		studies.forEach(study => {
			state.studies.push(study)
		})
	},
	SET_STUDY_FLAG (state, params) {
		let study = state.studies[params.index]
		study.flag[params.flag] = params.value
		Vue.set(state.studies, params.index, study)
	},
	UPDATE_STUDIES (state, params) {
		state.studies = params.studies
	},
	DELETE_STUDY (state, params) {
		let studyIdx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === params.StudyInstanceUID })
		if (studyIdx > -1) {
			Vue.delete(state.studies, studyIdx)
		}
	},
	SET_STUDY_SHOW_DETAILS (state, params) {
		let study = state.studies[params.index]
		study._showDetails = params.value
		Vue.set(state.studies, params.indexStudy, study)
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
