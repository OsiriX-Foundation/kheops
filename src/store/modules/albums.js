import { HTTP } from '@/router/http'
import axios from 'axios'
import moment from 'moment'
// initial state
const state = {
	all: [],
	flags: {},
	totalItems: null,
	filterParams: {
		sortBy: 'created_time',
		sortDesc: true,
		limit: 100,
		pageNb: 1,
		filters: {
			name: '',
			number_of_studies: '',
			number_of_users: '',
			number_of_comments: '',
			modalities: '',
			last_event_time: '',
			created_time: ''
		},
		canCreateCapabilityToken: false
	},
	request: ''
}

// getters
const getters = {
	albums: state => state.all
}

// actions
const actions = {

	getAlbums ({ commit }, params) {
		if (state.totalItems !== null && state.all.length >= state.totalItems && state.filterParams.sortBy === params.sortBy && state.filterParams.sortDesc === params.sortDesc && state.filterParams.canCreateCapabilityToken === params.canCreateCapabilityToken && _.isEqual(state.filterParams.filters, params.filters)) {
			return
		}
		//
		var reset = false; var request

		let requestParams = ''
		_.forEach(params.filters, function (value, filterName) {
			if (filterName.indexOf('Date') === -1) {
				if (value) {
					requestParams += '&' + filterName + '=' + value + '*'
				}
			}
		})

		if (params.filters !== undefined) {
			if (params.filters.CreateDateFrom || params.filters.CreateDateTo) {
				let fromDate = ''
				let toDate = ''
				if (params.filters.CreateDateFrom) {
					fromDate = moment(params.filters.CreateDateFrom).format('YYYYMMDD')
				}
				if (params.filters.CreateDateTo) {
					toDate = moment(params.filters.CreateDateTo).format('YYYYMMDD')
				}
				requestParams += '&created_time=' + fromDate + '-' + toDate
			}

			if (params.filters.EventDateFrom || params.filters.EventDateTo) {
				let fromDate = ''
				let toDate = ''
				if (params.filters.EventDateFrom) {
					fromDate = moment(params.filters.EventDateFrom).format('YYYYMMDD')
				}
				if (params.filters.EventDateTo) {
					toDate = moment(params.filters.EventDateTo).format('YYYYMMDD')
				}
				requestParams += '&last_event_time=' + fromDate + '-' + toDate
			}
		}

		let offset = 0
		if (state.filterParams.sortBy !== params.sortBy || state.filterParams.sortDesc !== params.sortDesc || state.request !== requestParams) {
			offset = 0
			params.limit = (state.all.length > 100) ? state.all.length : 100
			reset = true
		} else offset = (params.pageNb - 1) * params.limit
		let sortSense = (params.sortDesc) ? '-' : ''
		if (params.canCreateCapabilityToken) {
			request = 'albums?canCreateCapabilityToken=true&sort=name'
		} else {
			request = 'albums?limit=' + params.limit + '&offset=' + offset + (params.sortBy ? '&sort=' + sortSense + params.sortBy : '') + requestParams
		}
		HTTP.get(request, { headers: { 'Accept': 'application/json' } }).then(res => {
			commit('SET_TOTAL', res.headers['x-total-count'])
			let data = []
			_.forEach(res.data, d => {
				let album = {}
				_.forEach(d, (v, k) => {
					album[k] = v
					if (album.album_id !== undefined) {
						if (state.flags[album.album_id] === undefined) {
							let flag = {
								id: album.album_id,
								is_selected: false,
								is_favorite: d.is_favorite,
								comment: false
							}
							commit('SET_FLAG', flag)
						}
					}
				})
				if (album.album_id !== undefined) data.push(album)
			})
			commit('SET_ALBUMS', { data: data, reset: reset })
			commit('SET_ALBUM_FILTER_PARAMS', params)
			commit('SET_REQUEST_PARAMS', requestParams)
		})
	},
	toggleFavorite ({ commit }, params) {
		if (params.type === 'albums') {
			let isFavorite = !state.all[params.index].is_favorite
			let albumId = state.all[params.index].album_id
			if (isFavorite) {
				return HTTP.put('/albums/' + albumId + '/favorites').then(() => {
					console.log('OK ' + albumId + ' is in favorites')
					commit('TOGGLE_FAVORITE', params)
					return true
				}).catch(err => {
					console.error(err)
					return false
				})
			} else {
				return HTTP.delete('/albums/' + albumId + '/favorites').then(() => {
					console.log('KO ' + albumId + ' is NOT in favorites')
					commit('TOGGLE_FAVORITE', params)
					return true
				})
			}
		}
	},
	toggleSelectedAlbum ({ commit }, params) {
		commit('TOGGLE_SELECTED_ALBUM', params)
	},

	createAlbum ({ commit }, params) {
		/*
		TODO : A modifier lorsque 1a requêtes pour créer et ajouter users
		*/
		var query = ''
		_.forEach(params, (value, key) => {
			if (key !== 'users') query += encodeURIComponent(key) + '=' + encodeURIComponent(value) + '&'
		})
		return HTTP.post('albums', query, { headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded' } }).then(res => {
			commit('CREATE_ALBUM', res.data)
			params.users.forEach(user => {
				this.dispatch('add_user_to_album', { album_id: res.data.album_id, user_name: user.email })
			})
		}).catch(() => {
		})
	},

	putStudiesInAlbum ({ commit }, params) {
		let promises = []
		_.forEach(params.data, d => {
			if (d.series_id) {
				promises.push(HTTP.put('studies/' + d.study_id + '/series/' + d.series_id + '/albums/' + d.album_id))
			} else {
				promises.push(HTTP.put('studies/' + d.study_id + '/albums/' + d.album_id))
			}
		})
		axios.all(promises).then(results => {
			_.forEach(results, res => {
				if (res.status === 201) {
					commit('TOGGLE_SELECTED_STUDY', { type: 'all' })
				}
			})
		})
	}
}

