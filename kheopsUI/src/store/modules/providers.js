import Vue from 'vue';
import { HTTP } from '@/router/http';

// initial state
const state = {
  providers: [],
  provider: {},
};

const getters = {
  providers: (state) => state.providers,
  provider: (state) => state.provider,
};

const actions = {
  initProvider({ commit }) {
    commit('INIT_PROVIDER');
  },
  getProviders({ commit, dispatch }, params) {
    const providers = [];
    const { albumID } = params;
    return HTTP.get(`/albums/${albumID}/reportproviders`, '', { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => {
      if (res.status === 200) {
        res.data.forEach((provider) => {
          const newProvider = provider;
          newProvider.stateURL = {};
          newProvider.stateURL.loading = true;
          newProvider.stateURL.checkURL = false;
          providers.push(newProvider);
        });
        commit('SET_PROVIDERS', { providers });
        dispatch('setCheckURLProviders', { providers });
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  getProvider({ commit, dispatch }, params) {
    const { albumID } = params;
    const { clientID } = params;
    return HTTP.get(`/albums/${albumID}/reportproviders/${clientID}`, '', { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => {
      if (res.status === 200) {
        const provider = res.data;
        provider.stateURL = {};
        provider.stateURL.loading = true;
        provider.stateURL.checkURL = false;
        commit('SET_PROVIDER', { provider });
        dispatch('setCheckURLProvider', { provider, commit: 'UPDATE_PROVIDER' });
      }
      return res;
    }).catch((err) => Promise.reject(err));
  },
  postProvider(context, params) {
    const { albumID } = params;
    let queries = '';
    Object.keys(params.query).forEach((key) => {
      queries += `${key}=${params.query[key]}&`;
    });
    return HTTP.post(`/albums/${albumID}/reportproviders`, queries, { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => res).catch((err) => err);
  },
  deleteProvider(context, params) {
    const { albumID } = params;
    const { clientID } = params;
    return HTTP.delete(`/albums/${albumID}/reportproviders/${clientID}`, '', { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => res).catch((err) => err);
  },
  updateProvider(context, params) {
    const { albumID } = params.paramsURL;
    const { clientID } = params.paramsURL;
    let queries = '';
    Object.keys(params.query).forEach((key) => {
      queries += `${key}=${params.query[key]}&`;
    });
    return HTTP.patch(`/albums/${albumID}/reportproviders/${clientID}`, queries, { headers: { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => res).catch((err) => err);
  },
  setCheckURLProvider({ commit }, params) {
    const query = `url=${params.provider.url}`;
    const { provider } = params;
    return HTTP.post('/reportproviders/metadata', query, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then((res) => {
      provider.stateURL.loading = false;
      provider.stateURL.checkURL = (res.data.valid === true);
      provider.data = res.data;
      commit(params.commit, { provider });
      return res;
    }).catch((err) => {
      provider.stateURL.loading = false;
      provider.stateURL.checkURL = false;
      commit(params.commit, { provider });
      return err;
    });
  },
  setCheckURLProviders({ dispatch }, params) {
    params.providers.forEach((provider) => {
      dispatch('setCheckURLProvider', { provider, commit: 'UPDATE_PROVIDERS' });
    });
  },
};

const mutations = {
  INIT_PROVIDER(state) {
    state.provider = {};
  },
  INIT_PROVIDERS(state) {
    state.providers = [];
  },
  UPDATE_PROVIDER(state, params) {
    state.provider.stateURL = params.provider.stateURL;
    state.provider.data = params.provider.data;
  },
  UPDATE_PROVIDERS(state, params) {
    const indexProvider = state.providers.findIndex((provider) => provider.client_id === params.provider.client_id);
    Vue.set(state.providers, indexProvider, params.provider);
  },
  ADD_PROVIDER(state, params) {
    state.providers.push(params.provider);
  },
  SET_PROVIDER(state, params) {
    state.provider = params.provider;
  },
  SET_PROVIDERS(state, params) {
    state.providers = params.providers;
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
