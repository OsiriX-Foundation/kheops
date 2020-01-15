import axios from 'axios';
import Vue from 'vue';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

// initial state
const state = {
  albums: [],
  defaultFlagAlbum: {
    is_selected: false,
    is_hover: false,
  },
};

// getters
const getters = {
  albums: (state) => state.albums,
};

// actions
const actions = {
  initAlbums({ commit }) {
    commit('INIT_ALBUMS');
  },
  getAlbums({ commit }, params) {
    const request = 'albums';
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
        commit('INIT_ALBUMS');
      }
      commit('SET_ALBUMS', albums);
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
  setFlagAlbum({ commit }, params) {
    const index = state.albums.findIndex((album) => album.album_id === params.album_id);
    commit('SET_ALBUM_FLAG', { index, flag: params.flag, value: params.value });
  },
  setValueAlbum({ commit }, params) {
    const index = state.albums.findIndex((album) => album.album_id === params.album_id);
    commit('UPDATE_ALBUM', { index, flag: params.flag, value: params.value });
  },
  manageFavoriteAlbum(context, params) {
    const request = `albums/${params.album_id}/favorites`;
    const method = params.value === true ? 'put' : 'delete';
    return HTTP[method](`${request}`).then((res) => res).catch((err) => err);
  },
};

// mutations
const mutations = {
  INIT_ALBUMS(state) {
    state.albums = [];
  },
  SET_ALBUMS(state, albums) {
    albums.forEach((album) => {
      state.albums.push(album);
    });
  },
  SET_ALBUM_FLAG(state, params) {
    const album = state.albums[params.index];
    album.flag[params.flag] = params.value;
    Vue.set(state.albums, params.index, album);
  },
  UPDATE_ALBUM(state, params) {
    const album = state.albums[params.index];
    album[params.flag] = params.value;
    Vue.set(state.albums, params.index, album);
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
