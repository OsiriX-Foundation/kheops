import { HTTP } from '@/router/http'
import dicomoperations from '@/mixins/dicomoperations'
import httpoperations from '@/mixins/httpoperations'
import SRImage from '@/assets/SR_2.png'
import PDFImage from '@/assets/pdf-240x240.png'
import VideoImage from '@/assets/video.png'
import DicomLogo from '@/assets/dicom_logo.png'

// initial state
const state = {
	studies: [],
	defaultFlagStudy: {
		is_selected: false,
		is_hover: false,
		is_favorite: false,
		is_commented: false,
		view: ''
	},
	defaultFlagSerie: {
		is_selected: false,
		is_favorite: false
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
					study.flag = JSON.parse(JSON.stringify(state.defaultFlagStudy))
					study.flag.is_favorite = study.SumFavorites['Value'][0] > 0
					study.flag.is_commented = study.SumComments['Value'][0] > 0
				})
				params.studies = studies
				commit('SET_STUDIES_TEST', params.studies)
			}
		})
	},
	getSeriesTest ({ commit, dispatch }, params) {
		let index = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		let queriesTab = []
		if (params.queries !== undefined) {
			queriesTab = httpoperations.getQueriesParameters(params.queries)
		}
		const request = `/studies/${params.StudyInstanceUID}/series`
		let queries = queriesTab.length > 0 ? '?' + queriesTab.join('&') : ''
		return HTTP.get(request + queries, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
			const series = dicomoperations.translateDICOM(res.data)
			let indexSerie
			series.forEach(serie => {
				indexSerie = state.studies[index].series === undefined ? -1 : state.studies[index].series.findIndex(serieState => {
					return serieState.SeriesInstanceUID.Value[0] === serie.SeriesInstanceUID.Value[0]
				})
				serie.flag = JSON.parse(JSON.stringify(state.defaultFlagSerie))
				serie.flag.is_selected = indexSerie !== -1 ? state.studies[index].series[indexSerie].flag.is_selected : state.studies[index].flag.is_selected
				dispatch('getImage', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: serie.SeriesInstanceUID.Value[0] })
			})
			commit('SET_SERIES_TEST', { index: index, series: series })
			return res
		})
	},
	getImage ({ commit }, params) {
		let request = `/wado?studyUID=${params.StudyInstanceUID}&seriesUID=${params.SeriesInstanceUID}&requestType=WADO&rows=250&columns=250&contentType=image%2Fjpeg`
		return HTTP.get(request, {
			responseType: 'arraybuffer',
			headers: {
				'Accept': 'image/jpeg'
			}
		}).then(resp => {
			console.log(resp)
			let img = DicomLogo
			if (resp.data) {
				let arr = new Uint8Array(resp.data)
				let raw = String.fromCharCode.apply(null, arr)
				let b64 = btoa(raw)
				var mimeType = resp.headers['content-type'].toLowerCase()
				img = 'data:' + mimeType + ';base64,' + b64
			}
			commit('SET_IMAGE_TEST', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, img: img })
		}).catch(() => {
			commit('SET_IMAGE_TEST', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, img: DicomLogo })
		})
	},
	setFlagByStudyUID ({ commit }, params) {
		let index = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		commit('SET_STUDY_FLAG_TEST', { index: index, flag: params.flag, value: params.value })
	},
	setFlagByStudyUIDSerieUID ({ commit }, params) {
		let indexStudy = state.studies.findIndex(study => {
			return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
		})
		let indexSerie = state.studies[indexStudy].series.findIndex(serie => {
			return serie.SeriesInstanceUID.Value[0] === params.SeriesInstanceUID
		})
		commit('SET_SERIE_FLAG_TEST', { indexStudy: indexStudy, indexSerie: indexSerie, flag: params.flag, value: params.value })
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
			commit('SET_STUDY_FLAG_TEST', { index: index, flag: 'is_favorite', value: params.value })
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
	SET_SERIES_TEST (state, params) {
		state.studies[params.index].series = params.series
	},
	SET_STUDY_FLAG_TEST (state, params) {
		state.studies[params.index].flag[params.flag] = params.value
	},
	SET_SERIE_FLAG_TEST (state, params) {
		state.studies[params.indexStudy].series[params.indexSerie].flag[params.flag] = params.value
	},
	SET_IMAGE_TEST (state, params) {
		let idx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === params.StudyInstanceUID })
		if (idx > -1) {
			let sidx = _.findIndex(state.studies[idx].series, s => { return s.SeriesInstanceUID.Value[0] === params.SeriesInstanceUID })
			if (sidx > -1) state.studies[idx].series[sidx].imgSrc = params.img
		}
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
