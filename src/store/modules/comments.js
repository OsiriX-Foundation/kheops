import Vue from 'vue';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

// initial state
const state = {
  comments: {},
};

// getters
const getters = {
  comments: (state) => state.comments,
  getCommentsByUID: (state) => (uid) => {
    if (state.comments[uid] !== undefined) {
      return state.comments[uid];
    }
    return [];
  },
};

// actions
const actions = {
  getStudyComments({ commit }, params) {
    const request = `studies/${params.StudyInstanceUID}/comments`;
    let queries = '';

    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }

    return HTTP.get(`${request}${queries}`, { headers: { Accept: 'application/json' } }).then((res) => {
      commit('SET_COMMENTS', { StudyInstanceUID: params.StudyInstanceUID, comments: res.data.reverse() });
      return res;
    }).catch((err) => Promise.reject(err));
  },
  postStudyComment(context, params) {
    const request = `studies/${params.StudyInstanceUID}/comments`;
    let queries = '';

    if (params.queries !== undefined) {
      queries = httpoperations.getFormData(params.queries);
    }
    return HTTP.post(request, queries, { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => res).catch((err) => Promise.reject(err));
  },
  getAlbumComments({ commit }, params) {
    const request = `albums/${params.album_id}/events`;
    let queries = '';

    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(`${request}${queries}`, { headers: { Accept: 'application/json' } }).then((res) => {
      commit('SET_COMMENTS', { StudyInstanceUID: params.album_id, comments: res.data.reverse() });
      return res;
    }).catch((err) => Promise.reject(err));
  },
  postAlbumComment(context, params) {
    const request = `albums/${params.album_id}/comments`;
    let queries = '';

    if (params.queries !== undefined) {
      queries = httpoperations.getFormData(params.queries);
    }
    return HTTP.post(request, queries, { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => res).catch((err) => Promise.reject(err));
  },
};

// mutations
const mutations = {
  INIT_COMMENTS(state) {
    state.comments = {};
  },
  SET_COMMENTS(state, params) {
    Vue.set(state.comments, params.StudyInstanceUID, params.comments);
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
