import { HTTP } from '@/router/http'
// initial state
const state = {
	album: {}

}

// getters
const getters = {
	albumTest: state => state.album
}

// actions
const actions = {
	getAlbumTest ({ commit }, params) {
        let request = `albums/${params.album_id}`
		return HTTP.get(request, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_ALBUM_TEST', res.data)
            return res
		}).catch(err => {
            return err
        })
	}

}

// mutations
const mutations = {
    INIT_ALBUM (state) {
        state.album = {}
    },
	SET_ALBUM_TEST (state, album) {
		state.album = album
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
