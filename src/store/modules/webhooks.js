// initial state
const state = {
  webhooks: [],
};

const getters = {
  source: (state) => state.webhooks,
};

const actions = {
  setWebhooks({ commit }, source) {
    commit('SET_WEBHOOKS', source);
  },
  initWebhooks({ commit }) {
    commit('INIT_WEBHOOKS');
  },
};

const mutations = {
  SET_WEBHOOKS(state, webhooks) {
    state.webhooks = webhooks;
  },
  INIT_WEBHOOKS(state) {
    state.webhooks = [];
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
