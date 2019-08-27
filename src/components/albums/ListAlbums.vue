<i18n>
{
	"en": {
		"newalbum": "New album",
		"Study #": "Study #",
		"Modalities": "Modalities",
		"User #": "User #",
		"Message #": "Message #",
		"Date": "Created date",
		"LastEvent": "Last event",
		"selectednbalbums": "{count} album is selected | {count} albums are selected",
		"permissionsfailed": "You can't send this albums : ",
    "send": "The album {albumName} has been shared.",
    "nomodality": "No modality",
		"name": "Name",
		"nomorealbums": "No more albums",
		"noresults": "No results",
		"albumshared": "Album shared",
		"error": "An error occur please reload the albums."
	},
	"fr": {
		"newalbum": "Nouvel album",
		"Study #": "# Etudes",
		"Modalities": "Modalités",
		"User #": "# Utilisateurs",
		"Message #": "# Messages",
		"Date": "Date de création",
		"LastEvent": "Dernier événement",
		"selectednbalbums": "{count} album est sélectionnée | {count} albums sont sélectionnées",
		"permissionsfailed": "Vous ne pouvez pas envoyer ces albums : ",
		"send": "L'album {albumName} a été partagé.",
    "nomodality": "Aucune modalité",
		"name": "Nom",
		"nomorealbums": "Pas d'albums en plus",
		"noresults": "Aucun results",
		"albumshared": "Album partagé",
		"error": "Une erreur s'est produite, veuillez recharger les albums."
	}
}
</i18n>
<template>
  <div>
    <list-albums-headers
      :disabled-btn-share="albumsSelected.length === 0"
      @inviteClick="form_send_album = true"
      @searchClick="showFilters = !showFilters"
    />

    <form-get-user
      v-if="form_send_album && albumsSelected.length > 0"
      @get-user="sendToUser"
      @cancel-user="form_send_album=false"
    />
    <b-table
      striped
      hover
      :items="albums"
      :fields="fields"
      :sort-desc="true"
      :no-local-sorting="true"
      :dark="false"
      :no-sort-reset="true"
      :tbody-class="'link'"
      @row-clicked="clickAlbum"
      @sort-changed="sortingChanged"
    >
      <template
        slot="HEAD_name"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.name"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('filter')"
          > <br>
        </div>
        <sort-list
          :sort-desc="albumsParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="albumsParams.sortBy"
        />
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_number_of_studies"
        slot-scope="data"
      >
        <sort-list
          :sort-desc="albumsParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="albumsParams.sortBy"
        />
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_number_of_users"
        slot-scope="data"
      >
        <sort-list
          :sort-desc="albumsParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="albumsParams.sortBy"
        />
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_number_of_comments"
        slot-scope="data"
      >
        <sort-list
          :sort-desc="albumsParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="albumsParams.sortBy"
        />
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_created_time"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          class="form-row"
          @click.stop=""
        >
          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.CreateDateFrom"
                :disabled-dates="disabledFromCreateDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('fromDate')"
              />
            </div>
          </div>
          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.CreateDateTo"
                :disabled-dates="disabledToCreateDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('toDate')"
              />
            </div>
          </div>
        </div>
        <br v-if="showFilters">
        <sort-list
          :sort-desc="albumsParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="albumsParams.sortBy"
        />
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_last_event_time"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          class="form-row"
          @click.stop=""
        >
          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.EventDateFrom"
                :disabled-dates="disabledFromEventDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('fromDate')"
              />
            </div>
          </div>

          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.EventDateTo"
                :disabled-dates="disabledToEventDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('toDate')"
              />
            </div>
          </div>
        </div>
        <br v-if="showFilters">
        <sort-list
          :sort-desc="albumsParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="albumsParams.sortBy"
        />
        {{ $t(data.label) }}
      </template>
      <template
        slot="is_selected"
        slot-scope="row"
      >
        <b-button-group>
          <b-form-checkbox
            v-if="row.item.is_admin || row.item.add_user"
            v-model="row.item.is_selected"
            inline
            @click.native.stop
          />
        </b-button-group>
      </template>
      <template
        slot="name"
        slot-scope="row"
      >
        {{ row.value }}
        <span
          class="ml-1"
          @click.stop="toggleFavorite(row.item.album_id, row.item.is_favorite)"
        >
          <v-icon
            name="star"
            :color="(!row.item.is_favorite) ? 'grey' : ''"
          />
        </span>
      </template>
      <template
        slot="created_time"
        slot-scope="data"
      >
        {{ data.item.created_time | formatDate }}
      </template>
      <template
        slot="last_event_time"
        slot-scope="data"
      >
        {{ data.item.last_event_time | formatDate }}
      </template>
    </b-table>
    <infinite-loading
      spinner="spiral"
      :identifier="infiniteId"
      @infinite="infiniteHandler"
    >
      <div slot="no-more">
        {{ $t('nomorealbums') }}
      </div>
      <div slot="no-results">
        {{ $t('noresults') }}
      </div>
      <div slot="error">
        {{ $t('error') }}
        <button
          type="button"
          class=" btn btn-md"
          @click="searchAlbums()"
        >
          Reload
        </button>
      </div>
    </infinite-loading>
  </div>
</template>
<script>

import { mapGetters } from 'vuex'
import Datepicker from 'vuejs-datepicker'
import formGetUser from '@/components/user/getUser'
import ListAlbumsHeaders from '@/components/albums/ListAlbumsHeaders'
import InfiniteLoading from 'vue-infinite-loading'
import SortList from '@/components/inbox/SortList.vue'
import moment from 'moment'

