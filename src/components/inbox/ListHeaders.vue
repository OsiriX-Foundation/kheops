<i18n>
{
	"en": {
		"selectednbstudies": "{count} study is selected | {count} studies are selected",
		"addalbum": "Add to an album",
		"infoFavorites": "Favorites",
		"send": "Send",
		"delete": "Delete",
		"cancel": "Cancel",
		"addfavorites": "Add too favorites",
		"addfavorites": "Remove too favorites",
    "confirmDelete": "Are you sure you want to delete {count} study | Are you sure you want to delete {count} studies",
    "confirmDeleteSeries": "containing {count} serie? Once deleted, you will not be able to re-upload any series if other users still have access to them. | containing {count} series? Once deleted, you will not be able to re-upload any series if other users still have access to them.",
    "importdir": "Import directory",
    "importfiles": "Import files",
    "draganddrop": "Or drag and drop",
		"studiessharedsuccess": "studies sent successfully",
		"studiessharederror": "studies could not be sent",
		"addInbox": "Add to inbox"
	},
	"fr": {
		"selectednbstudies": "{count} étude est sélectionnée | {count} études sont sélectionnées",
		"addalbum": "Ajouter à un album",
		"infoFavorites": "Favoris",
		"send": "Send",
		"delete": "Delete",
		"cancel": "Annuler",
		"addfavorites": "Ajouter aux favoris",
		"addfavorites": "Supprimer des favoris",
		"confirmDelete": "Etes vous de sûr de vouloir supprimer {count} étude | Etes vous de sûr de vouloir supprimer {count} études",
    "confirmDeleteSeries": "contenant {count} série? Une fois supprimée, vous ne pouvais plus charger cette série tant qu'un autre utilisateur a accès à cette série. | contenant {count} séries? Une fois supprimées, vous ne pouvais plus charger ces séries tant qu'un autre utilisateur a accès à ces séries.",
    "importdir": "Importer un dossier",
    "importfiles": "Importer des fichiers",
    "draganddrop": "Ou Drag and Drop",
		"studiessharedsuccess": "études ont été envoyées avec succès",
		"addInbox": "Ajouter à la boite de réception",
		"studiessharederror": "études n'ont pas pu être envoyée"
	}
}
</i18n>

<template>
  <div>
    <div
      class="pt-2"
    >
      <div
        class="d-flex flex-wrap"
      >
        <div class="p-2 align-self-center d-none d-sm-block">
          <span>{{ $tc("selectednbstudies", selectedStudiesNb, { count: selectedStudiesNb }) }}</span>
        </div>
        <div
          v-if="showSendButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
            @click.stop="formSendStudy=!formSendStudy"
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
        <!--
					v-if="!albumId || (album.send_series || album.is_admin)"
				-->
        <b-dropdown
          v-if="showAlbumButton === true"
          :disabled="selectedStudiesNb === 0"
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
            </span><br>
            <span>{{ $t("addalbum") }}</span>
          </template>
          <b-dropdown-item
            v-for="allowedAlbum in allowedAlbums"
            :key="allowedAlbum.id"
            @click.stop="addToAlbum(allowedAlbum.album_id)"
          >
            {{ allowedAlbum.name }}
          </b-dropdown-item>
        </b-dropdown>

        <div
          v-if="showInboxButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
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
          v-if="showFavoriteButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
            @click="favoriteSelectedStudies()"
          >
            <span>
              <v-icon
                class="align-middle"
                name="star"
              />
            </span><br>
            {{ $t("infoFavorites") }}
          </button>
        </div>
        <div
          v-if="showDeleteButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
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
        <div class="ml-auto" />
        <div
          v-if="showImportButton === true"
          class="align-self-center"
        >
          <div>
            <b-dropdown
              id="dropdown-divider"
              class="m-1"
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
            @click="reloadStudies()"
          >
            <v-icon
              name="refresh"
              scale="2"
            />
          </button>
        </div>
        <div
          class="d-none d-sm-block align-self-center"
        >
          <button
            type="button"
            class=" btn btn-link btn-lg"
            @click="setFilters()"
          >
            <v-icon
              name="search"
              scale="2"
            />
          </button>
        </div>
      </div>
    </div>
    <confirm-button
      v-if="confirmDelete && selectedStudiesNb"
      :btn-primary-text="$t('delete')"
      :btn-danger-text="$t('cancel')"
      :text="$tc('confirmDelete',selectedStudiesNb,{count: selectedStudiesNb})"
      :method-confirm="deleteStudies"
      :method-cancel="() => confirmDelete=false"
    />
    <form-get-user
      v-if="formSendStudy && selectedStudiesNb"
      @get-user="sendToUser"
      @cancel-user="formSendStudy=false"
    />
  </div>
