// initial state
const state = {
  source: {},
};

const getters = {
  source: (state) => state.source,
};

const actions = {
  setSource({ commit }, source) {
    commit('SET_SOURCE', source);
  },
  initSource({ commit }) {
    commit('INIT_SOURCE');
  },
};

const mutations = {
  SET_SOURCE(state, source) {
    state.source = source;
  },
  INIT_SOURCE(state) {
    state.source = '';
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
