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
	studiesTest: state => state.studies,
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
			let sidx = _.findIndex(state.studies[idx].series, s => { return s.SeriesInstanceUID.Value[0] === serieUID })
			if (sidx > -1) {
				return {
					serie: state.studies[idx].series[sidx],
					studyIndex: idx,
					serieIndex: sidx
				}
			}
		}
		return {}
	}
}

// actions
const actions = {
	getStudiesTest ({ commit, dispatch }, params) {
		const request = 'studies'
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(`${request}${queries}`, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
			if (res.data !== '') {
				const studies = dicomoperations.translateDICOM(res.data)
				let params = {}
				studies.forEach(study => {
					study.flag = JSON.parse(JSON.stringify(state.defaultFlagStudy))
					study.flag.is_favorite = study.SumFavorites['Value'][0] > 0
					study.flag.is_commented = study.SumComments['Value'][0] > 0
					// https://bootstrap-vue.js.org/docs/components/table/
					// chapter - Row details support
					study._showDetails = false
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
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		const request = `/studies/${params.StudyInstanceUID}/series`
		return HTTP.get(request + queries, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
			const series = dicomoperations.translateDICOM(res.data)
			let indexSerie
			series.forEach(serie => {
				indexSerie = state.studies[index].series === undefined ? -1 : state.studies[index].series.findIndex(serieState => {
					return serieState.SeriesInstanceUID.Value[0] === serie.SeriesInstanceUID.Value[0]
				})
				serie.flag = JSON.parse(JSON.stringify(state.defaultFlagSerie))
				serie.flag.is_selected = indexSerie !== -1 ? state.studies[index].series[indexSerie].flag.is_selected : state.studies[index].flag.is_selected
				dispatch('getSerieMetadata', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: serie.SeriesInstanceUID.Value[0] }).then(res => {
					if (res.data !== undefined) {
						const tagSOPClassUID = '00080016'
						const SOPClassUID = {
							'videoPhotographicImageStorage': '1.2.840.10008.5.1.4.1.1.77.1.4.1',
							'encapsulatedPDFStorage': '1.2.840.10008.5.1.4.1.1.104.1'
						}
						if (res.data[0]['00080060'].Value[0].includes('SR')) {
							serie.imgSrc = SRImage
						} else if (res.data[0][tagSOPClassUID].Value[0] === SOPClassUID['videoPhotographicImageStorage']) {
							serie.imgSrc = VideoImage
						} else if (res.data[0][tagSOPClassUID].Value[0] === SOPClassUID['encapsulatedPDFStorage']) {
							serie.imgSrc = PDFImage
						} else {
							dispatch('getImageTest', {
								StudyInstanceUID: params.StudyInstanceUID,
								SeriesInstanceUID: serie.SeriesInstanceUID.Value[0]
							})
						}
					}
				})
			})
			commit('SET_SERIES_TEST', { index: index, series: series })
			return res
		})
	},
	getSerieMetadata ({ commit }, params) {
		let request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/metadata`
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(request + queries).then(res => {
			return res
		}).catch(err => {
			console.log(err)
		})
	},
	getImageTest ({ commit }, params) {
		let request = `/wado?studyUID=${params.StudyInstanceUID}&seriesUID=${params.SeriesInstanceUID}&requestType=WADO&rows=250&columns=250&contentType=image%2Fjpeg`
		return HTTP.get(request, {
			responseType: 'arraybuffer',
			headers: {
				'Accept': 'image/jpeg'
			}
		}).then(resp => {
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
		commit('SET_STUDY_FLAG_TEST', { index: index, flag: params.flag, value: params.value })
	},
	setFlagByStudyUIDSerieUID ({ commit }, params) {
		let indexStudy = params.studyIndex
		if (indexStudy === undefined || state.studies[indexStudy].StudyInstanceUID.Value[0] !== params.StudyInstanceUID) {
			indexStudy = state.studies.findIndex(study => {
				return study.StudyInstanceUID.Value[0] === params.StudyInstanceUID
			})
		}
		let indexSerie = params.serieIndex
		if (indexSerie === undefined || state.studies[indexStudy].series[indexSerie].SeriesInstanceUID.Value[0] !== params.SeriesInstanceUID) {
			indexSerie = state.studies[indexStudy].series.findIndex(serie => {
				return serie.SeriesInstanceUID.Value[0] === params.SeriesInstanceUID
			})
		}
		commit('SET_SERIE_FLAG_TEST', { indexStudy: indexStudy, indexSerie: indexSerie, flag: params.flag, value: params.value })
		return true
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
				commit('SET_STUDY_FLAG_TEST', { index: index, flag: 'is_favorite', value: params.value })
				return true
			})
		} else {
			return dispatch('removeFavorite', { request: (request + queries) }).then(res => {
				commit('SET_STUDY_FLAG_TEST', { index: index, flag: 'is_favorite', value: params.value })
				return true
			})
		}
	},
	addFavorite ({ commit }, params) {
		return HTTP.put(params.request).then(res => {
			return res
		})
	},
	removeFavorite ({ commit }, params) {
		return HTTP.delete(params.request).then(res => {
			return res
		})
	},
	deleteStudyTest ({ commit }, params) {
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		const request = `/studies/${params.StudyInstanceUID}`
		return HTTP.delete(request + queries).then(res => {
			commit('DELETE_STUDY_TEST', { StudyInstanceUID: params.StudyInstanceUID })
			return true
		}).catch(err => {
			console.log(err)
			return false
		})
	},
	deleteSerieTest ({ commit }, params) {
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}`
		return HTTP.delete(request + queries).then(res => {
			commit('DELETE_SERIE_TEST', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID })
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
		let study = state.studies[params.index]
		study.flag[params.flag] = params.value
		Vue.set(state.studies, params.indexStudy, study)
	},
	SET_SERIE_FLAG_TEST (state, params) {
		let study = state.studies[params.indexStudy]
		study.series[params.indexSerie].flag[params.flag] = params.value
		Vue.set(state.studies, params.indexStudy, study)
	},
	SET_IMAGE_TEST (state, params) {
		let idx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === params.StudyInstanceUID })
		if (idx > -1) {
			let sidx = _.findIndex(state.studies[idx].series, s => { return s.SeriesInstanceUID.Value[0] === params.SeriesInstanceUID })
			if (sidx > -1) {
				let study = state.studies[idx]
				study.series[sidx].imgSrc = params.img
				Vue.set(state.studies, idx, study)
			}
		}
	},
	UPDATE_STUDIES_TEST (state, params) {
		state.studies = params.studies
	},
	DELETE_STUDY_TEST (state, params) {
		let studyIdx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === params.StudyInstanceUID })
		if (studyIdx > -1) {
			Vue.delete(state.studies, studyIdx)
		}
	},
	DELETE_SERIE_TEST (state, params) {
		let studyIdx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === params.StudyInstanceUID })
		if (studyIdx > -1) {
			let seriesIdx = _.findIndex(state.studies[studyIdx].series, s => { return s.SeriesInstanceUID.Value[0] === params.SeriesInstanceUID })
			if (seriesIdx > -1) {
				Vue.delete(state.studies[studyIdx].series, seriesIdx)
				Vue.set(state.studies, studyIdx, state.studies[studyIdx])
			}
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
