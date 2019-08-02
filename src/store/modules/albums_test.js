import { HTTP } from '@/router/http'
import httpoperations from '@/mixins/httpoperations'
import axios from 'axios'

// initial state
const state = {
	albums: []
}

// getters
const getters = {
	albumsTest: state => state.albums
}

// actions
const actions = {
	initAlbumsTest ({ commit }, params) {
		commit('INIT_ALBUMS_TEST')
	},
	getAlbumsTest ({ commit }, params) {
		let request = 'albums'
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(`${request}${queries}`, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_ALBUMS_TEST', res.data)
			return res
		}).catch(err => {
			return err
		})
	},
	putStudiesInAlbumTest ({ commit }, params) {
		let request = 'studies'
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		let promises = []
		params.data.forEach(d => {
			if (d.serie_id) {
				promises.push(HTTP.put(`${request}/${d.study_id}/series/${d.serie_id}/albums/${d.album_id}${queries}`))
			} else {
				promises.push(HTTP.put(`${request}/${d.study_id}/albums/${d.album_id}${queries}`))
			}
		})
		axios.all(promises).then(results => {
			console.log(results)
		})
	}
}

// mutations
const mutations = {
	INIT_ALBUMS_TEST (state) {
		state.albums = []
	},
	SET_ALBUMS_TEST (state, albums) {
		albums.forEach(album => {
			state.albums.push(album)
		})
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
