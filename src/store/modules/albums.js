import axios from 'axios';
import Vue from 'vue';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

// initial state
const state = {
  albums: {},
  defaultFlagAlbum: {
    is_selected: false,
    is_hover: false,
  },
};

// getters
const getters = {
  albums: (state) => state.albums,
  getAlbumsByKey: (state) => (key) => {
    if (state.albums[key] !== undefined) {
      return state.albums[key];
    }
    return [];
  },
};

// actions
const actions = {
  initAlbums({ commit }, params) {
    commit('INIT_ALBUMS', params.key);
  },
  getAlbums({ commit }, params) {
    const request = 'albums';
    if (!Object.prototype.hasOwnProperty.call(state.albums, params.key)) {
      commit('INIT_ALBUMS', params.key);
    }
    let headers = {};
    if (params.headers !== undefined) {
      headers = params.headers;
    }
    headers.Accept = 'application/json';
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(`${request}${queries}`, { headers }).then((res) => {
      const albums = [];
      res.data.forEach((album) => {
        Object.assign(album, { flag: JSON.parse(JSON.stringify(state.defaultFlagAlbum)) });
        albums.push(album);
      });
      if (params.queries.offset === 0) {
        commit('INIT_ALBUMS', params.key);
      }
      commit('SET_ALBUMS', { albums, key: params.key });
      return res;
    }).catch((err) => Promise.reject(err));
  },
  putStudiesInAlbum(context, params) {
    const request = 'studies';
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    const { headers } = params;
    const promises = [];
    params.data.forEach((d) => {
      if (d.serie_id) {
        promises.push(
          HTTP.put(`${request}/${d.study_id}/series/${d.serie_id}/albums/${d.album_id}${queries}`, {}, { headers })
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
            })),
        );
      } else {
        promises.push(
          HTTP.put(`${request}/${d.study_id}/albums/${d.album_id}${queries}`, {}, { headers }).then((res) => ({
            res,
            studyId: d.study_id,
            albumId: d.album_id,
          })).catch((err) => ({
            res: err,
            studyId: d.study_id,
            albumId: d.album_id,
          })),
        );
      }
    });
    return axios.all(promises);
  },
  addUser(context, params) {
    const request = `albums/${params.album_id}/users/${params.user_id}`;
    return HTTP.put(request).then((res) => res).catch((err) => err);
  },
  setValueAlbum({ commit }, params) {
    const index = state.albums[params.key].findIndex((album) => album.album_id === params.album_id);
    commit('UPDATE_ALBUM', {
      index, flag: params.flag, value: params.value, key: params.key,
    });
  },
  manageFavoriteAlbum(context, params) {
    const request = `albums/${params.album_id}/favorites`;
    const method = params.value === true ? 'put' : 'delete';
    return HTTP[method](`${request}`).then((res) => res).catch((err) => err);
  },
};

// mutations
const mutations = {
  INIT_ALBUMS(state, key) {
    Vue.set(state.albums, key, []);
  },
  SET_ALBUMS(state, params) {
    params.albums.forEach((album) => {
      state.albums[params.key].push(album);
    });
  },
  UPDATE_ALBUM(state, params) {
    const album = state.albums[params.key][params.index];
    album[params.flag] = params.value;
    Vue.set(state.albums[params.key], params.index, album);
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
