<i18n>
{
	"en": {
		"newalbum": "New album",
		"Study #": "Study #",
		"Modalities": "Modalities",
		"User #": "User #",
		"Message #": "Message #",
		"Date": "Date",
		"LastEvent": "Last event",
		"selectednbalbums": "{count} album is selected | {count} albums are selected",
		"permissionsfailed": "You can't send this albums : ",
    "send": "The album {albumName} has been shared.",
    "nomodality": "No modality",
		"name": "Name",
		"nomorealbums": "No more albums",
		"noresults": "No results",
		"albumshared": "Album shared"
	},
	"fr": {
		"newalbum": "Nouvel album",
		"Study #": "# Etudes",
		"Modalities": "Modalités",
		"User #": "# Utilisateurs",
		"Message #": "# Messages",
		"Date": "Date",
		"LastEvent": "Dernier événement",
		"selectednbalbums": "{count} album est sélectionnée | {count} albums sont sélectionnées",
		"permissionsfailed": "Vous ne pouvez pas envoyer ces albums : ",
		"send": "L'album {albumName} a été partagé.",
    "nomodality": "Aucune modalité",
		"name": "Nom",
		"nomorealbums": "Pas d'albums en plus",
		"noresults": "Aucun results",
		"albumshared": "Album partagé"
	}
}
</i18n>
<template>
  <div>
    <list-albums-headers
      :disabled-btn-share="albumsSelected.length === 0"
      @inviteClick="form_send_album=true"
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
    >
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
    </infinite-loading>
  </div>
</template>
<script>

import { mapGetters } from 'vuex'
import Datepicker from 'vuejs-datepicker'
import formGetUser from '@/components/user/getUser'
import ListAlbumsHeaders from '@/components/albumsdatamodel/ListAlbumsHeaders'
import InfiniteLoading from 'vue-infinite-loading'

export default {
	name: 'Albums',
	components: { InfiniteLoading, ListAlbumsHeaders, formGetUser },
	data () {
		return {
			form_send_album: false,
			infiniteId: 0,
			albumsParams: {
				offset: 0,
				limit: 16,
				sortDesc: false,
				sortBy: 'last_event_time'
			},
			fields: [
				{
					key: 'is_selected',
					label: '',
					sortable: false,
					class: 'td_checkbox'
				},
				{
					key: 'name',
					label: this.$t('name'),
					tdClass: 'name',
					sortable: true
				},
				{
					key: 'number_of_studies',
					label: this.$t('Study #'),
					sortable: true,
					class: 'd-none d-sm-table-cell'
				},
				{
					key: 'modalities',
					label: this.$t('Modalities'),
					sortable: false,
					formatter: (value, key, item) => {
						if (value.length > 0) {
							return value.join()
						} else {
							return this.$t('nomodality')
						}
					}
				},
				{
					key: 'number_of_users',
					label: this.$t('User #'),
					sortable: true,
					class: 'd-none d-md-table-cell'
				},
				{
					key: 'number_of_comments',
					label: this.$t('Message #'),
					sortable: true,
					class: 'd-none d-lg-table-cell'
				},
				{
					key: 'created_time',
					label: this.$t('Date'),
					sortable: true,
					class: 'd-none d-sm-table-cell'
				},
				{
					key: 'last_event_time',
					label: this.$t('LastEvent'),
					sortable: true,
					class: 'd-none d-lg-table-cell'
				}
			]
		}
	},
	computed: {
		...mapGetters({
			albums: 'albumsTest'
		}),
		albumsSelected () {
			return this.albums.filter(album => { return album.is_selected === true })
		}
	},
	created () {
		this.$store.dispatch('initAlbumsTest', {})
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
			})
		},
		getAlbums (offset = 0, limit = 0) {
			let params = {
				limit: limit,
				offset: offset,
				sort: (this.albumsParams.sortDesc ? '-' : '') + this.albumsParams.sortBy
			}
			const queries = Object.assign(params, this.prepareFilters())
			return this.$store.dispatch('getAlbumsTest', { queries: queries })
		},
		prepareFilters () {
			return {}
		},
		sendToUser (user_id) {
			this.albumsSelected.forEach(album => {
				this.$store.dispatch('addUser', { album_id: album.album_id, user_id: user_id }).then(res => {
					this.$snotify.success(this.$t('albumshared'))
				})
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
</style>
