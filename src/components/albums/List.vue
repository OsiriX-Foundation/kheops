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
		"LastEvent": "last event",
		"selectednbalbums": "{count} album is selected | {count} albums are selected",
		"share": "Invite user",
		"permissionsfailed": "You can't send this albums : ",
    "send": "The album {albumName} has been shared.",
    "nomodality": "No modality"
	},
	"fr": {
		"newalbum": "Nouvel album",
		"Study #": "# Etudes",
		"Modalities": "modalités",
		"User #": "# utilisateurs",
		"Message #": "# messages",
		"Date": "date",
		"LastEvent": "dern. evnt",
		"selectednbalbums": "{count} album est sélectionnée | {count} albums sont sélectionnées",
		"share": "Inviter un utilisateur",
		"permissionsfailed": "Vous ne pouvez pas envoyer ces albums : ",
		"send": "L'album {albumName} a été partagé.",
    "nomodality": "Aucune modalité"
	}
}
</i18n>

<template>
  <div class="container-fluid">
    <div
      class="d-flex"
    >
      <div
        class="p-2"
      >
        <h3>
          <router-link
            to="albums/new"
            active-class="active"
            style="display: inline; color: white"
          >
            <v-icon
              name="plus"
              class="mr-2"
            />{{ $t('newalbum') }}
          </router-link>
        </h3>
      </div>
      <div
        class="p-2"
      >
        <button
          type="button"
          class="btn btn-link btn-sm text-center"
          :disabled="selectedAlbumsNb===0"
          @click.stop="form_send_album=!form_send_album"
        >
          <span>
            <v-icon
              name="user-plus"
              scale="1.5"
            />
          </span><br>
          {{ $t("share") }}
        </button>
      </div>
      <div
        class="ml-auto"
      >
        <button
          type="button"
          class="btn btn-link btn-lg"
          @click="showFilters=!showFilters"
        >
          <v-icon
            name="search"
            scale="2"
          />
        </button>
      </div>
    </div>
    <form-get-user
      v-if="form_send_album && selectedAlbumsNb"
      @get-user="sendToUser"
      @cancel-user="form_send_album=false"
    />
    <b-table
      striped
      :items="albums"
      :fields="fields"
      :sort-desc="true"
      :sort-by.sync="sortBy"
      :no-local-sorting="true"
      :dark="false"
      :no-sort-reset="true"
      @sort-changed="sortingChanged"
      @row-clicked="selectAlbum"
    >
      <template
        slot="HEAD_is_selected"
      >
        <!--
        <b-button
          variant="link"
          size="sm"
          class="mr-2"
        >
          <v-icon
            class="align-middle"
            name="chevron-down"
            style="visibility: hidden"
          />
        </b-button>
        <b-form-checkbox
          v-model="albums.allSelected"
          name="allSelected"
          @click.native.stop
          @change="selectAll(albums.allSelected)"
        />
				-->
      </template>
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
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_number_of_studies"
        slot-scope="data"
      >
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_modalities"
        slot-scope="data"
      >
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_number_of_users"
        slot-scope="data"
      >
        {{ $t(data.label) }}
      </template>
      <template
        slot="HEAD_number_of_comments"
        slot-scope="data"
      >
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
                :bootstrap-styling="false"
                :disabled-dates="disabledFromCreateDates"
                input-class="form-control form-control-sm  search-calendar"
                :calendar-button="false"
                calendar-button-icon=""
                wrapper-class="calendar-wrapper"
                :placeholder="$t('fromDate')"
                :clear-button="true"
                clear-button-icon="fa fa-times"
              />
            </div>
          </div>

          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.CreateDateTo"
                :bootstrap-styling="false"
                :disabled-dates="disabledToCreateDates"
                input-class="form-control form-control-sm search-calendar"
                :calendar-button="false"
                calendar-button-icon=""
                wrapper-class="calendar-wrapper"
                :placeholder="$t('toDate')"
                :clear-button="true"
                clear-button-icon="fa fa-times"
              />
            </div>
          </div>
          <!-- <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateFrom' placeholder="From"> - <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateTo' placeholder="To"> <br> -->
        </div>
        <br v-if="showFilters">
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
                :bootstrap-styling="false"
                :disabled-dates="disabledFromEventDates"
                input-class="form-control form-control-sm  search-calendar"
                :calendar-button="false"
                calendar-button-icon=""
                wrapper-class="calendar-wrapper"
                :placeholder="$t('fromDate')"
                :clear-button="true"
                clear-button-icon="fa fa-times"
              />
            </div>
          </div>

          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.EventDateTo"
                :bootstrap-styling="false"
                :disabled-dates="disabledToEventDates"
                input-class="form-control form-control-sm search-calendar"
                :calendar-button="false"
                calendar-button-icon=""
                wrapper-class="calendar-wrapper"
                :placeholder="$t('toDate')"
                :clear-button="true"
                clear-button-icon="fa fa-times"
              />
            </div>
          </div>
          <!-- <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateFrom' placeholder="From"> - <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateTo' placeholder="To"> <br> -->
        </div>
        <br v-if="showFilters">
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
            @change="toggleSelected(row.item,'album',!row.item.is_selected)"
          />
        </b-button-group>
      </template>
      <template
        slot="name"
        slot-scope="data"
      >
        <div class="nameContainer">
          {{ data.item.name }}
          <div class="nameIcons">
            <span
              :class="data.item.is_favorite?'selected':''"
              @click.stop="toggleFavorite(data.index)"
            >
              <v-icon
                v-if="data.item.is_favorite"
                class="align-middle"
                style="margin-right:0"
                name="star"
              />
              <v-icon
                v-else
                class="align-middle"
                style="margin-right:0"
                name="star"
                color="grey"
                :invert="true"
              />
            </span>
          </div>
        </div>
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
      <template
        slot="modalities"
        slot-scope="data"
      >
        {{ data.item.modalities.length > 0 ? ( data.item.modalities[0].split(',').join(' / ') ) : $t('nomodality') }}
      </template>

      <template
        slot="row-details"
        slot-scope="row"
      >
        <b-card>
          <dl><dt>Description</dt><dd>{{ row.item.description }}</dd></dl>
        </b-card>
      </template>
    </b-table>
  </div>
