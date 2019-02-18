<i18n>
{
	"en": {
		"selectednbstudies": "{count} study is selected | {count} studies are selected",
		"addalbum": "Add to an album",
		"download": "Download",
		"addfavorite": "Add to favorite",
		"PatientName": "Patient Name",
		"Modality": "Modality",
		"StudyDate": "Study Date",
		"AccessionNumber": "Accession #",
		"PatientID": "Patient ID",
		"filter": "Filter",
		"fromDate": "From",
		"toDate": "To",
		"studyputtoalbum": "Studies put successfully to an album",
		"includeseriesfromalbum": "include series from albums",
		"MRN": "MRN",
		"send": "Send",
		"delete": "Delete",
		"comments": "comments",
		"series": "series",
		"study": "study",
		"studiessharedsuccess": "studies shared successfully",
		"studiessharederror": "studies could not be shared"
	},
	"fr": {
		"selectednbstudies": "{count} étude est sélectionnée | {count} études sont sélectionnées",
		"addalbum": "Ajouter à un album",
		"download": "Télécharger",
		"addfavorite": "Ajouter aux favoris",
		"PatientName": "Nom du patient",
		"Modality": "Modalité",
		"StudyDate": "Date de l'étude",
		"AccessionNumber": "# accession",
		"PatientID": "ID patient",
		"filter": "Filtrer",
		"fromDate": "De",
		"toDate": "A",
		"studyputtoalbum": "L'étude a été enregistrée dans l'album avec succès",
		"includeseriesfromalbum": "inclure des séries présentes dans les albums",
		"MRN": "MRN",
		"send": "Envoyer",
		"delete": "Supprimer",
		"comments": "commentaire",
		"series": "séries",
		"study": "étude",
		"studiessharedsuccess": "études ont été partagées avec succès",
		"studiessharederror": "études n'ont pas pu être partagée"
	}
}
</i18n>

