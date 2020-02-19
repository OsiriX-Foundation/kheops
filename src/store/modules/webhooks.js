// initial state
const state = {
  webhooks: [],
};

const getters = {
  webhooks: (state) => state.webhooks,
};

const actions = {
  setWebhooks({ commit }, webhooks) {
    commit('SET_WEBHOOKS', webhooks);
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