</template>
<script>

import { mapGetters } from 'vuex'
import Datepicker from 'vuejs-datepicker'
import formGetUser from '@/components/user/getUser'

export default {
	name: 'Albums',
	components: { Datepicker, formGetUser },
	data () {
		return {
			pageNb: 1,
			active: false,
			form_send_album: false,
			fields: [
				{
					key: 'is_selected',
					label: '',
					sortable: false,
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
					sortable: true,
					thClass: 'd-none d-sm-table-cell',
					tdClass: 'd-none d-sm-table-cell'
				},
				{
					key: 'modalities',
					label: 'Modalities',
					sortable: false
				},
				{
					key: 'number_of_users',
					label: 'User #',
					sortable: true,
					thClass: 'd-none d-md-table-cell',
					tdClass: 'd-none d-md-table-cell'
				},
				{
					key: 'number_of_comments',
					label: 'Messages #',
					sortable: true,
					thClass: 'd-none d-lg-table-cell',
					tdClass: 'd-none d-lg-table-cell'
				},
				{
					key: 'created_time',
					label: 'Date',
					sortable: true,
					thClass: 'd-none d-sm-table-cell',
					tdClass: 'd-none d-sm-table-cell'
				},
				{
					key: 'last_event_time',
					label: 'LastEvent',
					sortable: true,
					thClass: 'd-none d-lg-table-cell',
					tdClass: 'd-none d-lg-table-cell'
				}
			],
			sortBy: 'created_time',
			sortDesc: true,
			limit: 100,
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
		},
		selectedAlbumsNb: {
			handler: function (selectedAlbumsNb) {
				if (selectedAlbumsNb === 0) {
					this.form_send_album = false
				}
			}
		}
	},
	created () {
		this.$store.dispatch('getAlbums', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit, reset: true })
	},
	mounted () {
		this.scroll()
	},
	methods: {
		scroll () {
			window.onscroll = () => {
				let bottomOfWindow = Math.floor((document.documentElement.scrollTop || document.body.scrollTop)) + Math.floor(window.innerHeight) === document.documentElement.offsetHeight
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
		toggleDetails (row) {
			this.$store.commit('TOGGLE_ALBUM_DETAILS', { albumId: row.item.album_id })
			row.toggleDetails()
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
		},
		sendToUser (userSub) {
			let albumsSelected = this.albums.filter(album => { return album.is_selected === true })
			let permissions = albumsSelected.every(album => { return album.is_admin || album.add_user })
			if (!permissions) {
				let noperms = albumsSelected.filter(album => { return !(album.is_admin || album.add_user) })
				this.$snotify.error(`${this.$t('permissionsfailed')} ${noperms.map(a => a.name).join(', ')}`)
			} else {
				albumsSelected.forEach(album => {
					this.$store.dispatch('add_user_to_album', { album_id: album.album_id, user_name: userSub })
						.then(() => {
							this.form_send_album = false
							this.albums.forEach(album => {
								album.is_selected = false
							})
							this.$snotify.success(`${this.$t('send', { albumName: album.name })}`)
						})
						.catch(res => {
							this.$snotify.error(this.$t(res))
						})
				})
			}
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
@media (max-width:1024px) {
  .nameIcons {
    visibility: visible;
    display: inline-block;
  }
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
