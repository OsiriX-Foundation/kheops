// initial state
const state = {
	sending: false,
	loading: false,
	files: [],
	totalSize: 0,
	error: [],
	source: ''
}

const getters = {
	sending: state => state.sending,
	loading: state => state.loading,
	files: state => state.files,
	totalSize: state => state.totalSize,
	error: state => state.error,
	source: state => state.source
}

const actions = {
	setLoading ({ commit }, params) {
		commit('SET_LOADING', params)
	},
	setSending ({ commit }, params) {
		commit('SET_SENDING', params)
	},
	setFiles ({ commit }, params) {
		commit('SET_TOTAL_SIZE', params)
		commit('SET_FILES', params)
	},
	setErrorFiles ({ commit }, params) {
		commit('SET_ERROR', params)
	},
	setSource ({ commit }, params) {
		commit('SET_SOURCE', params)
	},
	initErrorFiles ({ commit }) {
		commit('INIT_ERROR')
	},
	initFiles ({ commit }) {
		commit('INIT_FILES')
	},
	initSentFiles ({ commit }) {
		commit('INIT_SENTFILES')
	},
	initSource ({ commit }) {
		commit('INIT_SOURCE')
	},
	removeFilesId ({ commit }, params) {
		params.files.forEach((val) => {
			commit('REMOVE_FILE_ID', { id: val.id })
		})
	}
}

const mutations = {
	SET_LOADING (state, params) {
		state.loading = params.loading
	},
	SET_SENDING (state, params) {
		state.sending = params.sending
	},
	SET_FILES (state, params) {
		state.files = params.files
	},
	SET_TOTAL_SIZE (state, params) {
		state.totalSize = params.files.length
	},
	SET_ERROR (state, params) {
		state.error.push(params.error)
	},
	SET_SOURCE (state, params) {
		state.source = params.source
	},
	INIT_ERROR (state) {
		state.error = []
	},
	INIT_FILES (state) {
		state.files = []
	},
	INIT_SOURCE (state) {
		state.source = ''
	},
	REMOVE_FILE_ID (state, params) {
		let index = state.files.findIndex(x => x.id === params.id)
		state.files.splice(index, 1)
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
