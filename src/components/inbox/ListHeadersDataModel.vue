<i18n>
{
	"en": {
		"selectednbstudies": "{count} study is selected | {count} studies are selected",
		"addalbum": "Add to an album",
		"infoFavorites": "Favorites",
		"send": "Send",
		"delete": "Delete",
		"addfavorites": "Add too favorites",
		"addfavorites": "Remove too favorites",
    "confirmDelete": "Are you sure you want to delete {count} study | Are you sure you want to delete {count} studies",
    "confirmDeleteSeries": "containing {count} serie? Once deleted, you will not be able to re-upload any series if other users still have access to them. | containing {count} series? Once deleted, you will not be able to re-upload any series if other users still have access to them."
	},
	"fr": {
		"selectednbstudies": "{count} étude est sélectionnée | {count} études sont sélectionnées",
		"addalbum": "Ajouter à un album",
		"infoFavorites": "Favoris",
		"send": "Send",
		"delete": "Delete",
		"addfavorites": "Ajouter aux favoris",
		"addfavorites": "Supprimer des favoris",
		"confirmDelete": "Etes vous de sûr de vouloir supprimer {count} étude | Etes vous de sûr de vouloir supprimer {count} études",
    "confirmDeleteSeries": "contenant {count} série? Une fois supprimée, vous ne pouvais plus charger cette série tant qu'un autre utilisateur a accès à cette série. | contenant {count} séries? Une fois supprimées, vous ne pouvais plus charger ces séries tant qu'un autre utilisateur a accès à ces séries."
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

        <div
          class="ml-auto align-self-center"
        >
          <!--
          <div>
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
					-->
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
import formGetUser from '@/components/user/getUser'
import ConfirmButton from '@/components/inbox/ConfirmButton.vue'
import { HTTP } from '@/router/http'

Vue.use(ToggleButton)

export default {
	name: 'ListHeadersDataModel',
	components: { formGetUser, ConfirmButton },
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
					let serieNeed = study.series.filter(serie => { return serie.flag.is_selected === true })
					if (serieNeed.length > 0) {
						series[study.StudyInstanceUID.Value[0]] = serieNeed
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
					promises.push(HTTP.put(`studies/${study.StudyInstanceUID.Value[0]}/users/${userSub}?inbox=true`))
				})
				for (let studyUID in this.selectedSeries) {
					this.selectedSeries[studyUID].forEach(serie => {
						let serieUID = serie.SeriesInstanceUID.Value[0]
						promises.push(HTTP.put(`studies/${studyUID}/series/${serieUID}/users/${userSub}?inbox=true`))
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
		deselectStudySeries () {
			let params = {
				flag: 'is_selected',
				value: false
			}
			this.selectedStudies.forEach(study => {
				params.StudyInstanceUID = study.StudyInstanceUID.Value[0]
				this.$store.dispatch('setFlagByStudyUID', params)
				if (study.series !== undefined) {
					study.series.forEach(serie => {
						params.SeriesInstanceUID = serie.SeriesInstanceUID.Value[0]
						this.$store.dispatch('setFlagByStudyUIDSerieUID', params)
					})
				}
			})
			for (let studyUID in this.selectedSeries) {
				params.StudyInstanceUID = studyUID
				this.selectedSeries[studyUID].forEach(serie => {
					params.SeriesInstanceUID = serie.SeriesInstanceUID.Value[0]
					this.$store.dispatch('setFlagByStudyUIDSerieUID', params)
				})
			}
		},
		favoriteSelectedStudies () {
			let favorites = this.allSelectedStudies.every(s => { return s.flag.is_favorite === true })
			let params = {
				StudyInstanceUID: '',
				queries: {
					inbox: true
				},
				value: !favorites
			}
			this.allSelectedStudies.forEach(study => {
				params.StudyInstanceUID = study.StudyInstanceUID.Value[0]
				this.$store.dispatch('favoriteStudy', params)
			})
		},
		deleteStudies () {
			this.deleteSelectedStudies()
			this.deleteSelectedSeries()
			this.confirmDelete = false
		},
		deleteSelectedStudies () {
			this.selectedStudies.forEach(study => {
				let params = {
					StudyInstanceUID: study.StudyInstanceUID.Value[0],
					queries: {
						inbox: true
					}
				}
				this.$store.dispatch('deleteStudyTest', params)
			})
		},
		deleteSelectedSeries () {
			for (let studyUID in this.selectedSeries) {
				this.selectedSeries[studyUID].forEach(serie => {
					let serieUID = serie.SeriesInstanceUID.Value[0]
					let params = {
						StudyInstanceUID: studyUID,
						SeriesInstanceUID: serieUID,
						queries: {
							inbox: true
						}
					}
					this.$store.dispatch('deleteSerieTest', params)
				})
			}
		},
		addToAlbum (albumId) {
			let queries = {
				inbox: true
			}
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
			this.$store.dispatch('putStudiesInAlbumTest', { 'queries': queries, 'data': data }).then(res => {
				this.deselectStudySeries()
			})
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
