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
	series: {},
	defaultFlagSerie: {
		is_selected: false,
		is_favorite: false
	}
}

// getters
const getters = {
	series: state => state.series
}

// actions
const actions = {
	initSeries ({ commit, dispatch }, params) {
		commit('INIT_SERIES')
	},
	getSeries ({ commit, dispatch }, params) {
		const request = `/studies/${params.StudyInstanceUID}/series`
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(request + queries, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
			let newSeries = dicomoperations.translateObjectDICOM(res.data, '0020000E')
			for (let serieUID in newSeries) {
				newSeries[serieUID].imgSrc = state.series[params.StudyInstanceUID] !== undefined && state.series[params.StudyInstanceUID][serieUID] !== undefined ? state.series[params.StudyInstanceUID][serieUID].imgSrc : ''
				newSeries[serieUID].flag = JSON.parse(JSON.stringify(state.defaultFlagSerie))
				let seriesAlreadyExist = (state.series[params.StudyInstanceUID] !== undefined && state.series[params.StudyInstanceUID][serieUID] !== undefined)
				newSeries[serieUID].flag.is_selected = seriesAlreadyExist ? state.series[params.StudyInstanceUID][serieUID].flag.is_selected : params.studySelected
			}
			commit('SET_SERIES', { StudyInstanceUID: params.StudyInstanceUID, series: newSeries })
			dispatch('setSeriesImage', { StudyInstanceUID: params.StudyInstanceUID })
			return res
		}).catch(err => {
			return Promise.reject(err)
		})
	},
	setSeriesImage ({ commit, dispatch }, params) {
		let series = state.series[params.StudyInstanceUID]
		for (let serieUID in series) {
			if (series[serieUID].imgSrc === '') {
				dispatch('setSerieImage', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: serieUID })
			}
		}
	},
	setSerieImage ({ commit, dispatch }, params) {
		let serie = state.series[params.StudyInstanceUID][params.SeriesInstanceUID]
		if (serie.NumberOfSeriesRelatedInstances.Value[0] === 1) {
			return dispatch('getSerieMetadata', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID }).then(res => {
				if (res.data !== undefined) {
					return dispatch('setImageSrc', { StudyInstanceUID: params.StudyInstanceUID, serie: serie, serieUID: params.SeriesInstanceUID, data: res.data })
				}
			}).catch(err => {
				Promise.reject(err)
			})
		} else {
			dispatch('getImage', {
				StudyInstanceUID: params.StudyInstanceUID,
				SeriesInstanceUID: params.SeriesInstanceUID,
				serie: serie
			})
		}
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
	setImageSrc ({ dispatch, commit }, params) {
		const tagSOPClassUID = '00080016'
		const tagModality = '00080060'
		const SOPClassUID = {
			'videoPhotographicImageStorage': '1.2.840.10008.5.1.4.1.1.77.1.4.1',
			'encapsulatedPDFStorage': '1.2.840.10008.5.1.4.1.1.104.1'
		}
		if (params.data[0][tagSOPClassUID] !== undefined) {
			params.serie.SOPClassUID = params.data[0][tagSOPClassUID]
		}
		if (params.data[0][tagModality].Value[0].includes('SR')) {
			params.serie.imgSrc = SRImage
			commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, serie: params.serie, SeriesInstanceUID: params.serieUID })
		} else if (params.data[0][tagSOPClassUID].Value[0] === SOPClassUID['videoPhotographicImageStorage']) {
			params.serie.imgSrc = VideoImage
			commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, serie: params.serie, SeriesInstanceUID: params.serieUID })
		} else if (params.data[0][tagSOPClassUID].Value[0] === SOPClassUID['encapsulatedPDFStorage']) {
			params.serie.imgSrc = PDFImage
			commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, serie: params.serie, SeriesInstanceUID: params.serieUID })
		} else {
			dispatch('getImage', {
				StudyInstanceUID: params.StudyInstanceUID,
				SeriesInstanceUID: params.serieUID,
				serie: params.serie
			})
		}
		return true
	},
	getImage ({ commit }, params) {
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
			params.serie.imgSrc = img
			commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie: params.serie })
		}).catch(() => {
			params.serie.imgSrc = DicomLogo
			commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie: params.serie })
		})
	},
	setFlagByStudyUIDSerieUID ({ commit }, params) {
		let serie = state.series[params.StudyInstanceUID][params.SeriesInstanceUID]
		serie.flag[params.flag] = params.value
		commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie: serie })
		return true
	},
	deleteSerie ({ commit }, params) {
		const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}`
		return HTTP.delete(request).then(res => {
			commit('DELETE_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID })
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
	}
}

// mutations
const mutations = {
	INIT_SERIES (state) {
		state.series = {}
	},
	SET_SERIES (state, params) {
		Vue.set(state.series, params.StudyInstanceUID, params.series)
	},
	SET_SERIE (state, params) {
		if (state.series[params.StudyInstanceUID] !== undefined) {
			Vue.set(state.series[params.StudyInstanceUID], params.SeriesInstanceUID, params.serie)
		}
	},
	DELETE_SERIE (state, params) {
		Vue.delete(state.series[params.StudyInstanceUID], params.SeriesInstanceUID)
	},
	DELETE_SERIE_STUDY (state, params) {
		Vue.delete(state.series, params.StudyInstanceUID)
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
