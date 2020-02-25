import Vue from 'vue';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

// initial state
const state = {
  webhooks: [],
  webhook: {},
};

const getters = {
  webhooks: (state) => state.webhooks,
  webhook: (state) => state.webhook,
};

const actions = {
  getWebhooks({ commit }, params) {
    const headers = { Accept: 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' };
    const url = `albums/${params.albumId}/webhooks`;
    return HTTP.get(url, '', headers).then((res) => {
      if (res.status === 200) {
        commit('SET_WEBHOOKS', res.data);
      }
      return res;
    }).catch((err) => {
      throw err;
    });
  },
  updateWebhook({ commit }, params) {
    const url = `albums/${params.albumId}/webhooks/${params.webhookId}`;
    const queries = httpoperations.getFormData(params.queries);
    return HTTP.patch(url, queries).then((res) => {
      const webhook = res.data;
      commit('UPDATE_WEBHOOK', webhook);
      return res;
    }).catch((err) => {
      throw err;
    });
  },
  setWebhooks({ commit }, webhooks) {
    commit('SET_WEBHOOKS', { webhooks });
  },
  initWebhooks({ commit }) {
    commit('INIT_WEBHOOKS');
  },
  getWebhook({ commit }, params) {
    const url = `albums/${params.albumId}/webhooks/${params.webhookId}`;
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getQueriesParameters(params.queries);
    }
    return HTTP.get(url + queries).then((res) => {
      const webhook = res.data;
      commit('SET_WEBHOOK', webhook);
      return res;
    }).catch((err) => {
      throw err;
    });
  },
  editWebhook(ctx, params) {
    const queries = httpoperations.getFormData(params.queries);
    const url = `albums/${params.albumId}/webhooks/${params.webhookId}`;
    return HTTP.patch(url, queries).then((res) => res)
      .catch((err) => {
        throw err;
      });
  },
  initWebhook({ commit }) {
    commit('INIT_WEBHOOK');
  },
  removeWebhook({ commit }, params) {
    const url = `albums/${params.albumId}/webhooks/${params.webhookId}`;
    return HTTP.delete(url).then((res) => {
      commit('INIT_WEBHOOK');
      return res;
    }).catch((err) => {
      throw err;
    });
  },
};

const mutations = {
  SET_WEBHOOKS(state, webhooks) {
    state.webhooks = webhooks;
  },
  INIT_WEBHOOKS(state) {
    state.webhooks = [];
  },
  UPDATE_WEBHOOK(state, webhook) {
    const idx = _.findIndex(state.webhooks, (w) => w.id === webhook.id);
    Vue.set(state.webhooks, idx, webhook);
  },
  SET_WEBHOOK(state, webhook) {
    state.webhook = webhook;
  },
  INIT_WEBHOOK(state) {
    state.webhook = {};
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
