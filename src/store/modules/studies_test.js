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
			return state.studies[idx].series[serieUID]
		}
		return {}
	}
}

// actions
const actions = {
	initStudiesTest ({ commit, dispatch }, params) {
		commit('INIT_STUDIES_TEST')
	},
	getStudiesTest ({ commit, dispatch }, params) {
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
					dispatch('initStudiesTest')
				}
				commit('SET_STUDIES_TEST', studies)
			}
			if (res.status === 204) {
				commit('SET_STUDIES_TEST', [])
			}
			return res
		}).catch(err => {
			return Promise.reject(err)
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
			let objSeries = dicomoperations.translateObjectDICOM(res.data, '0020000E')
			for (let serieUID in objSeries) {
				objSeries[serieUID].flag = JSON.parse(JSON.stringify(state.defaultFlagSerie))
				let seriesAlreadyExist = (state.studies[index].series !== undefined && state.studies[index].series[serieUID] !== undefined)
				objSeries[serieUID].flag.is_selected = seriesAlreadyExist ? state.studies[index].series[serieUID].flag.is_selected : state.studies[index].flag.is_selected
				let serie = objSeries[serieUID]
				dispatch('getSerieMetadata', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: serieUID }).then(res => {
					if (res.data !== undefined) {
						dispatch('setImageTest', { StudyInstanceUID: params.StudyInstanceUID, serie: serie, indexStudy: index, serieUID: serieUID, data: res.data })
					}
				})
			}
			commit('SET_SERIES_TEST', { index: index, series: objSeries })
			return res
		}).catch(err => {
			return Promise.reject(err)
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
			return Promise.reject(err)
		})
	},
	setImageTest ({ dispatch, commit }, params) {
		const tagSOPClassUID = '00080016'
		const SOPClassUID = {
			'videoPhotographicImageStorage': '1.2.840.10008.5.1.4.1.1.77.1.4.1',
			'encapsulatedPDFStorage': '1.2.840.10008.5.1.4.1.1.104.1'
		}
		if (params.data[0][tagSOPClassUID] !== undefined) {
			params.serie.SOPClassUID = params.data[0][tagSOPClassUID]
		}
		if (params.data[0]['00080060'].Value[0].includes('SR')) {
			params.serie.imgSrc = SRImage
		} else if (params.data[0][tagSOPClassUID].Value[0] === SOPClassUID['videoPhotographicImageStorage']) {
			params.serie.imgSrc = VideoImage
		} else if (params.data[0][tagSOPClassUID].Value[0] === SOPClassUID['encapsulatedPDFStorage']) {
			params.serie.imgSrc = PDFImage
		} else {
			dispatch('getImageTest', {
				StudyInstanceUID: params.StudyInstanceUID,
				SeriesInstanceUID: params.serieUID
			})
		}
		commit('SET_SERIE_TEST', { indexStudy: params.indexStudy, serie: params.serie, indexSerie: params.serieUID })
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
			commit('SET_SERIE_IMAGE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, img: img })
		}).catch(() => {
			commit('SET_SERIE_IMAGE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, img: DicomLogo })
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
		commit('SET_SERIE_FLAG_TEST', { indexStudy: indexStudy, SeriesInstanceUID: params.SeriesInstanceUID, flag: params.flag, value: params.value })
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
	deleteStudyTest ({ commit }, params) {
		const request = `/studies/${params.StudyInstanceUID}`
		return HTTP.delete(request).then(res => {
			commit('DELETE_STUDY_TEST', { StudyInstanceUID: params.StudyInstanceUID })
			return true
		}).catch(err => {
			console.log(err)
			return false
		})
	},
	deleteSerieTest ({ commit }, params) {
		const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}`
		return HTTP.delete(request).then(res => {
			commit('DELETE_SERIE_TEST', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID })
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
	sendSerie ({ commit }, params) {
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/users/${params.userSub}`
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
	INIT_STUDIES_TEST (state) {
		state.studies = []
	},
	SET_STUDIES_TEST (state, studies) {
		studies.forEach(study => {
			state.studies.push(study)
		})
	},
	SET_SERIES_TEST (state, params) {
		state.studies[params.index].series = params.series
	},
	SET_SERIE_TEST (state, params) {
		let study = state.studies[params.indexStudy]
		study.series[params.indexSerie] = params.serie
		Vue.set(state.studies, params.indexStudy, study)
	},
	SET_STUDY_FLAG_TEST (state, params) {
		let study = state.studies[params.index]
		study.flag[params.flag] = params.value
		Vue.set(state.studies, params.index, study)
	},
	SET_SERIE_FLAG_TEST (state, params) {
		let study = state.studies[params.indexStudy]
		study.series[params.SeriesInstanceUID].flag[params.flag] = params.value
		Vue.set(state.studies, params.indexStudy, study)
	},
	SET_SERIE_IMAGE (state, params) {
		let idx = _.findIndex(state.studies, s => { return s.StudyInstanceUID.Value[0] === params.StudyInstanceUID })
		if (idx > -1) {
			let study = state.studies[idx]
			study.series[params.SeriesInstanceUID].imgSrc = params.img
			Vue.set(state.studies, idx, study)
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
			delete state.studies[studyIdx].series[params.SeriesInstanceUID]
			/*
			let seriesIdx = _.findIndex(state.studies[studyIdx].series, s => { return s.SeriesInstanceUID.Value[0] === params.SeriesInstanceUID })
			if (seriesIdx > -1) {
				Vue.delete(state.studies[studyIdx].series, seriesIdx)
				Vue.set(state.studies, studyIdx, state.studies[studyIdx])
			}
			*/
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
