<!--
  TODO: Remove settimeout when load studies.
-->
<i18n>
{
	"en": {
		"selectednbstudies": "{count} study is selected | {count} study is selected | {count} studies are selected",
		"addalbum": "Add to an album",
		"download": "Download",
		"addfavorite": "Add to favorites",
		"removefavorite": "Remove to favorites",
		"PatientName": "Patient Name",
		"Modality": "Modality",
		"StudyDate": "Study Date",
		"StudyDescription": "Study description",
		"PatientID": "Patient ID",
		"filter": "Filter",
		"fromDate": "From",
		"toDate": "To",
		"studyputtoalbum": "Studies successfully added to the album",
		"includeseriesfromalbum": "Include series in albums",
		"send": "Send",
		"delete": "Delete",
		"comments": "comments",
		"series": "series",
		"study": "study",
		"nostudy": "No studies found",
		"studiessend": "studies send to inbox",
    "confirmDelete": "Are you sure you want to delete {count} study | Are you sure you want to delete {count} studies",
    "confirmDeleteSeries": "containing {count} serie? Once deleted, you will not be able to re-upload any series if other users still have access to them. | containing {count} series? Once deleted, you will not be able to re-upload any series if other users still have access to them.",
    "cancel": "Cancel",
    "importdir": "Import directory",
    "importfiles": "Import files",
    "draganddrop": "Or drag and drop",
    "favorites": "Favorites",
		"nomorestudies": "No more studies",
		"noresults": "No results",
		"error": "An error occur please reload the studies."
	},
	"fr": {
		"selectednbstudies": "{count} étude est sélectionnée | {count} étude est sélectionnée | {count} études sont sélectionnées",
		"addalbum": "Ajouter à un album",
		"download": "Télécharger",
		"addfavorite": "Ajouter aux favoris",
		"removefavorite": "Supprimer des favoris",
		"PatientName": "Nom du patient",
		"Modality": "Modalité",
		"StudyDate": "Date de l'étude",
		"StudyDescription": "Description de l'étude",
		"PatientID": "Patient ID",
		"filter": "Filtrer",
		"fromDate": "De",
		"toDate": "A",
		"studyputtoalbum": "L'étude a été enregistrée dans l'album avec succès",
		"includeseriesfromalbum": "inclure des séries présentes dans les albums",
		"send": "Envoyer",
		"delete": "Supprimer",
		"comments": "commentaire",
		"series": "séries",
		"study": "étude",
    "nostudy": "Aucne étude trouvée",
		"studiessend": "études envoyées dans votre boîte de réception",
    "confirmDelete": "Etes vous de sûr de vouloir supprimer ? ",
    "cancel": "Annuler",
    "importdir": "Importer un dossier",
    "importfiles": "Importer des fichiers",
    "draganddrop": "Ou Drag and Drop",
    "favorites": "Favorites",
		"nomorestudies": "Plus d'études",
		"noresults": "Aucun resultats",
		"error": "Une erreur s'est produite, veuillez recharger les études."
	}
}
</i18n>

