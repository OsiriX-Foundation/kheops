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
		"studiessharedsuccess": "studies sent successfully",
		"studiessharederror": "studies could not be sent",
		"addInbox": "Add to inbox",
		"nostudy": "No studies found",
		"studiessend": "studies send to inbox",
    "confirmDelete": "Are you sure you want to delete {count} study | Are you sure you want to delete {count} studies",
    "confirmDeleteSeries": "containing {count} serie? Once deleted, you will not be able to re-upload any series if other users still have access to them. | containing {count} series? Once deleted, you will not be able to re-upload any series if other users still have access to them.",
    "cancel": "Cancel",
    "importdir": "Import directory",
    "importfiles": "Import files",
    "draganddrop": "Or drag and drop",
    "favorites": "Favorites"
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
		"studiessharedsuccess": "études ont été envoyées avec succès",
		"studiessharederror": "études n'ont pas pu être envoyée",
		"addInbox": "Add to inbox",
    "nostudy": "Aucne étude trouvée",
		"studiessend": "études envoyées dans votre boîte de réception",
    "confirmDelete": "Etes vous de sûr de vouloir supprimer ? ",
    "cancel": "Annuler",
    "importdir": "Importer un dossier",
    "importfiles": "Importer des fichiers",
    "draganddrop": "Ou Drag and Drop",
    "favorites": "Favorites"
	}
}
</i18n>

