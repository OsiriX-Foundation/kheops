/* eslint-disable */

<i18n>
{
	"en": {
		"newalbum": "New album",
		"Study #": "study #",
		"Modalities": "modalities",
		"User #": "user #",
		"Message #": "message #",
		"Date": "date",
		"LastEvent": "last event"
	},
	"fr": {
		"newalbum": "Nouvel album",
		"Study #": "# Etudes",
		"Modalities": "modalités",
		"User #": "# utilisateurs",
		"Message #": "# messages",
		"Date": "date",
		"LastEvent": "dern. evnt"
	}
}
</i18n>

<template>
	<div class = 'container-fluid'>
		<div class="my-3 selection-button-container" style = ' position: relative;'>
			<h3 >
				<router-link to="albums/new" class="nav-link" active-class="active" style = 'display: inline'><v-icon name = 'plus'></v-icon>{{$t('newalbum')}}</router-link>
				<button type = 'button' class = "btn btn-link btn-lg float-right" @click='showFilters=!showFilters' style='position: absolute; right: 20px;top: 0'><v-icon name = 'search' scale='2'/></button>
			</h3>
		</div>
		<b-table  striped :items="albums" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"  @sort-changed="sortingChanged" :no-local-sorting="true" @row-clicked='selectAlbum' >

			<template slot="HEAD_is_selected" slot-scope="data">
				{{$t(data.label)}}
				<b-button variant="link" size="sm"  class="mr-2" >
					<v-icon  class="align-middle"   name="chevron-down" style = 'visibility: hidden'></v-icon>
				</b-button>
				<b-form-checkbox @click.native.stop @change="selectAll(albums.allSelected)" v-model="albums.allSelected" name="allSelected">
				</b-form-checkbox>
			</template>
			<template slot="HEAD_name" slot-scope="data">
				<div v-if='showFilters' @click.stop='' ><input type = 'search' class = 'form-control form-control-sm' v-model='filters.name' :placeholder="$t('filter')"> <br/></div>
				{{$t(data.label)}}

			</template>
			<template slot="HEAD_number_of_studies" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.NbStudies' :placeholder="$t('filter')"> <br/></div>
				{{$t(data.label)}}
			</template>
			<template slot="HEAD_modalities" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.ModalitiesInStudy' :placeholder="$t('filter')"> <br/></div>
				{{$t(data.label)}}
			</template>
			<template slot="HEAD_number_of_users" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.NbUsers' :placeholder="$t('filter')"> <br/></div>
				{{$t(data.label)}}
			</template>
			<template slot="HEAD_number_of_comments" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.NbMessages' :placeholder="$t('filter')"> <br/></div>
				{{$t(data.label)}}
			</template>
			<template slot="HEAD_created_time" slot-scope="data">
				<div v-if='showFilters' @click.stop='' class = 'form-row'>

					<div class = 'col form-inline'>
						<div class = 'form-group'>
							<datepicker v-model="filters.CreateDateFrom"  :bootstrap-styling='false' :disabledDates="disabledFromCreateDates" input-class="form-control form-control-sm  search-calendar" :calendar-button="false" calendar-button-icon=""  wrapper-class='calendar-wrapper' :placeholder="$t('fromDate')" :clear-button="true" clear-button-icon='fa fa-times'></datepicker>
						</div>
					</div>

					<div class = 'col form-inline'>
						<div class = 'form-group'>
							<datepicker v-model="filters.CreateDateTo" :bootstrap-styling='false' :disabledDates="disabledToCreateDates"  input-class="form-control form-control-sm search-calendar" :calendar-button="false"  calendar-button-icon="" wrapper-class='calendar-wrapper' :placeholder="$t('toDate')" :clear-button="true" clear-button-icon='fa fa-times'></datepicker>
						</div>
					</div>
					<!-- <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateFrom' placeholder="From"> - <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateTo' placeholder="To"> <br> -->
				</div>
				<br v-if='showFilters' />
				{{$t(data.label)}}

			</template>
			<template slot="HEAD_last_event_time" slot-scope="data">
				<div v-if='showFilters' @click.stop='' class = 'form-row'>

					<div class = 'col form-inline'>
						<div class = 'form-group'>
							<datepicker v-model="filters.EventDateFrom"  :bootstrap-styling='false' :disabledDates="disabledFromEventDates" input-class="form-control form-control-sm  search-calendar" :calendar-button="false" calendar-button-icon=""  wrapper-class='calendar-wrapper' :placeholder="$t('fromDate')" :clear-button="true" clear-button-icon='fa fa-times'></datepicker>
						</div>
					</div>

					<div class = 'col form-inline'>
						<div class = 'form-group'>
							<datepicker v-model="filters.EventDateTo" :bootstrap-styling='false' :disabledDates="disabledToEventDates"  input-class="form-control form-control-sm search-calendar" :calendar-button="false"  calendar-button-icon="" wrapper-class='calendar-wrapper' :placeholder="$t('toDate')" :clear-button="true" clear-button-icon='fa fa-times'></datepicker>
						</div>
					</div>
					<!-- <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateFrom' placeholder="From"> - <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateTo' placeholder="To"> <br> -->
				</div>
				<br v-if='showFilters' />
				{{$t(data.label)}}

			</template>
			<template slot="is_selected" slot-scope="row">
				<b-form-group>
					<b-button variant="link" size="sm"  class="mr-2" @click.stop="row.toggleDetails">
						<v-icon v-if= "row.detailsShowing" class="align-middle" name="chevron-down"></v-icon>
						<v-icon v-else class="align-middle" name="chevron-right"></v-icon>
					</b-button>
					<b-form-checkbox v-model = "row.item.is_selected" @click.native.stop @change="toggleSelected(row.item,'album',!row.item.is_selected)" >
					</b-form-checkbox>

				</b-form-group>
			</template>
			<template slot = 'name' slot-scope='data'>
				<div class = 'nameContainer'>
					{{data.item.name}}
					<div class = 'nameIcons'>
						<span @click.stop = "toggleFavorite(data.index)" :class="data.item.is_favorite?'selected':''">
							<v-icon  v-if="data.item.is_favorite" class="align-middle" style="margin-right:0" name="star"></v-icon>
							<v-icon v-else class="align-middle" style="margin-right:0" name="star" color="grey" :invert="true"></v-icon>
						</span>
					</div>
				</div>
			</template>

			<template slot = 'created_time' slot-scope='data'>{{data.item.created_time | formatDate}}</template>
			<template slot = 'last_event_time' slot-scope='data'>{{data.item.last_event_time | formatDate}}</template>

			<template slot="row-details" slot-scope="row">
				<b-card>
					<dl><dt>Description</dt><dd>{{row.item.description}}</dd></dl>
				</b-card>
			</template>

		</b-table>
	</div>