<template>
  <div>
    <input
      id="file"
      ref="inputfiles"
      type="file"
      name="file"
      class="inputfile"
      multiple
      :disabled="sendingFiles"
      @change="inputLoadFiles"
    >
    <input
      id="directory"
      ref="inputdir"
      type="file"
      name="file"
      class="inputfile"
      webkitdirectory
      :disabled="sendingFiles"
      @change="inputLoadDirectories"
    >
    <list-headers
      :studies="studies"
      :albums="albums"
      :show-send-button="permissions.send_series"
      :show-album-button="permissions.send_series"
      :show-favorite-button="permissions.add_series"
      :show-delete-button="permissions.delete_series"
      :show-import-button="permissions.add_series"
      :show-inbox-button="permissions.add_inbox"
      :album-id="album.album_id !== undefined ? album.album_id : ''"
      @setFilters="changeFilterValue"
      @reloadStudies="reloadStudies"
    />
    <b-table
      class="container-fluid"
      striped
      hover
      :items="studies"
      :fields="fields"
      :sort-desc="true"
      :no-local-sorting="true"
      :no-sort-reset="true"
      :tbody-class="'table-wrapper-scroll-y link'"
      @sort-changed="sortingChanged"
      @row-hovered="setItemHover"
      @row-unhovered="setItemUnhover"
      @row-clicked="showRowDetails"
    >
      <div
        slot="table-busy"
        class="text-center my-2"
      >
        <strong>Loading...</strong>
      </div>

      <!--
				HEADER TABLE
			-->
      <template
        slot="HEAD_PatientName"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.PatientName"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('filter')"
          > <br>
        </div>
        <sort-list
          :sort-desc="studiesParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="studiesParams.sortBy"
        />
        {{ data.label }}
      </template>

      <template
        slot="HEAD_PatientID"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.PatientID"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('filter')"
          > <br>
        </div>
        <sort-list
          :sort-desc="studiesParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="studiesParams.sortBy"
        />
        {{ data.label }}
      </template>

      <template
        slot="HEAD_StudyDescription"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.StudyDescription"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('filter')"
          > <br>
        </div>
        {{ data.label }}
      </template>

      <template
        slot="HEAD_StudyDate"
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
                v-model="filters.StudyDateFrom"
                :disabled-dates="disabledFromDates"
                input-class="form-control form-control-sm  search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('fromDate')"
              />
            </div>
          </div>

          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.StudyDateTo"
                :disabled-dates="disabledToDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('toDate')"
              />
            </div>
          </div>
        </div>
        <br v-if="showFilters">
        <sort-list
          :sort-desc="studiesParams.sortDesc"
          :current-header="data.field.key"
          :sort-by="studiesParams.sortBy"
        />
        {{ data.label }}
      </template>

      <template
        slot="HEAD_ModalitiesInStudy"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <select
            v-model="filters.ModalitiesInStudy"
            class="form-control"
          >
            <option value="" />
            <option
              v-for="modality in modalities"
              :key="modality.id"
              :value="modality"
            >
              {{ modality }}
            </option>
          </select>
          <br>
          <br>
        </div>
        {{ $t(data.label) }}
      </template>
      <!--
				CONTENT TABLE
			-->
      <template
        slot="is_selected"
        slot-scope="row"
      >
        <b-button-group>
          <b-button
            variant="link"
            size="sm"
            class="mr-1 pt-0"
            @click.stop="showSeries(row)"
          >
            <v-icon
              class="align-middle"
              :name="row.detailsShowing ? 'chevron-down' : 'chevron-right'"
              @click.stop="row.toggleDetails"
            />
          </b-button>
          <b-form-checkbox
            v-model="row.item.flag.is_selected"
            :indeterminate="row.item.flag.is_indeterminate"
            class="mr-0"
            inline
            @change="setChecked(row)"
          />
        </b-button-group>
      </template>
      <template
        slot="PatientName"
        slot-scope="row"
      >
        <div
          :class="mobiledetect===true ? '' : 'd-flex flex-wrap'"
        >
          <div class="">
            {{ row.value["Alphabetic"] }} {{ row.value["Ideographic"] }}
          </div>
          <div :class="mobiledetect===true ? '' : 'ml-auto'">
            <!--
            :show-favorite-icon="permissions.add_series"
          -->
            <list-icons
              :study="row.item"
              :mobiledetect="mobiledetect"
              :show-favorite-icon="permissions.add_series"
              :show-comment-icon="true"
              :show-download-icon="permissions.download_series"
              :show-import-icon="permissions.add_series"
              :show-report-provider-icon="album.album_id !== undefined ? true : false"
              :album-id="album.album_id !== undefined ? album.album_id : ''"
            >
              <template
                slot="reportprovider"
              >
                <icon-list-providers
                  :study="row.item"
                  :providers="providersEnable"
                />
              </template>
            </list-icons>
          </div>
        </div>
      </template>
      <template
        slot="StudyDate"
        slot-scope="row"
      >
        {{ row.value | formatDate }}
      </template>
      <!--Infos study (Series / Comments / Study Metadata) -->
      <template
        slot="row-details"
        slot-scope="row"
      >
        <b-card>
          <list-item-details
            :study-u-i-d="row.item.StudyInstanceUID.Value[0]"
            :album-id="albumID"
          />
        </b-card>
      </template>
    </b-table>
    <infinite-loading
      :identifier="infiniteId"
      @infinite="infiniteHandler"
    >
      <div slot="spinner">
        <pulse-loader
          color="white"
        />
      </div>
      <div slot="no-more">
        {{ $t('nomorestudies') }}
      </div>
      <div slot="no-results">
        {{ $t('noresults') }}
      </div>
      <div slot="error">
        {{ $t('error') }} <br> <br>
        <button
          type="button"
          class=" btn btn-md"
          @click="searchStudies()"
        >
          Reload
        </button>
      </div>
    </infinite-loading>
  </div>
</template>