<template>
  <div
    v-if="!loading"
  >
    <!--
      https://www.w3schools.com/howto/howto_js_sticky_header.asp
      http://jsfiddle.net/jpXjH/6/
    -->
    <div
      id="myHeader"
      ref="myHeader"
      :class="isActive ? 'sticky' : ''"
      class="pt-2"
    >
      <div
        class="d-flex flex-wrap"
      >
        <div class="p-2 align-self-center d-none d-sm-block">
          <span>{{ $tc("selectednbstudies",selectedStudiesNb,{count: selectedStudiesNb}) }}</span>
        </div>
        <div
          v-if="!filters.album_id || (album.is_admin || album.send_series)"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="disableBtnHeader"
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
        </div>
        <div
          v-if="(!filters.album_id || (album.send_series || album.is_admin)) && allowedAlbums.length > 0"
          class="align-self-center"
        >
          <b-dropdown
            variant="link"
            size="sm"
            no-caret
            :disabled="disableBtnHeader"
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
              v-for="allowedAlbum in allowedAlbums"
              :key="allowedAlbum.id"
              @click.stop="addToAlbum(allowedAlbum.album_id)"
            >
              {{ allowedAlbum.name }}
            </b-dropdown-item>
          </b-dropdown>
        </div>

        <div
          v-if="filters.album_id && (album.send_series || album.is_admin)"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="disableBtnHeader"
            @click="addToInbox()"
          >
            <span>
              <v-icon
                class="align-middle"
                name="bars"
              />
            </span><br>
            {{ $t("addInbox") }}
          </button>
        </div>
        <div
          v-if="!filters.album_id"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="disableBtnHeader"
            @click="addSelectedStudiesFavorite()"
          >
            <span>
              <v-icon
                class="align-middle"
                name="star"
              />
            </span><br>
            {{ $t(infoFavorites) }}
          </button>
        </div>
        <div
          v-if="!filters.album_id || (album.is_admin || album.delete_series)"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="disableBtnHeader"
            @click="confirmDelete=!confirmDelete"
          >
            <span>
              <v-icon
                class="align-middle"
                name="trash"
              />
            </span><br>
            {{ $t("delete") }}
          </button>
        </div>
        <div
          class="ml-auto align-self-center"
        >
          <div
            v-if="(!filters.album_id || (album.add_series || album.is_admin))"
          >
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
            <b-dropdown
              id="dropdown-divider"
              class="m-2"
              variant="link"
              right
            >
              <template slot="button-content">
                <add-icon
                  width="30px"
                  height="30px"
                />
              </template>
              <b-dropdown-item-button
                :disabled="sendingFiles"
              >
                <label for="file">
                  {{ $t("importfiles") }}
                </label>
              </b-dropdown-item-button>
              <b-dropdown-item-button
                v-if="determineWebkitDirectory()"
                :disabled="sendingFiles"
              >
                <label for="directory">
                  {{ $t("importdir") }}
                </label>
              </b-dropdown-item-button>
              <b-dropdown-divider />
              <b-dropdown-item-button
                v-if="determineWebkitDirectory()"
                @click="showDragAndDrop"
              >
                {{ $t("draganddrop") }}
              </b-dropdown-item-button>
            </b-dropdown>
          </div>
        </div>

        <div
          class="d-none d-sm-block align-self-center"
        >
          <button
            type="button"
            class=" btn btn-link btn-lg"
            @click="showFilters=!showFilters"
          >
            <v-icon
              name="search"
              scale="2"
            />
          </button>
        </div>
      </div>
      <confirm-button
        v-if="confirmDelete && selectedStudiesNb"
        :btn-primary-text="$t('delete')"
        :btn-danger-text="$t('cancel')"
        :text="$tc('confirmDelete',selectedStudiesNb,{count: selectedStudiesNb}) + ' ' +
          $tc('confirmDeleteSeries', selectedSeriesNb, {count: selectedSeriesNb})"
        :method-confirm="deleteSelectedStudies"
        :method-cancel="() => confirmDelete=false"
      />
      <form-get-user
        v-if="form_send_study && selectedStudiesNb"
        @get-user="sendToUser"
        @cancel-user="form_send_study=false"
      />
    </div>

    <div
      ref="studiesList"
      class="content"
    >
      <b-table
        class="container-fluid"
        striped
        :items="studies"
        :fields="fields"
        :sort-desc="true"
        :sort-by.sync="sortBy"
        :no-local-sorting="true"
        :no-sort-reset="true"
        :tbody-class="'table-wrapper-scroll-y'"
        @sort-changed="sortingChanged"
        @row-clicked="showDetailsOnRow"
      >
        <template
          slot="HEAD_is_selected"
          slot-scope="data"
        >
          {{ $t(data.label) }}
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
            v-model="studies.allSelected"
            name="allSelected"
            @click.native.stop
            @change="selectAll(studies.allSelected)"
          />
          -->
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
          <b-button-group>
            <b-button
              variant="link"
              size="sm"
              class="mr-1 pt-0"
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
              inline
              @click.native.stop
              @change="toggleSelected(row.item,'study',!row.item.is_selected)"
            />
          </b-button-group>
        </template>

        <!--Infos study (Series / Comments / Study Metadata) -->
        <template
          slot="row-details"
          slot-scope="row"
        >
          <b-card>
            <div class="row">
              <div class="col-xl-auto mb-4">
                <nav class="nav nav-pills nav-justified flex-column text-center text-xl-left">
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
                v-if="row.item.view==&quot;series&quot;"
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
                      :source="$route.params.album_id ? $route.params.album_id : 'inbox'"
                      @selectedSeries="countSelectedSeries"
                    />
                  </div>
                </div>
              </div>

              <div
                v-if="row.item.view==&quot;comments&quot;"
                class="col-sm-12 col-md-12 col-lg-12 col-xl-10"
              >
                <comments-and-notifications
                  :id="row.item.StudyInstanceUID[0]"
                  scope="studies"
                />
              </div>

              <div
                v-if="row.item.view==&quot;study&quot;"
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
              <div class="patientName_ct col-md-auto d-block d-sm-none">
                {{ row.item.ModalitiesInStudy [0] | formatModality }}
              </div>
              <div class="patientNameIcons col-md-auto">
                <span
                  :class="row.item.is_favorite?'selected':''"
                  @click.stop="toggleFavorite(row.item)"
                >
                  <v-icon
                    v-if="row.item.is_favorite"
                    class="align-middle"
                    style="margin-right:1"
                    name="star"
                  />
                  <v-icon
                    v-else-if="Object.keys(album).length === 0 || (album.add_series || album.is_admin)"
                    class="align-middle"
                    style="margin-right:1"
                    name="star"
                    color="grey"
                  />
                </span>
                <span
                  :class="row.item.SumComments[0]?'selected':''"
                  @click.stop="handleComments(row)"
                >
                  <v-icon
                    v-if="row.item.SumComments[0]"
                    class="align-middle"
                    style="margin-right:1"
                    name="comment-dots"
                  />
                  <v-icon
                    v-else
                    class="align-middle"
                    style="margin-right:1"
                    name="comment"
                    color="grey"
                  />
                </span>
                <a
                  v-if="!filters.album_id || (album.download_series || album.is_admin)"
                  href="#"
                  class="download"
                  @click.stop="getURLDownload(row.item.StudyInstanceUID)"
                >
                  <v-icon
                    class="align-middle"
                    style="margin-right:1"
                    name="download"
                  />
                </a>
                <span
                  v-if="OS.match(/(Mac|iPhone|iPod|iPad)/i)"
                  @click.stop="openViewer(row.item.StudyInstanceUID, 'Osirix')"
                >
                  <osirix-icon
                    width="22px"
                    height="22px"
                  />
                </span>
                <span
                  v-if="row.item.ModalitiesInStudy[0] !== 'SR'"
                  @click.stop="openViewer(row.item.StudyInstanceUID, 'Ohif')"
                >
                  <visibility-icon
                    width="24px"
                    height="24px"
                  />
                </span>
                <label
                  for="file"
                  style="cursor:pointer; display: inline;"
                  @click="studyUIDadd=row.item.StudyInstanceUID[0]"
                >
                  <add-icon
                    width="24px"
                    height="24px"
                  />
                </label>
                <!--
                <span><v-icon class="align-middle" style="margin-right:0" name="link"></v-icon></span>
                -->
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

        <template
          slot="PatientID"
          slot-scope="data"
        >
          {{ data.item.PatientID[0] }}
        </template>

        <template
          slot="StudyDescription"
          slot-scope="data"
        >
          {{ data.item.StudyDescription[0] }}
        </template>
      </b-table>
    </div>
    <div
      v-if="studies.length===0"
      style="text-align:center;"
      class="card"
    >
      <div
        class="card-body"
      >
        {{ $t('nostudy') }}
      </div>
    </div>
  </div>
  <div
    v-else
    class="container-fluid"
    style="margin-top: 30px; text-align:center;"
  >
    <pulse-loader
      :loading="loading"
      color="white"
    />
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
// https://github.com/greyby/vue-spinner
import PulseLoader from 'vue-spinner/src/PulseLoader.vue'
import ConfirmButton from '@/components/inbox/ConfirmButton.vue'
import OsirixIcon from '@/components/kheopsSVG/OsirixIcon.vue'
import VisibilityIcon from '@/components/kheopsSVG/VisibilityIcon.vue'
import { ViewerToken } from '@/mixins/tokens.js'
import AddIcon from '@/components/kheopsSVG/AddIcon'

