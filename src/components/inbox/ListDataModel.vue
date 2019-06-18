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
  <div>
    <list-headers-data-model
      :studies="studies"
    />
    <b-table
      stacked="sm"
      striped
      hover
      :items="studies"
      :fields="fields"
      :sort-desc="true"
      tbody-tr-class="link"
      @row-hovered="setItemHover"
      @row-unhovered="setItemHover"
    >
      <template
        slot="is_selected"
        slot-scope="row"
      >
        <b-button-group>
          <b-button
            variant="link"
            size="sm"
            class="mr-1 pt-0"
          >
            <v-icon
              v-if="row.detailsShowing"
              class="align-middle"
              name="chevron-down"
            />
            <v-icon
              v-else
              class="align-middle"
              name="chevron-right"
            />
          </b-button>
          <b-form-checkbox
            v-model="row.item.flag.is_selected"
            inline
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
    </b-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import ListHeadersDataModel from '@/components/inbox/ListHeadersDataModel'
import ListIcons from '@/components/inbox/ListIcons'

export default {
	name: 'StudiesDataModel',
	components: { ListHeadersDataModel, ListIcons },
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
			fields: {
				isSelected: {
					key: 'is_selected',
					label: '',
					sortable: false,
					class: 'td_checkbox breakword'
				},
				PatientName: {
					label: this.$t('PatientName'),
					sortable: true,
					tdClass: 'breakwork',
					formatter: (value, key, item) => {
						return value.Value[0]['Alphabetic']
					}
				},
				PatientID: {
					label: this.$t('PatientID'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'breakword d-none d-md-table-cell d-lg-table-cell',
					formatter: (value, key, item) => {
						return value.Value[0]
					}
				},
				StudyDescription: {
					label: this.$t('StudyDescription'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'breakword d-none d-lg-table-cell',
					formatter: (value, key, item) => {
						return value.Value[0]
					}
				},
				StudyDate: {
					label: this.$t('StudyDate'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'breakword d-none d-sm-table-cell d-md-table-cell d-lg-table-cell',
					formatter: (value, key, item) => {
						return value.Value[0]
					}
				},
				ModalitiesInStudy: {
					label: this.$t('Modality'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'breakword d-none d-sm-table-cell',
					formatter: (value, key, item) => {
						return value.Value[0]
					}
				}
			}
		}
	},

	computed: {
		...mapGetters({
			studies: 'studiesTest'
		}),
		OS () {
			return navigator.platform
		}
	},

	watch: {
		studies: {
			handler: function (studies) {
				if (studies.length > 0) {
					this.UI.loading = false
				}
			}
		}
	},

	created () {
		this.UI.loading = true
		let params = {
			limit: 100,
			offset: 0,
			inbox: true,
			includefield: ['favorite', 'comments', '00081030']
		}
		this.$store.dispatch('getStudiesTest', { queries: params })
	},

	mounted () {
	},

	methods: {
		setItemHover (item, index, event) {
			let params = {
				StudyInstanceUID: item.StudyInstanceUID.Value[0],
				index: index,
				flag: 'is_hover',
				value: !item.flag.is_hover
			}
			this.$store.dispatch('setFlagByStudyUID', params)
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