</template>

<script>
import ToggleButton from 'vue-js-toggle-button'
import Vue from 'vue'
import { mapGetters } from 'vuex'
import formGetUser from '@/components/user/getUser'
import ConfirmButton from '@/components/inbox/ConfirmButton.vue'
import { HTTP } from '@/router/http'
import AddIcon from '@/components/kheopsSVG/AddIcon'

Vue.use(ToggleButton)

export default {
	name: 'ListHeaders',
	components: { formGetUser, ConfirmButton, AddIcon },
	props: {
		studies: {
			type: Array,
			required: true,
			default: () => ([])
		},
		allowedAlbums: {
			type: Array,
			required: true,
			default: () => ([])
		},
		albumId: {
			type: String,
			required: true,
			default: ''
		},
		showSendButton: {
			type: Boolean,
			required: false,
			default: true
		},
		showAlbumButton: {
			type: Boolean,
			required: false,
			default: true
		},
		showInboxButton: {
			type: Boolean,
			required: false,
			default: true
		},
		showFavoriteButton: {
			type: Boolean,
			required: false,
			default: true
		},
		showDeleteButton: {
			type: Boolean,
			required: false,
			default: true
		},
		showImportButton: {
			type: Boolean,
			required: false,
			default: true
		}
	},
	data () {
		return {
			formSendStudy: false,
			confirmDelete: false,
			showFilters: false
		}
	},
	computed: {
		...mapGetters({
			sendingFiles: 'sending'
		}),
		selectedStudiesNb () {
			return _.filter(this.studies, s => { return (s.flag.is_selected === true || s.flag.is_indeterminate === true) }).length
		},
		selectedStudies () {
			return _.filter(this.studies, s => { return (s.flag.is_selected === true) })
		},
		selectedSeries () {
			let series = {}
			let notAllSeriesSelected = this.studies.filter(study => { return (study.flag.is_indeterminate === true) })
			notAllSeriesSelected.forEach(study => {
				if (study.series !== undefined) {
					let seriesSelected = []
					for (let serieUID in study.series) {
						if (study.series[serieUID].flag.is_selected === true) {
							seriesSelected.push(study.series[serieUID])
						}
					}
					if (seriesSelected.length > 0) {
						series[study.StudyInstanceUID.Value[0]] = seriesSelected
					}
				}
			})
			return series
		},
		allSelectedStudies () {
			return _.filter(this.studies, s => { return (s.flag.is_selected === true || s.flag.is_indeterminate === true) })
		}
	},

	watch: {
		selectedStudiesNb: {
			handler: function (selectedStudiesNb) {
				if (selectedStudiesNb === 0) {
					this.confirmDelete = false
					this.formSendStudy = false
				}
			}
		}
	},
	created () {
	},
	mounted () {
	},
	methods: {
		sendToUser (userSub) {
			if (this.selectedStudiesNb > 0) {
				let promises = []
				this.selectedStudies.forEach(study => {
					let params = {
						StudyInstanceUID: study.StudyInstanceUID.Value[0],
						userSub: userSub,
						queries: this.getSource()
					}
					promises.push(this.$store.dispatch('sendStudy', params))
				})
				for (let studyUID in this.selectedSeries) {
					this.selectedSeries[studyUID].forEach(serie => {

						let params = {
							StudyInstanceUID: studyUID,
							SeriesInstanceUID: serie.SeriesInstanceUID.Value[0],
							userSub: userSub,
							queries: this.getSource()
						}
						promises.push(this.$store.dispatch('sendSerie', params))
					})
				}

				Promise.all(promises).then(res => {
					this.$snotify.success(`${this.selectedStudiesNb} ${this.$t('studiessharedsuccess')}`)
					this.formSendStudy = false
					this.deselectStudySeries()
				}).catch(err => {
					console.log(err)
				})
			}
		},
		setObjectFlagStudy (StudyInstanceUID, flag, value) {
			let paramsIsSelected = {
				StudyInstanceUID: StudyInstanceUID,
				flag: flag,
				value: value
			}
			return paramsIsSelected
		},
		setObjectFlagSerie (StudyInstanceUID, SeriesInstanceUID, flag, value) {
			let paramsIsIndeterminate = {
				StudyInstanceUID: StudyInstanceUID,
				SeriesInstanceUID: SeriesInstanceUID,
				flag: flag,
				value: value
			}
			return paramsIsIndeterminate
		},
		deselectStudySeries () {
			this.allSelectedStudies.forEach(study => {
				let StudyInstanceUID = study.StudyInstanceUID.Value[0]
				if (study.flag.is_selected === true) {
					this.$store.dispatch('setFlagByStudyUID', this.setObjectFlagStudy(StudyInstanceUID, 'is_selected', false))
				}
				if (study.series !== undefined) {
					for (let serieUID in study.series) {
						let serie = study.series[serieUID]
						if (serie.flag.is_selected) {
							let SeriesInstanceUID = serie.SeriesInstanceUID.Value[0]
							this.$store.dispatch('setFlagByStudyUID', this.setObjectFlagStudy(StudyInstanceUID, 'is_indeterminate', false))
							this.$store.dispatch('setFlagByStudyUIDSerieUID', this.setObjectFlagSerie(StudyInstanceUID, SeriesInstanceUID, 'is_selected', false))
						}
					}
				}
			})
		},
		favoriteSelectedStudies () {
			let favorites = this.allSelectedStudies.every(s => { return s.flag.is_favorite === true })
			let params = {
				StudyInstanceUID: '',
				queries: this.getSource(),
				value: !favorites
			}
			this.allSelectedStudies.forEach(study => {
				params.StudyInstanceUID = study.StudyInstanceUID.Value[0]
				this.$store.dispatch('favoriteStudy', params)
			})
		},
		getSource () {
			if (this.albumId === '') {
				return {
					inbox: true
				}
			} else {
				return {
					album: this.albumId
				}
			}
		},
		deleteStudies () {
			this.deleteSelectedStudies()
			this.deleteSelectedSeries()
			this.confirmDelete = false
			this.deselectStudySeries()
		},
		deleteSelectedStudies () {
			this.selectedStudies.forEach(study => {
				let params = {
					StudyInstanceUID: study.StudyInstanceUID.Value[0]
				}
				if (this.albumId === '') {
					this.$store.dispatch('deleteStudyTest', params)
				} else {
					params.album_id = this.albumId
					this.$store.dispatch('removeStudyInAlbum', params)
				}
			})
		},
		deleteSelectedSeries () {
			for (let studyUID in this.selectedSeries) {
				this.selectedSeries[studyUID].forEach(serie => {
					let serieUID = serie.SeriesInstanceUID.Value[0]
					let params = {
						StudyInstanceUID: studyUID,
						SeriesInstanceUID: serieUID
					}
					if (this.albumId === '') {
						this.$store.dispatch('deleteSerieTest', params)
					} else {
						params.album_id = this.albumId
						this.$store.dispatch('removeSerieInAlbum', params)
					}
				})
			}
		},
		addToAlbum (albumId) {
			let queries = this.getSource()
			let data = this.generateStudySerieData(albumId)
			this.$store.dispatch('putStudiesInAlbumTest', { 'queries': queries, 'data': data }).then(res => {
				this.deselectStudySeries()
			})
		},
		generateStudySerieData (albumId) {
			let data = []
			this.selectedStudies.forEach(study => {
				data.push({
					album_id: albumId,
					study_id: study.StudyInstanceUID.Value[0]
				})
			})
			for (let studyUID in this.selectedSeries) {
				this.selectedSeries[studyUID].forEach(serie => {
					data.push({
						album_id: albumId,
						study_id: studyUID,
						serie_id: serie.SeriesInstanceUID.Value[0]
					})
				})
			}
			return data
		},
		addToInbox () {
			alert('Not done !!!')
			let queries = this.getSource()
			let data = this.generateStudySerieData(this.albumId)
			console.log(data)
		},
		determineWebkitDirectory () {
			// https://stackoverflow.com/questions/11381673/detecting-a-mobile-browser
			var tmpInput = document.createElement('input')
			if ('webkitdirectory' in tmpInput && typeof window.orientation === 'undefined') return true
			return false
		},
		setFilters () {
			this.showFilters = !this.showFilters
			this.$emit('setFilters', this.showFilters)
		},
		showDragAndDrop () {
			this.$store.dispatch('setDemoDragAndDrop', true)
		},
		reloadStudies () {
			this.$emit('reloadStudies')
		}
	}
}

</script>
<style scoped>
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

	.inputfile {
    width: 0.1px;
    height: 0.1px;
    opacity: 0;
    overflow: hidden;
    position: absolute;
    z-index: -1;
  }
</style>