</template>
<script>

import { mapGetters } from 'vuex'
import Datepicker from 'vuejs-datepicker'

export default {
	name: 'albums',
	data () {
		return {
			pageNb: 1,
			active: false,
			fields: [
				{
					key: 'is_selected',
					label: '',
					sortable: true,
					class: 'td_checkbox'
				},
				{
					key: 'name',
					label: 'name',
					tdClass: 'name',
					sortable: true
				},
				{
					key: 'number_of_studies',
					label: 'Study #',
					sortable: true
				},
				{
					key: 'modalities',
					label: 'Modalities',
					sortable: true
				},
				{
					key: 'number_of_users',
					label: 'User #',
					sortable: true
				},
				{
					key: 'number_of_comments',
					label: 'Messages #',
					sortable: true
				},
				{
					key: 'created_time',
					label: 'Date',
					sortable: true
				},
				{
					key: 'last_event_time',
					label: 'LastEvent',
					sortable: true
				}
			],
			sortBy: 'created_time',
			sortDesc: true,
			limit: 8,
			optionsNbPages: [5, 10, 25, 50, 100],
			showFilters: false,
			filterTimeout: null,
			filters: {
				name: '',
				number_of_studies: '',
				modalities: '',
				number_of_users: '',
				number_of_comments: '',
				CreateDateFrom: '',
				CreateDateTo: '',
				EventDateFrom: '',
				EventDateTo: ''
			}

		}
	},
	components: { Datepicker },
	computed: {
		...mapGetters({
			albums: 'albums'
		}),
		totalRows () {
			return this.albums.length
		},
		selectedAlbumsNb () {
			return _.filter(this.albums, s => { return s.is_selected === true }).length
		},
		disabledToCreateDates: function () {
			let vm = this
			return {
				to: vm.filters.CreateDateFrom,
				from: new Date()
			}
		},
		disabledFromCreateDates: function () {
			return {
				from: new Date()
			}
		},
		disabledToEventDates: function () {
			let vm = this
			return {
				to: vm.filters.EventDateFrom,
				from: new Date()
			}
		},
		disabledFromEventDates: function () {
			return {
				from: new Date()
			}
		}

	},
	methods: {
		scroll () {
			window.onscroll = () => {
				let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight

				if (bottomOfWindow) {
					this.pageNb++
					this.$store.dispatch('getAlbums', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
				}
			}
		},
		sortingChanged (ctx) {
			// ctx.sortBy   ==> Field key for sorting by (or null for no sorting)
			// ctx.sortDesc ==> true if sorting descending, false otherwise

			this.pageNb = ctx.currentPage
			this.sortBy = ctx.sortBy
			this.sortDesc = ctx.sortDesc
			this.limit = this.albums.length
			this.$store.dispatch('getAlbums', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
		},
		toggleSelected (item, type, isSelected) {
			let index = _.findIndex(this.albums, s => { return s.album_id === item.album_id })
			this.$store.dispatch('toggleSelectedAlbum', { type: type, index: index, is_selected: isSelected }).then(() => {
			})
		},

		toggleFavorite (index) {
			var vm = this
			this.$store.dispatch('toggleFavorite', { type: this.$route.name, index: index }).then(res => {
				if (res[1] === false) vm.$snotify.error('Sorry, an error occured')
			})
		},
		handleComments (index, entity) {
			this.albums[index][entity] = !this.albums[index][entity]
		},
		selectAll (isSelected) {
			this.$store.commit('SELECT_ALL_ALBUMS', !isSelected)
			this.albums.allSelected = !this.albums.allSelected
		},
		deleteSelectedAlbums () {
			var vm = this
			var i
			for (i = this.albums.length - 1; i > -1; i--) {
				if (this.albums[i].is_selected) {
					vm.$store.dispatch('deleteAlbum', { album_id: this.albums[i].album_id })
					vm.$delete(vm.albums, i)
				}
			}
		},
		downloadSelectedAlbums () {
			var vm = this
			_.forEach(this.albums, function (album) {
				if (album.is_selected) {
					vm.$store.dispatch('downloadAlbum', { album_id: album.album_id })
				}
			})
		},
		searchOnline () {
			this.$store.dispatch('getAlbums', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
		},
		selectAlbum (item) {
			if (item.album_id) {
				this.$router.push('/albums/' + item.album_id)
			}
		}

	},
	created () {
		this.$store.dispatch('getAlbums', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
	},
	mounted () {
		this.scroll()
	},
	watch: {
		filters: {
			handler: function (filters) {
				if (this.filterTimeout) {
					clearTimeout(this.filterTimeout)
				}
				this.filterTimeout = setTimeout(() => this.searchOnline(filters), 300)
			},
			deep: true
		},
		showFilters: {
			handler: function (showFilters) {
				if (!showFilters) {
					this.filters = {
					}
				}
			}
		}
	}

}

</script>

<style scoped>
table th{
	text-transform: capitalize;
}
select{
	display: inline !important;
}
.btn-link {
	font-weight: 400;
	color: white;
	background-color: transparent;
}

.btn-link:hover {
	color: #c7d1db;
	text-decoration: underline;
	background-color: transparent;
	border-color: transparent;
}

.selection-button-container{
	height: 60px;
}

.td_checkbox {
	width: 150px;
}

input.search-calendar{
	width: 100px !important;
}

div.calendar-wrapper{
	color: #333;
}

.nameContainer{
	position: relative;
	white-space: nowrap;
	cursor: pointer;
}

.nameIcons{
	margin-left: 10px;
	visibility: hidden;
	display: inline;
	cursor: pointer;
}

.name:hover .nameIcons {
	visibility:visible;
}

.nameIcons > span.selected{
	visibility:visible !important;
}

.nameIcons span{
	margin: 0 3px;
}

</style>
