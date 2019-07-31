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
    "favorites": "Favorites",
		"nomorestudies": "No more studies"
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
    "favorites": "Favorites",
		"nomorestudies": "Plus d'études"
	}
}
</i18n>

<template>
  <div>
    <list-headers-data-model
      :studies="studies"
			:allowed-albums="albums"
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
      :busy="UI.loading"
      @sort-changed="sortingChanged"
      @row-hovered="setItemHover"
      @row-unhovered="setItemHover"
      @row-clicked="showRowDetails"
    >
      <div
        slot="table-busy"
        class="text-center my-2"
      >
        <strong>Loading...</strong>
      </div>
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
            inline
            @change="setChecked(row)"
          />
        </b-button-group>
      </template>
      <template
        slot="PatientName"
        slot-scope="row"
      >
        {{ row.value }}
        <list-icons
          :study="row.item"
        />
      </template>
      <!--Infos study (Series / Comments / Study Metadata) -->
      <template
        slot="row-details"
        slot-scope="row"
      >
        <b-card>
          <list-item-details
            :study-u-i-d="row.item.StudyInstanceUID.Value[0]"
          />
        </b-card>
      </template>
    </b-table>
    <infinite-loading
      spinner="spiral"
      @infinite="infiniteHandler"
    >
      <div slot="no-more">
        {{ $t('nomorestudies') }}
      </div>
      <div slot="no-results" />
      <!--
      <div
        slot="error"
        slot-scope="{ trigger }"
      >
        Error message, click <a
          href="javascript:;"
          @click="trigger"
        >here</a> to retry
      </div>
			-->
    </infinite-loading>
  </div>
</template>

<script>
// https://peachscript.github.io/vue-infinite-loading/guide/start-with-hn.html
import { mapGetters } from 'vuex'
import ListHeadersDataModel from '@/components/inbox/ListHeadersDataModel'
import ListIcons from '@/components/inbox/ListIcons'
import ListItemDetails from '@/components/inbox/ListItemDetails.vue'
import InfiniteLoading from 'vue-infinite-loading'

export default {
	name: 'StudiesDataModel',
	components: { ListHeadersDataModel, ListIcons, ListItemDetails, InfiniteLoading },
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
			UI: {
				loading: false,
				studiesFlag: []
			},
			studiesParams: {
				offset: 0,
				limit: 15,
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
						return value.Value[0]['Alphabetic']
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
						return value.Value[0]
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
						return value.Value[0]
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
						return value.Value[0]
					},
					thStyle: {
						'width': '150px'
					}
				}
			}
		}
	},
	computed: {
		...mapGetters({
			studies: 'studiesTest',
			albums: 'albumsTest'
		}),
		OS () {
			return navigator.platform
		}
	},
	watch: {
	},
	created () {
		this.$store.dispatch('initStudiesTest', {})
		let queriesAlbums = {
			canAddSeries: true
		}
		this.$store.dispatch('getAlbumsTest', {'queries': queriesAlbums})
	},
	mounted () {
	},
	methods: {
		// https://peachscript.github.io/vue-infinite-loading/old/#!/getting-started/trigger-manually
		infiniteHandler ($state) {
			let params = {
				limit: this.studiesParams.limit,
				offset: this.studiesParams.offset,
				inbox: true,
				includefield: ['favorite', 'comments', '00081030'],
				sort: (this.studiesParams.sortDesc ? '-' : '') + this.studiesParams.sortBy
			}
			this.$store.dispatch('getStudiesTest', { queries: params }).then(res => {
				if (res.status === 200 && res.data.length > 0) {
					this.studiesParams.offset += this.studiesParams.limit
					$state.loaded()
				} else {
					$state.complete()
				}
			})
		},
		setItemHover (item, index, event) {
			let params = {
				StudyInstanceUID: item.StudyInstanceUID.Value[0],
				flag: 'is_hover',
				value: !item.flag.is_hover
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
		setChecked (row) {
			let value = row.item.flag.is_selected
			let StudyInstanceUID = row.item.StudyInstanceUID.Value[0]
			let studyIndex = this.studies.findIndex(study => {
				return study.StudyInstanceUID.Value[0] === StudyInstanceUID
			})
			let params = {
				StudyInstanceUID: StudyInstanceUID,
				studyIndex: studyIndex,
				flag: 'is_selected',
				value: !value
			}
			this.$store.dispatch('setFlagByStudyUID', params)
			if (row.item.series !== undefined) {
				this.setSeriesCheck(row.item.series, params)
			}
		},
		setSeriesCheck (series, params) {
			series.forEach((serie, index) => {
				params.SeriesInstanceUID = serie.SeriesInstanceUID.Value[0]
				params.serieIndex = index
				this.$store.dispatch('setFlagByStudyUID', params)
				this.$store.dispatch('setFlagByStudyUIDSerieUID', params)
			})
		},
		sortingChanged (ctx) {
			this.UI.loading = true
			this.studiesParams.sortDesc = ctx.sortDesc
			this.studiesParams.sortBy = ctx.sortBy
			let params = {
				limit: this.studiesParams.offset,
				offset: 0,
				inbox: true,
				includefield: ['favorite', 'comments', '00081030'],
				sort: (this.studiesParams.sortDesc ? '-' : '') + this.studiesParams.sortBy
			}
			this.$store.dispatch('initStudiesTest', { })
			this.$store.dispatch('getStudiesTest', { queries: params }).then(res => {
				this.UI.loading = false
			})
		},
		showRowDetails (item, index, event) {
			if (!item._showDetails) {
				this.setViewDetails(item.StudyInstanceUID.Value[0], item.flag.view)
				item._showDetails = true
			} else {
				item._showDetails = false
			}
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