<template>
  <div class="container-fluid">
    <!--button Study selected -->
    <div class="my-3 selection-button-container">
      <span :style="(selectedStudiesNb)?'':'visibility: hidden'">
        <span>{{ $tc("selectednbstudies",selectedStudiesNb,{count: selectedStudiesNb}) }}</span>
        <button
          v-if="!filters.album_id"
          type="button"
          class="btn btn-link btn-sm text-center"
          @click.stop="form_send_study=!form_send_study"
        >
          <span>
            <v-icon
              class="align-middle"
              name="paper-plane"
            />
          </span><br>
          {{ $t("send") }}
        </button>
        <!-- <button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="book"></v-icon></span><br/>{{ $t("addalbum") }}</button> -->
        <b-dropdown
          variant="link"
          size="sm"
          no-caret
        >
          <template slot="button-content">
            <span>
              <v-icon
                class="align-middle"
                name="book"
              />
            </span><br>{{ $t("addalbum") }}
          </template>
          <b-dropdown-item
            v-for="album in allowedAlbums"
            :key="album.id"
            @click.stop="addToAlbum(album.album_id)"
          >
            {{ album.name }}
          </b-dropdown-item>
        </b-dropdown>

        <button
          type="button"
          class="btn btn-link btn-sm text-center"
          @click="downloadSelectedStudies()"
        >
          <span>
            <v-icon
              class="align-middle"
              name="download"
            />
          </span><br>
          {{ $t("download") }}
        </button>
        <button
          v-if="!filters.album_id"
          type="button"
          class="btn btn-link btn-sm text-center"
        >
          <span>
            <v-icon
              class="align-middle"
              name="star"
            />
          </span><br>
          {{ $t("addfavorite") }}
        </button>
        <button
          type="button"
          class="btn btn-link btn-sm text-center"
          @click="deleteSelectedStudies()"
        >
          <span>
            <v-icon
              class="align-middle"
              name="trash"
            />
          </span><br>
          {{ $t("delete") }}
        </button>
      </span>

      <span
        v-if="!filters.album_id"
        style="margin-left: 30px;"
      >
        <toggle-button
          v-model="filters.inbox_and_albums"
          :labels="{checked: 'Yes', unchecked: 'No'}"
        />
        <label class="ml-3">
          {{ $t('includeseriesfromalbum') }}
        </label>
      </span>

      <button
        type="button"
        class="btn btn-link btn-lg float-right"
        @click="showFilters=!showFilters"
      >
        <v-icon
          name="search"
          scale="2"
        />
      </button>
    </div>

    <form-get-user
      v-if="form_send_study"
      @get-user="sendToUser"
      @cancel-user="form_send_study=false"
    />


    <b-table
      class="container-fluid"
      responsive
      striped
      :items="studies"
      :fields="fields"
      :sort-desc="true"
      :sort-by.sync="sortBy"
      :no-local-sorting="true"
      @sort-changed="sortingChanged"
    >
      <template
        slot="HEAD_is_selected"
        slot-scope="data"
      >
        {{ $t(data.label) }}
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
          v-model="studies.allSelected"
          name="allSelected"
          @click.native.stop
          @change="selectAll(studies.allSelected)"
        />
      </template>

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
        {{ $t(data.label) }}
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
        {{ $t(data.label) }}
      </template>

      <template
        slot="HEAD_AccessionNumber"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.AccessionNumber"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('filter')"
          > <br>
        </div>
        {{ $t(data.label) }}
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
                :bootstrap-styling="false"
                :disabled-dates="disabledFromDates"
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
                v-model="filters.StudyDateTo"
                :bootstrap-styling="false"
                :disabled-dates="disabledToDates"
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
          <!-- <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateFrom' placeholder="From"> - <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateTo' placeholder="To"> <br/> -->
        </div>
        <br v-if="showFilters">
        {{ $t(data.label) }}
      </template>

      <template
        slot="HEAD_ModalitiesInStudy"
        slot-scope="data"
      >
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.ModalitiesInStudy"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('filter')"
          ><br>
        </div>
        {{ $t(data.label) }}
      </template>

      <template
        slot="ModalitiesInStudy"
        slot-scope="data"
      >
        {{ data.item.ModalitiesInStudy[0] | formatModality }}
      </template>

      <template
        slot="is_selected"
        slot-scope="row"
      >
        <b-form-group>
          <b-button
            variant="link"
            size="sm"
            class="mr-2"
            @click.stop="showSeries(row)"
          >
            <v-icon
              v-if="row.detailsShowing"
              class="align-middle"
              name="chevron-down"
              @click.stop="row.toggleDetails"
            />
            <v-icon
              v-else
              class="align-middle"
              name="chevron-right"
              @click.stop="row.toggleDetails"
            />
          </b-button>
          <b-form-checkbox
            v-model="row.item.is_selected"
            @click.native.stop
            @change="toggleSelected(row.item,'study',!row.item.is_selected)"
          />
        </b-form-group>
      </template>

      <!--Infos study (Series / Comments / Study Metadata) -->
      <template
        slot="row-details"
        slot-scope="row"
      >
        <b-card>
          <div class="row">
            <div class="col-xl-auto mb-4">
              <nav class="nav nav-pills nav-justified flex-column">
                <a
                  class="nav-link"
                  :class="(row.item.view=='series')?'active':''"
                  @click="row.item.view='series'"
                >
                  {{ $t('series') }}
                </a>
                <a
                  class="nav-link"
                  :class="(row.item.view=='comments')?'active':''"
                  @click="loadStudiesComments(row.item)"
                >
                  {{ $t('comments') }}
                </a>
                <a
                  class="nav-link"
                  :class="(row.item.view=='study')?'active':''"
                  @click="loadStudiesMetadata(row.item)"
                >
                  {{ $t('study') }}
                </a>
              </nav>
            </div>
            <div
              v-if="row.item.view==='series'"
              class="col-sm-12 col-md-12 col-lg-12 col-xl-10"
            >
              <div class="row">
                <div
                  v-for="serie in row.item.series"
                  :key="serie.id"
                  class="col-sm-12 col-md-12 col-lg-12 col-xl-6 mb-5"
                >
                  <series-summary
                    :key="serie.SeriesInstanceUID[0]"
                    :series-instance-u-i-d="serie.SeriesInstanceUID[0]"
                    :selected="serie.is_selected"
                    :study-instance-u-i-d="row.item.StudyInstanceUID[0]"
                  />
                </div>
              </div>
            </div>

            <div
              v-if="row.item.view==='comments'"
              class="col-md-10"
            >
              <comments-and-notifications
                :id="row.item.StudyInstanceUID[0]"
                scope="studies"
              />
            </div>

            <div
              v-if="row.item.view==='study'"
              class="col-sm-12 col-md-12 col-lg-12 col-xl-10"
            >
              <study-metadata
                :id="row.item.StudyInstanceUID[0]"
                scope="studies"
              />
            </div>
          </div>
        </b-card>
      </template>
      <!-- Button next to patient name -->
      <template
        slot="PatientName"
        slot-scope="row"
      >
        <div class="patientNameContainer">
          <div class="row">
            <div class="patientName col-md-auto">
              {{ row.item.PatientName }}
            </div>
            <div class="patientNameIcons col-md-auto">
              <span
                :class="row.item.is_favorite?'selected':''"
                @click="toggleFavorite(row.index,'study')"
              >
                <v-icon
                  v-if="row.item.is_favorite"
                  class="align-middle"
                  style="margin-right:0"
                  name="star"
                />
                <v-icon
                  v-else
                  class="align-middle"
                  style="margin-right:0"
                  name="star"
                />
              </span>
              <span
                :class="row.item.comments.length?'selected':''"
                @click="handleComments(row)"
              >
                <v-icon
                  v-if="row.item.comments.length"
                  class="align-middle"
                  style="margin-right:0"
                  name="comment"
                />
                <v-icon
                  v-else
                  class="align-middle"
                  style="margin-right:0"
                  name="comment"
                />
              </span>
              <a
                :href="getURLDownload(row.item.StudyInstanceUID)"
                class="download"
              >
                <v-icon
                  class="align-middle"
                  style="margin-right:0"
                  name="download"
                />
              </a>
              <span>
                <v-icon
                  class="align-middle"
                  style="margin-right:0"
                  name="link"
                />
              </span>
            </div>
          </div>
        </div>
      </template>

      <template
        slot="StudyDate"
        slot-scope="data"
      >
        {{ data.item.StudyDate[0] | formatDate }}
      </template>
    </b-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import commentsAndNotifications from '@/components/comments/commentsAndNotifications'