// mutations
const mutations = {
	SET_ALBUMS (state, data) {
		let albums = data.data
		let reset = data.reset
		_.forEach(albums, (d, i) => {
			d.is_selected = false
			d.is_favorite = false
			d.comment = null

			_.forEach(state.flags, (flag, albumId) => {
				if (d.album_id === albumId) {
					albums[i].is_selected = flag.is_selected
					albums[i].is_favorite = flag.is_favorite
					albums[i].comment = flag.comment
				}
			})
		})
		if (reset) state.all = albums
		else {
			state.all = _.uniqBy(state.all.concat(albums), function (d) { return d.album_id })
		}
	},
	SELECT_ALL_ALBUMS (state, isSelected) {
		_.forEach(state.all, function (album) {
			album.is_selected = isSelected
		})
	},
	SELECT_ALBUM (state, album) {
		state.current = album
	},
	SET_ALBUM_FILTER_PARAMS (state, params) {
		state.filterParams.sortBy = params.sortBy
		state.filterParams.sortDesc = params.sortDesc
		state.filterParams.limit = params.limit
		state.filterParams.pageNb = params.pageNb
		if (params.filters !== undefined) {
			state.filterParams.filters.name = params.filters.name
			state.filterParams.filters.number_of_studies = params.filters.number_of_studies
			state.filterParams.filters.number_of_users = params.filters.number_of_users
			state.filterParams.filters.number_of_comments = params.filters.number_of_comments
			state.filterParams.filters.modalities = params.filters.modalities
		}
	},
	SET_TOTAL (state, value) {
		state.totalItems = value
	},
	TOGGLE_FAVORITE (state, params) {
		if (params.type === 'albums') {
			state.all[params.index].is_favorite = !state.all[params.index].is_favorite
			state.flags[state.all[params.index].album_id].is_favorite = state.all[params.index].is_favorite
		}
	},
	TOGGLE_SELECTED_ALBUM (state, params) {
		if (params.type === 'album') {
			state.all[params.index].is_selected = !state.all[params.index].is_selected
			state.flags[state.all[params.index].album_id].is_selected = state.all[params.index].is_selected
		}
	},
	SET_FLAG (state, flag) {
		state.flags[flag.id] = {
			is_selected: flag.is_selected,
			is_favorite: flag.is_favorite,
			comment: flag.comment
		}
	},
	SET_REQUEST_PARAMS (state, request) {
		state.request = request
	},
	CREATE_ALBUM (state, album) {
		state.all.push(album)
	}

}

export default {
	state,
	getters,
	actions,
	mutations
}
