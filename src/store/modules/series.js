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
	getSeriesObject ({ commit, dispatch }, params) {
		const request = `/studies/${params.StudyInstanceUID}/series`
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(request + queries, { headers: { 'Accept': 'application/dicom+json' } }).then(res => {
            let objSeries = dicomoperations.translateObjectDICOM(res.data, '0020000E')
			for (let serieUID in objSeries) {
				objSeries[serieUID].flag = JSON.parse(JSON.stringify(state.defaultFlagSerie))
				let seriesAlreadyExist = (state.series[params.StudyInstanceUID] !== undefined && state.series[params.StudyInstanceUID][serieUID] !== undefined)
                objSeries[serieUID].flag.is_selected = seriesAlreadyExist ? state.series[params.StudyInstanceUID][serieUID].flag.is_selected : params.studySelected
                let serie = objSeries[serieUID]
                dispatch('getSerieMetadataObject', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: serieUID }).then(res => {
					if (res.data !== undefined) {
						dispatch('setImageObject', { StudyInstanceUID: params.StudyInstanceUID, serie: serie, serieUID: serieUID, data: res.data })
					}
				})
            }
            commit('SET_SERIES_OBJ', {StudyInstanceUID: params.StudyInstanceUID, series: objSeries})
			return res
		}).catch(err => {
			return Promise.reject(err)
		})
	},
	getSerieMetadataObject ({ commit }, params) {
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
	setImageObject ({ dispatch, commit }, params) {
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
			dispatch('getImageObject', {
				StudyInstanceUID: params.StudyInstanceUID,
				SeriesInstanceUID: params.serieUID,
				serie: params.serie
			})
		}
		commit('SET_SERIE_OBJ', { StudyInstanceUID: params.StudyInstanceUID, serie: params.serie, SeriesInstanceUID: params.serieUID })
	},
	getImageObject ({ commit }, params) {
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
			commit('SET_SERIE_OBJ', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie: params.serie })
		}).catch(() => {
			params.serie.imgSrc = DicomLogo
			commit('SET_SERIE_OBJ', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie: params.serie })
		})
	},
	setFlagByStudyUIDSerieUIDObject ({ commit }, params) {
		commit('SET_SERIE_FLAG_OBJ', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, flag: params.flag, value: params.value })
		return true
	}
}

// mutations
const mutations = {
	SET_SERIES_OBJ (state, params) {
		Vue.set(state.series, params.StudyInstanceUID, params.series)
	},
	SET_SERIE_OBJ (state, params) {
		console.log('set !')
		Vue.set(state.series[params.StudyInstanceUID], params.SeriesInstanceUID, params.serie)
	},
	SET_SERIE_FLAG_OBJ (state, params) {
		let serie = state.series[params.StudyInstanceUID][params.SeriesInstanceUID]
		serie.flag[params.flag] = params.value
		Vue.set(state.series[params.StudyInstanceUID], params.SeriesInstanceUID, serie)
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
