import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';
// initial state
const state = {
  album: {},
  users: [],
  tokens: [],
  validParamsToken: true,
};

// getters
const getters = {
  album: (state) => state.album,
  albumUsers: (state) => state.users,
  albumTokens: (state) => state.tokens,
  validParamsToken: (state) => state.validParamsToken,
};

// actions
const actions = {
  getAlbum({ commit }, params) {
    const request = `albums/${params.album_id}`;
    return HTTP.get(request, { headers: { Accept: 'application/json' } }).then((res) => {
      commit('SET_ALBUM', res.data);
      return res;
    }).catch((err) => Promise.reject(err));
  },
  createAlbum({ commit }, params) {
    const request = 'albums';
    let formData = '';
    if (params.formData !== undefined) {
      formData = httpoperations.getFormData(params.formData);
    }
    return HTTP.post(request, formData, { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => {
      if (res.status === 201) {
        commit('SET_ALBUM', res.data);
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  removeStudyInAlbum({ commit }, params) {
    const request = `studies/${params.StudyInstanceUID}/albums/${params.album_id}`;
    return HTTP.delete(request).then((res) => {
      if (res.status === 204) {
        commit('DELETE_STUDY', params);
        commit('DELETE_SERIE_STUDY', params);
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  removeSerieInAlbum({ commit }, params) {
    const request = `studies/${params.StudyInstanceUID}/series/${params.SeriesInstanceUID}/albums/${params.album_id}`;
    return HTTP.delete(request).then((res) => {
      if (res.status === 204) {
        commit('DELETE_SERIE', params);
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  setKeyValueAlbum(context, params) {
    state.album[params.key] = params.value;
  },
  getUsersAlbum({ commit }, params) {
    const request = `albums/${params.album_id}/users`;
    return HTTP.get(request, '', { headers: { Accept: 'application/json' } }).then((res) => {
      if (res.status === 200) {
        commit('SET_ALBUM_USERS', res.data);
      }
    }).catch((err) => Promise.reject(err));
  },
  editAlbum({ commit }, params) {
    const request = `albums/${params.album_id}`;
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.patch(request + queries, '', { headers: { Accept: 'application/json' } }).then((res) => {
      if (res.status === 200) {
        commit('SET_ALBUM', res.data);
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  deleteAlbum({ commit }, params) {
    const request = `albums/${params.album_id}`;
    return HTTP.delete(request).then((res) => {
      if (res.status === 204) {
        commit('INIT_ALBUM');
        commit('INIT_ALBUM_USERS');
      }
    }).catch((err) => Promise.reject(err));
  },
  addAlbumUser({ dispatch }, params) {
    const request = `albums/${params.album_id}/users/${params.user}`;
    return HTTP.put(request).then((res) => {
      if (res.status === 201) {
        dispatch('getUsersAlbum', { album_id: params.album_id });
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  removeAlbumUser({ dispatch }, params) {
    const request = `albums/${params.album_id}/users/${params.user}`;
    return HTTP.delete(request).then((res) => {
      if (res.status === 204) {
        dispatch('getUsersAlbum', { album_id: params.album_id });
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  manageAlbumUserAdmin({ dispatch }, params) {
    const request = `albums/${params.album_id}/users/${params.user_name}/admin`;
    const method = (params.user_is_admin) ? 'put' : 'delete';
    return HTTP[method](request).then((res) => {
      if (res.status === 204) {
        dispatch('getUsersAlbum', { album_id: params.album_id });
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  getAlbumTokens({ commit }, params) {
    const request = 'capabilities';
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(request + queries, '', { headers: { Accept: 'application/json' } }).then((res) => {
      if (res.status === 200) {
        commit('SET_ALBUM_TOKENS', res.data);
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  setModalitiesAlbum({ commit }) {
    commit('SET_MODALITIES', state.album.modalities);
  },
  initValidParamToken({ commit }) {
    commit('INIT_VALID_PARAM_TOKEN');
  },
  setValidParamToken({ commit }, value) {
    commit('SET_VALID_PARAM_TOKEN', value);
  },
};

// mutations
const mutations = {
  INIT_ALBUM(state) {
    state.album = {};
  },
  SET_ALBUM(state, album) {
    state.album = album;
  },
  INIT_ALBUM_USERS(state) {
    state.users = [];
  },
  SET_ALBUM_USERS(state, users) {
    state.users = users;
  },
  INIT_TOKENS(state) {
    state.tokens = [];
  },
  SET_ALBUM_TOKENS(state, tokens) {
    state.tokens = tokens;
  },
  SET_ALBUM_TOKEN(state, token) {
    state.tokens.push(token);
  },
  INIT_VALID_PARAM_TOKEN(state) {
    state.validParamsToken = true;
  },
  SET_VALID_PARAM_TOKEN(state, value) {
    state.validParamsToken = value;
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
