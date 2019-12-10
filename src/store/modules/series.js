import Vue from 'vue';
import { HTTP } from '@/router/http';
import dicomoperations from '@/mixins/dicomoperations';
import httpoperations from '@/mixins/httpoperations';
import SRImage from '@/assets/img/SR_2.png';
import PDFImage from '@/assets/img/pdf-240x240.png';
import VideoImage from '@/assets/img/video.png';
import DicomLogo from '@/assets/img/dicom_logo.png';

// initial state
const state = {
  series: {},
  defaultFlagSerie: {
    is_selected: false,
    is_favorite: false,
  },
  modalities: [
    'DOC',
    'XC',
    'SR',
  ],
};

// getters
const getters = {
  series: (state) => state.series,
};

// actions
const actions = {
  initSeries({ commit }) {
    commit('INIT_SERIES');
  },
  getSeries({ commit, dispatch }, params) {
    const request = `/studies/${params.StudyInstanceUID}/series`;
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(request + queries, { headers: { Accept: 'application/dicom+json' } }).then((res) => {
      const newSeries = dicomoperations.translateObjectDICOM(res.data, '0020000E');
      Object.keys(newSeries).forEach((serieUID) => {
        newSeries[serieUID].imgSrc = state.series[params.StudyInstanceUID] !== undefined && state.series[params.StudyInstanceUID][serieUID] !== undefined ? state.series[params.StudyInstanceUID][serieUID].imgSrc : '';
        newSeries[serieUID].flag = JSON.parse(JSON.stringify(state.defaultFlagSerie));
        newSeries[serieUID].SOPClassUID = state.series[params.StudyInstanceUID] !== undefined && state.series[params.StudyInstanceUID][serieUID] !== undefined ? state.series[params.StudyInstanceUID][serieUID].SOPClassUID : undefined;
        const seriesAlreadyExist = (state.series[params.StudyInstanceUID] !== undefined && state.series[params.StudyInstanceUID][serieUID] !== undefined);
        newSeries[serieUID].flag.is_selected = seriesAlreadyExist ? state.series[params.StudyInstanceUID][serieUID].flag.is_selected : params.studySelected;
      });
      commit('SET_SERIES', { StudyInstanceUID: params.StudyInstanceUID, series: newSeries });
      dispatch('setSeriesImage', { StudyInstanceUID: params.StudyInstanceUID });
      return res;
    }).catch((err) => Promise.reject(err));
  },
  setSeriesImage({ dispatch }, params) {
    const series = state.series[params.StudyInstanceUID];
    Object.keys(series).forEach((serieUID) => {
      if (series[serieUID].imgSrc === '') {
        dispatch('setSerieImage', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: serieUID });
      }
    });
  },
  setSerieImage({ dispatch }, params) {
    const serie = state.series[params.StudyInstanceUID][params.SeriesInstanceUID];
    const checkNumberOfSeriesRelatedInstance = (serie.NumberOfSeriesRelatedInstances !== undefined && serie.NumberOfSeriesRelatedInstances.Value !== undefined && serie.NumberOfSeriesRelatedInstances.Value[0] === 1);
    const checkModalities = !checkNumberOfSeriesRelatedInstance && (serie.Modality !== undefined && serie.Modality.Value !== undefined && state.modalities.includes(serie.Modality.Value[0]));
    if ((checkModalities) || (checkNumberOfSeriesRelatedInstance)) {
      return dispatch('getSeriesInstances', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID }).then((res) => {
        if (res.data !== undefined) {
          const modality = serie.Modality !== undefined ? serie.Modality.Value[0] : '';
          return dispatch('setImageSrc', {
            StudyInstanceUID: params.StudyInstanceUID, serie, serieUID: params.SeriesInstanceUID, data: res.data, modality,
          });
        }
        return res;
      }).catch((err) => {
        dispatch('setImageError', serie);
        Promise.reject(err);
      });
    }
    return dispatch('getImage', {
      StudyInstanceUID: params.StudyInstanceUID,
      SeriesInstanceUID: params.SeriesInstanceUID,
      serie,
    });
  },
  getSeriesInstances(context, params) {
    const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/instances`;
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(request + queries).then((res) => res).catch((err) => Promise.reject(err));
  },
  getSeriesMetadata(context, params) {
    const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/metadata`;
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(request + queries).then((res) => res).catch((err) => Promise.reject(err));
  },
  setImageSrc({ dispatch, commit }, params) {
    const tagSOPClassUID = '00080016';
    const tagModality = '00080060';
    const { serie } = params;
    const SOPClassUID = {
      videoPhotographicImageStorage: '1.2.840.10008.5.1.4.1.1.77.1.4.1',
      encapsulatedPDFStorage: '1.2.840.10008.5.1.4.1.1.104.1',
    };
    const modality = params.modality.length > 0 || params.data[0][tagModality] === undefined ? params.modality : params.data[0][tagModality].Value[0];
    if (params.data[0][tagSOPClassUID] !== undefined) {
      serie.SOPClassUID = params.data[0][tagSOPClassUID];
    }
    if (modality.includes('SR')) {
      serie.imgSrc = SRImage;
      commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, serie: params.serie, SeriesInstanceUID: params.serieUID });
    } else if (params.data[0][tagSOPClassUID].Value[0] === SOPClassUID.videoPhotographicImageStorage) {
      serie.imgSrc = VideoImage;
      commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, serie: params.serie, SeriesInstanceUID: params.serieUID });
    } else if (params.data[0][tagSOPClassUID].Value[0] === SOPClassUID.encapsulatedPDFStorage) {
      serie.imgSrc = PDFImage;
      commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, serie: params.serie, SeriesInstanceUID: params.serieUID });
    } else {
      dispatch('getImage', {
        StudyInstanceUID: params.StudyInstanceUID,
        SeriesInstanceUID: params.serieUID,
        serie: params.serie,
      });
    }
    return true;
  },
  getImage({ commit, dispatch }, params) {
    const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/thumbnail`;
    const queries = `viewport=${encodeURIComponent('256,256')}`;
    const { serie } = params;
    return HTTP.get(`${request}?${queries}`, {
      responseType: 'arraybuffer',
      headers: {
        Accept: 'image/png',
      },
    }).then((resp) => {
      let img = DicomLogo;
      if (resp.data) {
        const arr = new Uint8Array(resp.data);
        const raw = String.fromCharCode.apply(null, arr);
        const b64 = btoa(raw);
        const mimeType = resp.headers['content-type'].toLowerCase();
        img = `data:${mimeType};base64,${b64}`;
      }
      serie.imgSrc = img;
      commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie: params.serie });
    }).catch(() => {
      dispatch('setImageError', serie);
    });
  },
  setImageError({ commit }, params) {
    const serie = params;
    serie.imgSrc = DicomLogo;
    commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie: params.serie });
  },
  setFlagByStudyUIDSerieUID({ commit }, params) {
    const serie = state.series[params.StudyInstanceUID][params.SeriesInstanceUID];
    serie.flag[params.flag] = params.value;
    commit('SET_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID, serie });
    return true;
  },
  deleteSerie({ commit }, params) {
    const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}`;
    return HTTP.delete(request).then((res) => {
      commit('DELETE_SERIE', { StudyInstanceUID: params.StudyInstanceUID, SeriesInstanceUID: params.SeriesInstanceUID });
      return res;
    }).catch((err) => Promise.reject(err));
  },
  sendSerie(context, params) {
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    const request = `/studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/users/${params.userSub}`;
    return HTTP.put(request + queries).then((res) => res).catch((err) => Promise.reject(err));
  },
};

// mutations
const mutations = {
  INIT_SERIES(state) {
    state.series = {};
  },
  SET_SERIES(state, params) {
    Vue.set(state.series, params.StudyInstanceUID, params.series);
  },
  SET_SERIE(state, params) {
    if (state.series[params.StudyInstanceUID] !== undefined) {
      Vue.set(state.series[params.StudyInstanceUID], params.SeriesInstanceUID, params.serie);
    }
  },
  DELETE_SERIE(state, params) {
    Vue.delete(state.series[params.StudyInstanceUID], params.SeriesInstanceUID);
  },
  DELETE_SERIE_STUDY(state, params) {
    Vue.delete(state.series, params.StudyInstanceUID);
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