export default {
	name: 'Albums',
	components: { InfiniteLoading, ListAlbumsHeaders, formGetUser, Datepicker, SortList },
	data () {
		return {
			form_send_album: false,
			showFilters: false,
			infiniteId: 0,
			albumsParams: {
				offset: 0,
				limit: 16,
				sortDesc: true,
				sortBy: 'last_event_time'
			},
			fields: [
				{
					key: 'is_selected',
					label: '',
					sortable: false,
					class: 'td_checkbox breakword',
					thStyle: {
						'width': '100px'
					}
				},
				{
					key: 'name',
					label: this.$t('name'),
					tdClass: 'name',
					sortable: true,
					class: 'breakword',
					thStyle: {
						'width': '250px'
					}
				},
				{
					key: 'number_of_studies',
					label: this.$t('Study #'),
					sortable: true,
					class: 'd-none d-sm-table-cell breakword',
					thStyle: {
						'width': '200px'
					}
				},
				{
					key: 'number_of_users',
					label: this.$t('User #'),
					sortable: true,
					class: 'd-none d-md-table-cell breakword',
					thStyle: {
						'width': '200px'
					}
				},
				{
					key: 'number_of_comments',
					label: this.$t('Message #'),
					sortable: true,
					class: 'd-none d-lg-table-cell breakword',
					thStyle: {
						'width': '200px'
					}
				},
				{
					key: 'created_time',
					label: this.$t('Date'),
					sortable: true,
					class: 'd-none d-sm-table-cell breakword',
					thStyle: {
						'width': '200px'
					}
				},
				{
					key: 'last_event_time',
					label: this.$t('LastEvent'),
					sortable: true,
					class: 'd-none d-lg-table-cell breakword',
					thStyle: {
						'width': '200px'
					}
				},
				{
					key: 'modalities',
					label: this.$t('Modalities'),
					sortable: false,
					formatter: (value, key, item) => {
						if (value.length > 0) {
							return value.join(', ')
						} else {
							return this.$t('nomodality')
						}
					},
					class: 'breakword',
					thStyle: {
						'width': '200px'
					}
				}
			],
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
	computed: {
		...mapGetters({
			albums: 'albums'
		}),
		albumsSelected () {
			return this.albums.filter(album => { return album.is_selected === true })
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
	watch: {
		filters: {
			handler: function (filters) {
				this.searchAlbums()
			},
			deep: true
		},
		showFilters: {
			handler: function (showFilters) {
				if (!showFilters) {
					this.filters = {
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
			}
		}
	},
	created () {
		this.$store.dispatch('initAlbums', {})
	},
	methods: {
		clickAlbum (item) {
			if (item.album_id) {
				this.$router.push('/albums/' + item.album_id)
			}
		},
		infiniteHandler ($state) {
			this.getAlbums(this.albumsParams.offset, this.albumsParams.limit).then(res => {
				if (res.status === 200 && res.data.length > 0) {
					this.albumsParams.offset += this.albumsParams.limit
					$state.loaded()
				} else {
					$state.complete()
				}
			}).catch(err => {
				$state.error()
			})
		},
		getAlbums (offset = 0, limit = 0) {
			let params = {
				limit: limit,
				offset: offset,
				sort: (this.albumsParams.sortDesc ? '-' : '') + this.albumsParams.sortBy
			}
			const queries = Object.assign(params, this.prepareFilters())
			return this.$store.dispatch('getAlbums', { queries: queries })
		},
		prepareFilters () {
			let filtersToSend = {}
			for (let id in this.filters) {
				if (this.filters[id] !== '') {
					if (id === 'name') {
						filtersToSend[id] = `*${this.filters[id]}*`
					} else if (id === 'CreateDateFrom') {
						if (this.filters['CreateDateTo'] === '') {
							filtersToSend['created_time'] = `${this.transformDate(this.filters[id])}-`
						} else {
							filtersToSend['created_time'] = `${this.transformDate(this.filters[id])}-${this.transformDate(this.filters['CreateDateTo'])}`
						}
					} else if (id === 'CreateDateTo') {
						if (this.filters['CreateDateFrom'] === '') {
							filtersToSend['created_time'] = `-${this.transformDate(this.filters[id])}`
						}
					} else if (id === 'EventDateFrom') {
						if (this.filters['EventDateTo'] === '') {
							filtersToSend['last_event_time'] = `${this.transformDate(this.filters[id])}-`
						} else {
							filtersToSend['last_event_time'] = `${this.transformDate(this.filters[id])}-${this.transformDate(this.filters['EventDateTo'])}`
						}
					} else if (id === 'EventDateTo') {
						if (this.filters['EventDateFrom'] === '') {
							filtersToSend['last_event_time'] = `-${this.transformDate(this.filters[id])}`
						}
					} else {
						filtersToSend[id] = this.filters[id]
					}
				}
			}
			return filtersToSend
		},
		transformDate (date) {
			return moment(date).format('YYYYMMDD')
		},
		sendToUser (userId) {
			this.albumsSelected.forEach(album => {
				this.$store.dispatch('addUser', { album_id: album.album_id, user_id: userId }).then(res => {
					this.$snotify.success(this.$t('albumshared'))
				})
			})
		},
		sortingChanged (ctx) {
			this.albumsParams.sortDesc = ctx.sortDesc
			this.albumsParams.sortBy = ctx.sortBy
			this.searchAlbums()
		},
		searchAlbums () {
			this.albumsParams.offset = 0
			this.$store.dispatch('initAlbums', { })
			this.infiniteId += 1
		},
		toggleFavorite (albumID, isFavorite) {
			let value = !isFavorite
			this.$store.dispatch('manageFavoriteAlbum', { album_id: albumID, value: value }).then(res => {
				this.$store.dispatch('setValueAlbum', { album_id: albumID, flag: 'is_favorite', value: value })
			})
		}
	}
}
</script>

<style scoped>
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

.breakword {
	word-break: break-word;
}
</style>