Vue.use(ToggleButton)

export default {
	name: 'Studies',
	components: { seriesSummary, Datepicker, commentsAndNotifications, studyMetadata, formGetUser, PulseLoader, ConfirmButton, OsirixIcon, VisibilityIcon, AddIcon },
	mixins: [ ViewerToken ],
	props: {
		album: {
			type: Object,
			required: false,
			default: () => ({})
		}
	},
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
					class: 'td_checkbox breakword'
				},
				{
					key: 'PatientName',
					label: 'PatientName',
					tdClass: 'patientName',
					sortable: true,
					class: 'breakword'
				},
				{
					key: 'PatientID',
					label: 'PatientID',
					sortable: true,
					class: 'breakword d-none d-md-table-cell d-lg-table-cell'
				},
				{
					key: 'StudyDescription',
					label: 'StudyDescription',
					sortable: false,
					class: 'breakword d-none d-lg-table-cell'
				},
				{
					key: 'StudyDate',
					label: 'StudyDate',
					sortable: true,
					class: 'breakword d-none d-sm-table-cell d-md-table-cell d-lg-table-cell'
				},
				{
					key: 'ModalitiesInStudy',
					label: 'Modality',
					sortable: false,
					class: 'breakword d-none d-sm-table-cell'
				}
			],
			sortBy: 'StudyDate',
			sortDesc: true,
			limit: 100,
			optionsNbPages: [5, 10, 25, 50, 100],
			showFilters: false,
			filterTimeout: null,
			filters: {
				PatientName: '',
				PatientID: '',
				StudyDescription: '',
				StudyDateFrom: '',
				StudyDateTo: '',
				ModalitiesInStudy: '',
				inbox_and_albums: false,
				album_id: ''
			},
			loading: true,
			send: {
				expected: 0,
				count: 0
			},
			confirmDelete: false,
			selectedSeriesNb: 0,
			isActive: false,
			studyUIDadd: ''
		}
	},
	computed: {
		...mapGetters({
			studies: 'studies',
			albums: 'albums',
			sendingFiles: 'sending'
		}),
		totalRows () {
			return this.studies.length
		},
		selectedStudiesNb () {
			this.countSelectedSeries()
			return _.filter(this.studies, s => { return s.is_selected === true }).length
		},
		infoFavorites () {
			/*
      if (this.studies.filter(s => { return s.is_selected }).every(s => { return s.is_favorite === true })) {
				return 'removefavorite'
			} else {
				return 'addfavorite'
      }
      */
			return 'favorites'
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
			return _.filter(this.albums, a => { return (a.add_series || a.is_admin) && this.filters.album_id !== a.album_id })
		},
		OS () {
			return navigator.platform
		},
		disableBtnHeader () {
			return !this.selectedSeriesNb
		},
		access_token () {
			return Vue.prototype.$keycloak.token
		}
	},

	watch: {
		sendingFiles () {
			if (!this.sendingFiles) {
				this.getStudies()
			}
		},
		selectedStudiesNb: {
			handler: function (selectedStudiesNb) {
				if (selectedStudiesNb === 0) {
					this.confirmDelete = false
					this.form_send_study = false
				}
			}
		},
		send: {
			handler: function (send) {
				if (send.expected === send.count) {
					this.$snotify.success(`${this.send.expected} ${this.$t('studiessend')}`)
				}
			},
			deep: true
		},
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
						StudyDescription: '',
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
		this.setLoading(true)
		if (this.$route.params.album_id) {
			this.$store.commit('RESET_FLAGS')
			this.filters.album_id = this.$route.params.album_id
		} else {
			this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit, includefield: ['favorite', 'comments', '00081030'], resetDisplay: true })
				.then(() => {
					this.setLoading(false)
				})
			this.$store.dispatch('getAlbums', { pageNb: 1, limit: 40, sortBy: 'created_time', sortDesc: true })
		}
	},

	mounted () {
		this.scroll()
	},
	methods: {
		getStudies () {
			this.$store.dispatch('getStudies', {
				pageNb: this.pageNb,
				filters: this.filters,
				sortBy: this.sortBy,
				sortDesc: this.sortDesc,
				limit: this.limit,
				includefield: ['favorite', 'comments', '00081030']
			})
		},
		getURLDownload (StudyInstanceUID) {
			const source = this.$route.params.album_id === undefined ? 'inbox' : this.$route.params.album_id
			this.getViewerToken(this.access_token, StudyInstanceUID, source).then(res => {
				const queryparams = `accept=application%2Fzip&${source === 'inbox' ? 'inbox=true' : 'album=' + source}`
				const URL = `${process.env.VUE_APP_URL_API}/link/${res.data.access_token}/studies/${StudyInstanceUID}?${queryparams}`
				location.href = URL
			}).catch(err => {
				console.log(err)
			})
		},
		scroll () {
			const _this = this
			window.onscroll = () => {
				if (this.$route.params.album_id && this.$route.query.view !== 'studies') return

				let bottomOfWindow = Math.floor((document.documentElement.scrollTop || document.body.scrollTop)) + Math.floor(window.innerHeight) === document.documentElement.offsetHeight
				if (bottomOfWindow) {
					this.pageNb++
					this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit, includefield: ['favorite', 'comments', '00081030'] })
				}

				let sticky = _this.$refs.myHeader.offsetTop
				let heightSticky = _this.$refs.myHeader.clientHeight
				let studiesList = _this.$refs.studiesList.offsetTop
				if ((window.pageYOffset) > sticky - heightSticky && !this.isActive) {
					this.isActive = true
				} else if (window.pageYOffset < studiesList - heightSticky) {
					this.isActive = false
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
			this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit, includefield: ['favorite', 'comments', '00081030'] })
		},
		showSeries (row) {
			if (!row.item.detailsShowing) {
				this.$store.dispatch('getSeries', { StudyInstanceUID: row.item.StudyInstanceUID[0], album_id: this.filters.album_id })
			}
			this.toggleDetails(row)
		},
		toggleDetails (row) {
			this.$store.commit('TOGGLE_DETAILS', { StudyInstanceUID: row.item.StudyInstanceUID[0] })
			row.toggleDetails()
		},
		showDetailsOnRow (row) {
			if (!row._showDetails) {
				this.$store.dispatch('getSeries', { StudyInstanceUID: row.StudyInstanceUID[0], album_id: this.filters.album_id })
			}
			this.$store.commit('TOGGLE_DETAILS', { StudyInstanceUID: row.StudyInstanceUID[0] })
			row._showDetails = !row._showDetails
		},
		toggleFavorite (study) {
			var vm = this
			let params = this.$route.params.album_id === undefined ? { inbox: 'true' } : { album: this.$route.params.album_id }
			this.$store.dispatch('toggleFavorite', { type: 'study', StudyInstanceUID: study.StudyInstanceUID[0], queryparams: params }).then(res => {
				if (!res) vm.$snotify.error('Sorry, an error occured')
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
			this.$store.dispatch('getStudies', { pageNb: this.pageNb, filters: this.filters, sortBy: this.sortBy, sortDesc: this.sortDesc, limit: this.limit, includefield: ['favorite', 'comments', '00081030'] })
				.then(res => {
					this.setLoading(false)
				})
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
			let src = this.filters.album_id ? this.filters.album_id : 'inbox'
			if (data.length) {
				this.$store.dispatch('putStudiesInAlbum', { data: data, src: src }).then(() => {
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
				this.$store.dispatch('sendStudies', { StudyInstanceUIDs: studyIds, SeriesInstanceUIDs: seriesIds, user: userSub, src: this.filters.album_id ? this.filters.album_id : 'inbox' }).then(res => {
					this.$snotify.success(`${studies.length} ${this.$t('studiessharedsuccess')}`)
					if (res.error) this.$snotify.error(`${res.error} ${this.$t('studiessharederror')}`)
				})
			}
		},
		addSelectedStudiesFavorite () {
			let studies = this.studies.filter(s => { return s.is_selected })
			let favorites = studies.every(s => { return s.is_favorite === true }) ||
				studies.every(s => { return s.is_favorite === false })
			studies.forEach(study => {
				if (favorites) this.toggleFavorite(study, 'study')
				else if (study.is_favorite === false) this.toggleFavorite(study, 'study')
			})
		},
		addToInbox () {
			let studies = this.studies.filter(s => { return s.is_selected })
			this.send.expected = studies.length
			this.send.count = 0
			studies.forEach(study => {
				let selectedSeries = study.series.filter(serie => { return serie.is_selected })
				if (selectedSeries.length === study.series.length) {
					this.$store.dispatch('selfAppropriateSeries', {
						StudyInstanceUID: study.StudyInstanceUID[0],
						AlbumId: this.album.album_id
					}).then(res => {
						this.send.count++
						if (res.error) this.$snotify.error(`${res.error} error`)
					})
				} else {
					selectedSeries.forEach(serie => {
						let tmp = ''
						this.$store.dispatch('selfAppropriateSeries', {
							StudyInstanceUID: serie.StudyInstanceUID[0],
							SeriesInstanceUID: serie.SeriesInstanceUID[0]
						}).then(res => {
							if (tmp !== serie.StudyInstanceUID[0]) this.send.count++
							if (res.error) this.$snotify.error(`${res.error} error`)
							tmp = serie.StudyInstanceUID[0]
						})
					})
				}
			})
		},
		setLoading (val) {
			this.loading = val
		},
		countSelectedSeries () {
			this.selectedSeriesNb = 0
			this.studies.filter(s => { return s.is_selected }).forEach(function (study) {
				if (study.series.length) this.selectedSeriesNb += study.series.filter(s => { return s.is_selected }).length
				else this.selectedSeriesNb += study.NumberOfStudyRelatedSeries[0]
			}.bind(this))
		},
		openViewer (StudyInstanceUID, viewer) {
			const source = this.$route.params.album_id === undefined ? 'inbox' : this.$route.params.album_id
			let ohifWindow
			if (viewer === 'Ohif') {
				ohifWindow = window.open('', 'OHIFViewer')
			}
			this.getViewerToken(this.access_token, StudyInstanceUID, source).then(res => {
				if (viewer === 'Osirix') {
					this.openOsiriX(StudyInstanceUID, res.data.access_token)
				} else if (viewer === 'Ohif') {
					this.openOhif(StudyInstanceUID, res.data.access_token, source === 'inbox' ? 'inbox=true' : 'album=' + source, ohifWindow)
				}
			}).catch(err => {
				console.log(err)
			})
		},
		openOsiriX (StudyInstanceUID, token) {
			let url = `${process.env.VUE_APP_URL_API}/link/${token}/studies/${StudyInstanceUID}?accept=application/zip`
			window.open(`osirix://?methodName=downloadURL&URL='${encodeURIComponent(url)}'`, '_self')
		},
		openOhif (StudyInstanceUID, token, queryparams, ohifWindow) {
			let url = `${process.env.VUE_APP_URL_API}/studies/${StudyInstanceUID}/ohifmetadata?${queryparams}`
			ohifWindow.location.href = `${process.env.VUE_APP_URL_VIEWER}/?url=${encodeURIComponent(url)}#token=${token}`
		},
		inputLoadFiles () {
			if (this.$refs.inputfiles.files.length > 0) {
				const filesFromInput = this.$refs.inputfiles.files
				this.$emit('loadfiles', filesFromInput, this.studyUIDadd)
				this.initStudyUIDadd()
			}
		},
		inputLoadDirectories () {
			if (this.$refs.inputdir.files.length > 0) {
				const filesFromInput = this.$refs.inputdir.files
				this.$emit('loaddirectories', filesFromInput, this.studyUIDadd)
				this.initStudyUIDadd()
			}
		},
		determineWebkitDirectory () {
			// https://stackoverflow.com/questions/11381673/detecting-a-mobile-browser
			var tmpInput = document.createElement('input')
			if ('webkitdirectory' in tmpInput && typeof window.orientation === 'undefined') return true

			return false
		},
		showDragAndDrop () {
			this.$emit('demohover')
		},
		initStudyUIDadd () {
			this.studyUIDadd = ''
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
