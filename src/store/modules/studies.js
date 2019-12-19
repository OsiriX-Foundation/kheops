import Vue from 'vue';
import axios from 'axios';
import { HTTP } from '@/router/http';
import dicomoperations from '@/mixins/dicomoperations';
import httpoperations from '@/mixins/httpoperations';

// initial state
const state = {
  studies: [],
  defaultFlagStudy: {
    is_selected: false,
    is_hover: false,
    is_favorite: false,
    is_commented: false,
    is_indeterminate: false,
    view: '',
  },
  modalities: [],
};

// getters
const getters = {
  studies: (state) => state.studies,
  getFlagOfStudy: (state) => {
    const obj = {};
    state.studies.forEach((study) => {
      obj[study.StudyInstanceUID.Value[0]] = study.flag;
    });
    return obj;
  },
  getStudyByUID: (state) => (uid) => {
    const idx = _.findIndex(state.studies, (s) => s.StudyInstanceUID.Value[0] === uid);
    if (idx > -1) {
      return state.studies[idx];
    }
    return {};
  },
  modalities: (state) => state.modalities,
};

// actions
const actions = {
  initStudies({ commit }) {
    commit('INIT_STUDIES');
  },
  getStudies({ commit }, params) {
    const request = 'studies';
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(`${request}${queries}`, { headers: { Accept: 'application/dicom+json' } }).then((res) => {
      if (res.data !== '') {
        const studies = dicomoperations.translateDICOM(res.data);
        studies.forEach((study) => {
          /* eslint-disable no-param-reassign */
          study.flag = JSON.parse(JSON.stringify(state.defaultFlagStudy));
          study.flag.is_favorite = study.SumFavorites !== undefined ? study.SumFavorites.Value[0] > 0 : false;
          study.flag.is_commented = study.SumComments !== undefined ? study.SumComments.Value[0] > 0 : false;
          // https://bootstrap-vue.js.org/docs/components/table/
          // chapter - Row details support
          // eslint-disable-next-line
          study._showDetails = false;
          study.showIcons = false;
          /* eslint-enable no-param-reassign */
        });
        if (params.queries.offset === 0) {
          commit('INIT_STUDIES');
        }
        commit('SET_STUDIES', studies);
      }
      if (res.status === 204) {
        commit('SET_STUDIES', []);
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  updateStudies({ commit }, params) {
    const request = 'studies';
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(`${request}${queries}`, { headers: { Accept: 'application/dicom+json' } }).then((res) => {
      if (res.data !== '') {
        const studies = dicomoperations.translateDICOM(res.data);
        studies.forEach((study, index) => {
          const currentUID = study.StudyInstanceUID.Value[0];
          const stateUID = state.studies[index] !== undefined ? state.studies[index].StudyInstanceUID.Value[0] : undefined;

          /* eslint-disable no-param-reassign */
          study.flag = JSON.parse(JSON.stringify(state.defaultFlagStudy));
          study.flag.is_favorite = study.SumFavorites !== undefined ? study.SumFavorites.Value[0] > 0 : false;
          study.flag.is_commented = study.SumComments !== undefined ? study.SumComments.Value[0] > 0 : false;
          // eslint-disable-next-line
          study._showDetails = false;
          study.showIcons = false;
          /* eslint-enable no-param-reassign */
          if (state.studies.length > 0 && currentUID !== stateUID) {
            const paramsUpdate = {
              index,
              study,
            };
            commit('UPDATE_STUDIES', paramsUpdate);
            if (state.studies.length > params.queries.limit) {
              commit('REMOVE_LASTSTUDY');
            }
          }
        });
        if (state.studies.length === 0) {
          commit('SET_STUDIES', studies);
        }
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  setShowDetails({ commit }, params) {
    const index = state.studies.findIndex((study) => study.StudyInstanceUID.Value[0] === params.StudyInstanceUID);
    commit('SET_STUDY_SHOW_DETAILS', { index, value: params.value });
  },
  setFlagByStudyUID({ commit }, params) {
    let { index } = params;
    if (index === undefined) {
      index = state.studies.findIndex((study) => study.StudyInstanceUID.Value[0] === params.StudyInstanceUID);
    }
    commit('SET_STUDY_FLAG', { index, flag: params.flag, value: params.value });
  },
  favoriteStudy({ commit, dispatch }, params) {
    const index = state.studies.findIndex((study) => study.StudyInstanceUID.Value[0] === params.StudyInstanceUID);
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    const request = `/studies/${params.StudyInstanceUID}/favorites`;
    if (params.value === true) {
      return dispatch('addFavorite', { request: (request + queries) }).then(() => {
        commit('SET_STUDY_FLAG', { index, flag: 'is_favorite', value: params.value });
        return true;
      });
    }
    return dispatch('removeFavorite', { request: (request + queries) }).then(() => {
      commit('SET_STUDY_FLAG', { index, flag: 'is_favorite', value: params.value });
      return true;
    });
  },
  addFavorite(context, params) {
    return HTTP.put(params.request).then((res) => res).catch((err) => Promise.reject(err));
  },
  removeFavorite(context, params) {
    return HTTP.delete(params.request).then((res) => res).catch((err) => Promise.reject(err));
  },
  deleteStudy({ commit }, params) {
    const request = `/studies/${params.StudyInstanceUID}`;
    return HTTP.delete(request).then(() => {
      commit('DELETE_STUDY', { StudyInstanceUID: params.StudyInstanceUID });
      commit('DELETE_SERIE_STUDY', { StudyInstanceUID: params.StudyInstanceUID });
      return true;
    }).catch((err) => {
      console.log(err);
      return false;
    });
  },
  sendStudy(context, params) {
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    const request = `/studies/${params.StudyInstanceUID}/users/${params.userSub}`;
    return HTTP.put(request + queries).then((res) => res).catch((err) => Promise.reject(err));
  },
  selfAppropriateStudy(context, params) {
    const request = 'studies';
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    const { headers } = params;
    const promises = [];
    params.data.forEach((d) => {
      if (d.serie_id) {
        promises.push(HTTP.put(`${request}/${d.study_id}/series/${d.serie_id}${queries}`, {}, { headers })
          .then((res) => ({
            res,
            studyId: d.study_id,
            serieId: d.serie_id,
            albumId: d.album_id,
          }))
          .catch((err) => ({
            res: err,
            studyId: d.study_id,
            serieId: d.serie_id,
            albumId: d.album_id,
          })));
      } else {
        promises.push(HTTP.put(`${request}/${d.study_id}${queries}`, {}, { headers })
          .then((res) => ({
            res,
            studyId: d.study_id,
            albumId: d.album_id,
          }))
          .catch((err) => ({
            res: err,
            studyId: d.study_id,
            albumId: d.album_id,
          })));
      }
    });
    return axios.all(promises)
      .then((res) => res);
  },
  initModalities({ commit }) {
    commit('INIT_MODALITIES');
  },
  getInboxInfo({ commit }) {
    const request = 'inboxinfo';
    return HTTP.get(request).then((res) => {
      if (res.status === 200) {
        commit('SET_MODALITIES', res.data.modalities);
      } else {
        commit('SET_MODALITIES', []);
      }
      return res;
    }).catch((err) => {
      commit('SET_MODALITIES', []);
      Promise.reject(err);
    });
  },
};

// mutations
const mutations = {
  INIT_STUDIES(state) {
    state.studies = [];
  },
  SET_STUDIES(state, studies) {
    studies.forEach((study) => {
      state.studies.push(study);
    });
  },
  UPDATE_STUDIES(state, params) {
    state.studies.splice(params.index, 0, params.study);
  },
  REMOVE_LASTSTUDY(state) {
    if (state.studies.length > 0) {
      state.studies.splice(state.studies.length - 1, 1);
    }
  },
  SET_STUDY_FLAG(state, params) {
    const study = state.studies[params.index];
    study.flag[params.flag] = params.value;
    Vue.set(state.studies, params.index, study);
  },
  DELETE_STUDY(state, params) {
    const studyIdx = _.findIndex(state.studies, (s) => s.StudyInstanceUID.Value[0] === params.StudyInstanceUID);
    if (studyIdx > -1) {
      Vue.delete(state.studies, studyIdx);
    }
  },
  SET_STUDY_SHOW_DETAILS(state, params) {
    const study = state.studies[params.index];
    // eslint-disable-next-line
    study._showDetails = params.value;
    Vue.set(state.studies, params.indexStudy, study);
  },
  INIT_MODALITIES(state) {
    state.modalities = [];
  },
  SET_MODALITIES(state, newModalities) {
    state.modalities = newModalities;
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
