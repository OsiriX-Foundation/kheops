import { HTTP } from '@/router/http'
import httpoperations from '@/mixins/httpoperations'
import axios from 'axios'
import Vue from 'vue'

// initial state
const state = {
	albums: [],
	defaultFlagAlbum: {
		is_selected: false
	}
}

// getters
const getters = {
	albums: state => state.albums
}

// actions
const actions = {
	initAlbums ({ commit }, params) {
		commit('INIT_ALBUMS')
	},
	getAlbums ({ commit }, params) {
		let request = 'albums'
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		return HTTP.get(`${request}${queries}`, { headers: { 'Accept': 'application/json' } }).then(res => {
			let albums = []
			res.data.forEach(album => {
				Object.assign(album, { flag: state.defaultFlagAlbum })
				albums.push(album)
			})
			commit('SET_ALBUMS', albums)
			return res
		}).catch(err => {
			return err
		})
	},
	putStudiesInAlbum ({ commit }, params) {
		let request = 'studies'
		let queries = ''
		if (params.queries !== undefined) {
			queries = httpoperations.getQueriesParameters(params.queries)
		}
		let promises = []
		params.data.forEach(d => {
			if (d.serie_id) {
				promises.push(
					HTTP.put(`${request}/${d.study_id}/series/${d.serie_id}/albums/${d.album_id}${queries}`)
						.then(res => {
							return {
								res: res,
								studyId: d.study_id,
								serieId: d.serie_id,
								albumId: d.album_id
							}
						})
						.catch(err => {
							return {
								res: err,
								studyId: d.study_id,
								serieId: d.serie_id,
								albumId: d.album_id
							}
						})
				)
			} else {
				promises.push(
					HTTP.put(`${request}/${d.study_id}/albums/${d.album_id}${queries}`).then(res => {
						return {
							res: res,
							studyId: d.study_id,
							albumId: d.album_id
						}
					}).catch(err => {
						return {
							res: err,
							studyId: d.study_id,
							albumId: d.album_id
						}
					})
				)
			}
		})
		return axios.all(promises).then(results => {
			console.log(results)
			return results
		})
	},
	addUser ({ commit }, params) {
		let request = `albums/${params.album_id}/users/${params.user_id}`
		return HTTP.put(request).then(res => {
			console.log(res)
			return res
		}).catch(err => {
			console.log(err)
			return err
		})
	},
	setFlagAlbum ({ commit }, params) {
		let index = state.albums.findIndex(album => {
			return album.album_id === params.album_id
		})
		commit('SET_ALBUM_FLAG', { index: index, flag: params.flag, value: params.value })
	},
	setValueAlbum ({ commit }, params) {
		let index = state.albums.findIndex(album => {
			return album.album_id === params.album_id
		})
		commit('UPDATE_ALBUM', { index: index, flag: params.flag, value: params.value })
	},
	manageFavoriteAlbum ({ commit }, params) {
		let request = `albums/${params.album_id}/favorites`
		if (params.value === true) {
			return HTTP.put(`${request}`).then(res => {
				return res
			}).catch(err => {
				return err
			})
		} else if (params.value === false) {
			return HTTP.delete(`${request}`).then(res => {
				return res
			}).catch(err => {
				return err
			})
		}
	}
}

// mutations
const mutations = {
	INIT_ALBUMS (state) {
		state.albums = []
	},
	SET_ALBUMS (state, albums) {
		albums.forEach(album => {
			state.albums.push(album)
		})
	},
	SET_ALBUM_FLAG (state, params) {
		let album = state.albums[params.index]
		album.flag[params.flag] = params.value
		Vue.set(state.albums, params.index, album)
	},
	UPDATE_ALBUM (state, params) {
		let album = state.albums[params.index]
		album[params.flag] = params.value
		Vue.set(state.albums, params.index, album)
	}
}

export default {
	state,
	getters,
	actions,
	mutations
}