<script>
// https://peachscript.github.io/vue-infinite-loading/guide/start-with-hn.html
import { mapGetters } from 'vuex'
import ListHeaders from '@/components/inbox/ListHeaders'
import ListIcons from '@/components/inbox/ListIcons'
import ListItemDetails from '@/components/inbox/ListItemDetails.vue'
import InfiniteLoading from 'vue-infinite-loading'
import Datepicker from 'vuejs-datepicker'
import moment from 'moment'
import mobiledetect from '@/mixins/mobiledetect.js'
import SortList from '@/components/inbox/SortList.vue'
import IconListProviders from '@/components/providers/IconListProviders.vue'
import PulseLoader from 'vue-spinner/src/PulseLoader.vue'

export default {
	name: 'Studies',
	components: { ListHeaders, ListIcons, ListItemDetails, InfiniteLoading, Datepicker, SortList, IconListProviders, PulseLoader },
	mixins: [ ],
	props: {
		album: {
			type: Object,
			required: false,
			default: () => ({})
		}
	},
	data () {
		return {
			infiniteId: 0,
			showFilters: false,
			studiesParams: {
				offset: 0,
				limit: 16,
				sortDesc: true,
				sortBy: 'StudyDate'
			},
			fields: {
				isSelected: {
					key: 'is_selected',
					label: '',
					sortable: false,
					class: ['td_checkbox', 'breakword'],
					thStyle: {
						'width': '100px'
					}
				},
				PatientName: {
					label: this.$t('PatientName'),
					sortable: true,
					tdClass: 'breakwork',
					formatter: (value, key, item) => {
						if (value !== null && value.Value !== undefined) {
							return value.Value[0]
						} else {
							return ''
						}
					},
					thStyle: {
						'width': '250px'
					}
				},
				PatientID: {
					label: this.$t('PatientID'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'breakword d-none d-md-table-cell d-lg-table-cell',
					formatter: (value, key, item) => {
						return value.Value[0]
					},
					thStyle: {
						'width': '250px'
					}
				},
				StudyDescription: {
					label: this.$t('StudyDescription'),
					sortable: false,
					tdClass: 'breakwork',
					class: 'breakword d-none d-lg-table-cell',
					formatter: (value, key, item) => {
						if (value !== null && value.Value !== undefined) {
							return value.Value[0]
						} else {
							return ''
						}
					},
					thStyle: {
						'width': '400px'
					}
				},
				StudyDate: {
					label: this.$t('StudyDate'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'breakword d-none d-sm-table-cell d-md-table-cell d-lg-table-cell',
					formatter: (value, key, item) => {
						if (value !== null && value.Value !== undefined) {
							return value.Value[0]
						} else {
							return ''
						}
					},
					thStyle: {
						'width': '150px'
					}
				},
				ModalitiesInStudy: {
					label: this.$t('Modality'),
					sortable: false,
					tdClass: 'breakwork',
					class: 'breakword d-none d-sm-table-cell',
					formatter: (value, key, item) => {
						return value.Value.join(', ')
					},
					thStyle: {
						'width': '150px'
					}
				}
			},
			filters: {
				PatientName: '',
				PatientID: '',
				StudyDescription: '',
				StudyDateFrom: '',
				StudyDateTo: '',
				ModalitiesInStudy: ''
			}
		}
	},
	computed: {
		...mapGetters({
			studies: 'studies',
			series: 'series',
			albums: 'albums',
			sendingFiles: 'sending',
			providers: 'providers',
			modalities: 'modalities'
		}),
		OS () {
			return navigator.platform
		},
		disabledToDates: function () {
			return {
				to: this.filters.StudyDateFrom,
				from: new Date()
			}
		},
		disabledFromDates: function () {
			return {
				from: new Date()
			}
		},
		mobiledetect () {
			return mobiledetect.mobileAndTabletcheck()
		},
		albumID () {
			if (this.album.album_id !== undefined || this.album.album_id !== '') {
				return this.album.album_id
			} else {
				return undefined
			}
		},
		permissions () {
			return {
				add_series: this.album.album_id !== undefined ? this.album.add_series || this.album.is_admin : true,
				delete_series: this.album.album_id !== undefined ? this.album.delete_series || this.album.is_admin : true,
				download_series: this.album.album_id !== undefined ? this.album.download_series || this.album.is_admin : true,
				send_series: this.album.album_id !== undefined ? this.album.send_series || this.album.is_admin : true,
				write_comments: this.album.album_id !== undefined ? this.album.write_comments || this.album.is_admin : true,
				add_inbox: this.album.album_id !== undefined ? this.album.add_series || this.album.is_admin : false
			}
		},
		providersEnable () {
			return this.providers.filter(function (provider) {
				return provider.stateURL['checkURL'] === true
			})
		}
	},
	watch: {
		sendingFiles () {
			if (!this.sendingFiles) {
				/*
				this.$store.dispatch('initStudies', { })
				this.getStudies(0, this.studiesParams.offset > 0 ? this.studiesParams.offset : this.studiesParams.limit)
				*/
			}
		},
		filters: {
			handler: function (filters) {
				this.searchStudies()
			},
			deep: true
		},
		showFilters: {
			handler: function (showFilters) {
				if (!showFilters) {
					this.filters = {
						PatientName: '',
						PatientID: '',
						StudyDescription: '',
						StudyDateFrom: '',
						StudyDateTo: '',
						ModalitiesInStudy: ''
					}
				}
			}
		}
	},
	created () {
		this.initData()
		let queriesAlbums = {
			canAddSeries: true
		}
		this.$store.dispatch('getAlbums', { 'queries': queriesAlbums })
		this.setAlbumInbox()
	},
	destroyed () {
		this.$store.dispatch('initStudies', {})
		this.$store.dispatch('initSeries')
		this.$store.dispatch('initAlbums', {})
	},
	mounted () {
	},
	methods: {
		// https://peachscript.github.io/vue-infinite-loading/old/#!/getting-started/trigger-manually
		infiniteHandler ($state) {
			this.getStudies(this.studiesParams.offset, this.studiesParams.limit).then(res => {
				if (this.studies.length === parseInt(res.headers['x-total-count'])) {
					$state.complete()
				}
				if (res.status === 200 && res.data.length > 0) {
					this.studiesParams.offset += this.studiesParams.limit
					$state.loaded()
				} else if (res.status === 204 && res.data.length === 0) {
					$state.complete()
				}
			}).catch(err => {
				$state.error()
				return err
			})
		},
		initData () {
			this.$store.dispatch('initStudies', {})
			this.$store.dispatch('initSeries')
			this.$store.dispatch('initModalities')
			this.$store.dispatch('initAlbums', {})
		},
		reloadStudies () {
			this.searchStudies()
			if (this.albumID !== undefined) {
				this.getAlbum().then(res => {
					this.setAlbumInbox()
				})
			} else {
				this.setAlbumInbox()
			}
		},
		getAlbum () {
			return this.$store.dispatch('getAlbum', { album_id: this.album.album_id }).catch(err => {
				this.$router.push('/albums')
				return err
			})
		},
		setAlbumInbox () {
			if (this.albumID !== undefined) {
				this.$store.dispatch('getProviders', { albumID: this.albumID })
				this.$store.commit('SET_MODALITIES', this.album.modalities)
			} else {
				this.$store.dispatch('getInboxInfo')
			}
		},
		setItemHover (item, index, event) {
			let params = {
				StudyInstanceUID: item.StudyInstanceUID.Value[0],
				flag: 'is_hover',
				value: true
			}
			this.$store.dispatch('setFlagByStudyUID', params)
		},
		setItemUnhover (item, index, event) {
			let params = {
				StudyInstanceUID: item.StudyInstanceUID.Value[0],
				flag: 'is_hover',
				value: false
			}
			this.$store.dispatch('setFlagByStudyUID', params)
		},
		showSeries (row) {
			if (!row.item.detailsShowing) {
				this.toggleDetails(row)
			}
		},
		setViewDetails (StudyInstanceUID, flagView) {
			let viewSelected = flagView === '' ? 'series' : flagView
			let params = {
				StudyInstanceUID: StudyInstanceUID,
				flag: 'view',
				value: viewSelected
			}
			this.$store.dispatch('setFlagByStudyUID', params)
		},
		toggleDetails (row) {
			this.setViewDetails(row.item.StudyInstanceUID.Value[0], row.item.flag.view)
			row.toggleDetails()
		},
		createObjectFlag (StudyInstanceUID, studyIndex, flag, value) {
			return {
				StudyInstanceUID: StudyInstanceUID,
				studyIndex: studyIndex,
				flag: flag,
				value: value
			}
		},
		setChecked (row) {
			let value = row.item.flag.is_selected
			let StudyInstanceUID = row.item.StudyInstanceUID.Value[0]
			let studyIndex = this.studies.findIndex(study => {
				return study.StudyInstanceUID.Value[0] === StudyInstanceUID
			})

			let paramsSelected = this.createObjectFlag(StudyInstanceUID, studyIndex, 'is_selected', !value)
			this.$store.dispatch('setFlagByStudyUID', paramsSelected)
			let paramsIndeterminate = this.createObjectFlag(StudyInstanceUID, studyIndex, 'is_indeterminate', false)
			this.$store.dispatch('setFlagByStudyUID', paramsIndeterminate)
			if (this.series[StudyInstanceUID] !== undefined) {
				this.setSeriesCheck(this.series[StudyInstanceUID], paramsSelected)
			}
		},
		setSeriesCheck (series, params) {
			for (let serieUID in series) {
				params.SeriesInstanceUID = serieUID
				this.$store.dispatch('setFlagByStudyUIDSerieUID', params)
			}
		},
		sortingChanged (ctx) {
			this.studiesParams.sortDesc = ctx.sortDesc
			this.studiesParams.sortBy = ctx.sortBy
			this.searchStudies()
		},
		searchStudies () {
			this.studiesParams.offset = 0
			this.$store.dispatch('initStudies', { })
			this.$store.dispatch('initSeries')
			this.infiniteId += 1
		},
		getStudies (offset = 0, limit = 0) {
			let params = {
				limit: limit,
				offset: offset,
				includefield: ['favorite', 'comments', '00081030'],
				sort: (this.studiesParams.sortDesc ? '-' : '') + this.studiesParams.sortBy
			}
			if (this.albumID === undefined) {
				params.inbox = true
			} else {
				params.album = this.albumID
			}
			const queries = Object.assign(params, this.prepareFilters())
			return this.$store.dispatch('getStudies', { queries: queries })
		},
		prepareFilters () {
			let filtersToSend = {}
			for (let id in this.filters) {
				if (this.filters[id] !== '') {
					if (id === 'PatientName' || id === 'StudyDescription' || id === 'PatientID') {
						filtersToSend[id] = `*${this.filters[id]}*`
					} else if (id === 'StudyDateFrom') {
						if (this.filters['StudyDateTo'] === '') {
							filtersToSend['StudyDate'] = `${this.transformDate(this.filters[id])}-`
						} else {
							filtersToSend['StudyDate'] = `${this.transformDate(this.filters[id])}-${this.transformDate(this.filters['StudyDateTo'])}`
						}
					} else if (id === 'StudyDateTo') {
						if (this.filters['StudyDateFrom'] === '') {
							filtersToSend['StudyDate'] = `-${this.transformDate(this.filters[id])}`
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
		showRowDetails (item, index, event) {
			if (!item._showDetails) {
				this.setViewDetails(item.StudyInstanceUID.Value[0], item.flag.view)
				item._showDetails = true
			} else {
				item._showDetails = false
			}
		},
		inputLoadFiles () {
			if (this.$refs.inputfiles.files.length > 0) {
				const filesFromInput = this.$refs.inputfiles.files
				this.$emit('loadfiles', filesFromInput)
			}
		},
		inputLoadDirectories () {
			if (this.$refs.inputdir.files.length > 0) {
				const filesFromInput = this.$refs.inputdir.files
				this.$emit('loaddirectories', filesFromInput)
			}
		},
		changeFilterValue (value) {
			this.showFilters = value
		}
	}
}

</script>

<style scoped>
	select{
		display: inline !important;
	}
	.iconsHover{
		visibility: visible;
		display: inline;
		cursor: pointer;
	}
	.iconsUnhover{
		visibility: hidden;
		display: inline;
		cursor: pointer;
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

	.patientNameContainer{
		position: relative;
		white-space: nowrap;
	}

	.patientNameIcons{
		visibility:hidden;
		display: inline;
		cursor: pointer;
	}
	@media (max-width:1024px) {
		.patientNameIcons {
			visibility: visible;
			display: inline-block;
		}
	}
	.patientName:hover .patientNameIcons {
		visibility:visible;
	}

	.patientNameIcons > span.selected{
		visibility:visible !important;
	}

	.patientNameIcons span{
		margin: 0 3px;
	}

	.selection-button-container{
		height: 60px;
	}

	.td_checkbox {
		width: auto;
	}

	input.search-calendar{
		width: 100px !important;
	}

	div.calendar-wrapper{
		color: #333;
	}

	a{
		cursor: pointer;
	}

	a.download{
		color: #FFF;
	}

	a.download:hover{
		color: #fd7e14;
	}
  .inputfile {
    width: 0.1px;
    height: 0.1px;
    opacity: 0;
    overflow: hidden;
    position: absolute;
    z-index: -1;
  }
  .sticky {
    position: fixed;
    top: 70px;
    width: 100%;
    background: #555;
    z-index: 5;
    opacity: 0.95;
  }
  .sticky + .content {
    padding-top: 70px;
  }
  .table-wrapper-scroll-y {
    width: 5px;
    height: 5px;
    overflow: scroll;
    display: block;
  }
  .breakword {
		word-break: break-word;
	}
</style>