import formGetUser from '@/components/user/getUser'
import seriesSummary from '@/components/inbox/seriesSummary'
import studyMetadata from '@/components/study/studyMetadata.vue'
import ToggleButton from 'vue-js-toggle-button'
import Datepicker from 'vuejs-datepicker'
import Vue from 'vue'

Vue.use(ToggleButton)

export default {
	name: 'Studies',
	components: { seriesSummary, Datepicker, commentsAndNotifications, studyMetadata, formGetUser },
	data () {
		return {
			pageNb: 1,
			active: false,
			form_send_study: false,
			fields: [
				{
					key: 'is_selected',
					label: '',
					sortable: false,
					class: 'td_checkbox',
					thClass: 'd-none d-sm-table-cell'
					// margin: 'auto'
				},
				{
					key: 'PatientName',
					label: 'PatientName',
					thClass: 'd-none d-sm-table-cell',
					tdClass: 'patientName',
					sortable: true
				},
				{
					key: 'PatientID',
					label: 'MRN',
					sortable: true,
					thClass: 'd-none d-md-table-cell',
					tdClass: 'd-none d-md-table-cell'
				},
				{
					key: 'AccessionNumber',
					label: 'AccessionNumber',
					sortable: true,
					thClass: 'd-none d-lg-table-cell',
					tdClass: 'd-none d-lg-table-cell'
				},
				{
					key: 'StudyDate',
					label: 'StudyDate',
					thClass: 'd-none d-sm-table-cell',
					tdClass: 'd-none d-sm-table-cell',
					sortable: true
				},
				{
					key: 'ModalitiesInStudy',
					thClass: 'd-none d-sm-table-cell',
					tdClass: 'd-none d-sm-table-cell',
					label: 'Modality',
					sortable: true
				}
			],
			sortBy: 'StudyDate',
			sortDesc: true,
			limit: 8,
			optionsNbPages: [5, 10, 25, 50, 100],
			showFilters: false,
			filterTimeout: null,
			filters: {
				PatientName: '',
				PatientID: '',
				AccessionNumber: '',
				StudyDateFrom: '',
				StudyDateTo: '',
				ModalitiesInStudy: '',
				inbox_and_albums: false,
				album_id: ''
			}
		}
	},
	computed: {
		...mapGetters({
			studies: 'studies',
			albums: 'albums',
			user: 'currentUser'
		}),
		totalRows () {
			return this.studies.length
		},
		selectedStudiesNb () {
			return _.filter(this.studies, s => { return s.is_selected === true }).length
		},
		disabledToDates: function () {
			let vm = this
			return {
				to: vm.filters.StudyDateFrom,
				from: new Date()
			}
		},
		disabledFromDates: function () {
			return {
				from: new Date()
			}
		},
		allowedAlbums () {
			return _.filter(this.albums, a => { return a.add_series })
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
						PatientName: '',
						PatientID: '',
						AccessionNumber: '',
						StudyDateFrom: '',
						StudyDateTo: '',
						ModalitiesInStudy: '',
						inbox_and_albums: this.filters.inbox_and_albums,
						album_id: this.filters.album_id

					}
				}
			}
		}

	},

	created () {
		if (this.$route.params.album_id) {
			this.filters.album_id = this.$route.params.album_id
		} else {
			this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
			this.$store.dispatch('getAlbums', { pageNb: 1, limit: 40, sortBy: 'created_time', sortDesc: true })
		}
	},

	mounted () {
		this.scroll()
	},
	methods: {
		getURLDownload (StudyInstanceUID) {
			return `${process.env.VUE_APP_URL_API}/link/${this.user.jwt}/studies/${StudyInstanceUID}?accept=application%2Fzip`
		},
		scroll () {
			window.onscroll = () => {
				let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight
				if (bottomOfWindow) {
					this.pageNb++
					this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
				}
			}
		},
		sortingChanged (ctx) {
			// ctx.sortBy   ==> Field key for sorting by (or null for no sorting)
			// ctx.sortDesc ==> true if sorting descending, false otherwise

			this.pageNb = ctx.currentPage
			this.sortBy = ctx.sortBy
			this.sortDesc = ctx.sortDesc
			this.limit = this.studies.length
			this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
		},
		showSeries (row) {
			if (!row.detailsShowing) {
				this.$store.dispatch('getSeries', { StudyInstanceUID: row.item.StudyInstanceUID[0], album_id: this.filters.album_id })
			}
			row.toggleDetails()
		},

		toggleFavorite (index, type) {
			var vm = this
			this.$store.dispatch('toggleFavorite', { type: type, index: index, inbox: true, album: null }).then(res => {
				if (res) vm.$snotify.success(type + 'is now in favorites')
				else vm.$snotify.error('Sorry, an error occured')
			})
		},
		handleComments (row) {
			this.showSeries(row)
			row.item.view = 'comments'
		},
		selectAll (isSelected) {
			this.$store.commit('SELECT_ALL_STUDIES', !isSelected)
			this.studies.allSelected = !this.studies.allSelected
		},
		deleteSelectedStudies () {
			var vm = this
			var i, j
			for (i = this.studies.length - 1; i > -1; i--) {
				if (this.studies[i].is_selected) {
					let selectedSeries = _.filter(this.studies[i].series, s => { return s.is_selected })
					if (this.studies[i].series.length === 0 || this.studies[i].series.length === selectedSeries.length) {
						vm.$store.dispatch('deleteStudy', { StudyInstanceUID: this.studies[i].StudyInstanceUID[0], album_id: this.filters.album_id })
						// vm.$delete(vm.studies, i);
					} else {
						for (j = selectedSeries.length - 1; j > -1; j--) {
							let s = selectedSeries[j]
							vm.$store.dispatch('deleteSeries', { StudyInstanceUID: this.studies[i].StudyInstanceUID[0], SeriesInstanceUID: s.SeriesInstanceUID[0], album_id: this.filters.album_id })
							// vm.$delete(vm.studies[i].series,j);
						}
					}
				}
			}
		},
		toggleSelected (item, type, isSelected) {
			let index = _.findIndex(this.studies, s => { return s.StudyInstanceUID[0] === item.StudyInstanceUID[0] })
			this.$store.dispatch('toggleSelected', { type: type, index: index, is_selected: isSelected })
		},
		downloadSelectedStudies () {
			var vm = this
			_.forEach(this.studies, function (study) {
				if (study.is_selected) {
					vm.$store.dispatch('downloadStudy', { StudyInstanceUID: study.StudyInstanceUID })
				}
			})
		},
		searchOnline () {
			this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit })
		},
		addToAlbum (albumId) {
			let studies = _.filter(this.studies, s => { return s.is_selected })
			let data = []

			_.forEach(studies, s => {
				let series = _.filter(s.series, oneSeries => { return oneSeries.is_selected })
				if (series.length === s.series.length) {
					data.push({ study_id: s.StudyInstanceUID[0], series_id: null, album_id: albumId })
				} else {
					_.forEach(series, oneSeries => {
						data.push({ study_id: s.StudyInstanceUID[0], series_id: oneSeries.SeriesInstanceUID[0], album_id: albumId })
					})
				}
			})

			if (data.length) {
				this.$store.dispatch('putStudiesInAlbum', { data: data }).then(() => {
					this.$snotify.success(this.$t('studyputtoalbum'))
				})
			}
		},
		toggleStudyView (item) {
			this.$store.commit('TOGGLE_STUDY_VIEW', { StudyInstanceUID: item.StudyInstanceUID[0] })
		},
		loadStudiesComments (item) {
			item.view = 'comments'
			// this.$store.dispatch('getStudiesComments',{StudyInstanceUID: item.StudyInstanceUID[0]})
		},
		loadStudiesMetadata (item) {
			item.view = 'study'
		},
		sendToUser (userSub) {
			let studies = _.filter(this.studies, s => { return s.is_selected })
			let studyIds = []; let seriesIds = []
			_.forEach(studies, s => {
				let selectedSeries = _.filter(s.series, oneSeries => { return oneSeries.is_selected })
				if (selectedSeries.length === s.series.length) studyIds.push(s.StudyInstanceUID[0])
				else {
					_.forEach(selectedSeries, oneSeries => {
						seriesIds.push({
							StudyInstanceUID: s.StudyInstanceUID[0],
							SeriesInstanceUID: oneSeries.SeriesInstanceUID[0]
						})
					})
				}
			})

			if (studyIds.length || seriesIds.length) {
				this.$store.dispatch('sendStudies', { StudyInstanceUIDs: studyIds, SeriesInstanceUIDs: seriesIds, user: userSub }).then(res => {
					this.$snotify.success(`${res.success} ${this.$t('studiessharedsuccess')}`)
					if (res.error) this.$snotify.error(`${res.error} ${this.$t('studiessharederror')}`)
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

	.patientNameContainer{
		position: relative;
		white-space: nowrap;
	}

	.patientNameIcons{
		visibility: hidden;
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
</style>

