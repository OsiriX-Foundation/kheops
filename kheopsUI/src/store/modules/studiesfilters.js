// initial state
const state = {
  filters: {
    PatientName: '',
    PatientID: '',
    StudyDescription: '',
    StudyDateFrom: '',
    StudyDateTo: '',
    ModalitiesInStudy: '',
  },
  initFilters: {
    PatientName: '',
    PatientID: '',
    StudyDescription: '',
    StudyDateFrom: '',
    StudyDateTo: '',
    ModalitiesInStudy: '',
  },
  showFilters: false,
  initShowFilters: false,
};

const getters = {
  filters: (state) => state.filters,
  showFilters: (state) => state.showFilters,
};

const actions = {
  initFilters({ commit }) {
    commit('INIT_FILTERS');
  },
  setFilters({ commit }, filters) {
    commit('SET_FILTERS', filters);
  },
  initShowFilters({ commit }) {
    commit('INIT_SHOWFILTERS');
  },
  setShowFilters({ commit }, showFilters) {
    commit('SET_SHOWFILTERS', showFilters);
  },
};

const mutations = {
  INIT_FILTERS(state) {
    state.filters = { ...state.filters, ...state.initFilters };
  },
  SET_FILTERS(state, filters) {
    state.filters = { ...state.filters, ...filters };
  },
  INIT_SHOWFILTERS(state) {
    state.showFilters = state.initShowFilters;
  },
  SET_SHOWFILTERS(state, showFilters) {
    state.showFilters = showFilters;
  },
};

export default {
  state,
  getters,
  actions,
  mutations,
};
