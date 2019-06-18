<i18n>
{
	"en": {
		"selectednbstudies": "{count} study is selected | {count} studies are selected",
		"addalbum": "Add to an album",
		"infoFavorites": "Favorites",
		"addfavorites": "Add too favorites",
		"addfavorites": "Remove too favorites",
    "confirmDelete": "Are you sure you want to delete {count} study | Are you sure you want to delete {count} studies",
		"confirmDeleteSeries": "containing {count} serie? Once deleted, you will not be able to re-upload any series if other users still have access to them. | containing {count} series? Once deleted, you will not be able to re-upload any series if other users still have access to them.",
		"series": "Series",
		"comments": "Comments",
		"study": "Study"
	},
	"fr": {
		"selectednbstudies": "{count} étude est sélectionnée | {count} études sont sélectionnées",
		"addalbum": "Ajouter à un album",
		"infoFavorites": "Favoris",
		"addfavorites": "Ajouter aux favoris",
		"addfavorites": "Supprimer des favoris",
		"confirmDelete": "Etes vous de sûr de vouloir supprimer {count} étude | Etes vous de sûr de vouloir supprimer {count} études",
		"confirmDeleteSeries": "contenant {count} série? Une fois supprimée, vous ne pouvais plus charger cette série tant qu'un autre utilisateur a accès à cette série. | contenant {count} séries? Une fois supprimées, vous ne pouvais plus charger ces séries tant qu'un autre utilisateur a accès à ces séries.",
		"series": "Séries",
		"comments": "Commentaires",
		"study": "Etude"
	}
}
</i18n>

<template>
	<div class="row">
		<div class="col-xl-auto mb-4">
			<nav class="nav nav-pills nav-justified flex-column text-center text-xl-left">
				<a
					class="nav-link"
					:class="(study.flag.view === 'series')?'active':''"
					@click="setViewDetails(study.StudyInstanceUID.Value[0], 'series')"
				>
					{{ $t('series') }}
				</a>
				<a
					class="nav-link"
					:class="(study.flag.view === 'comments')?'active':''"
					@click="setViewDetails(study.StudyInstanceUID.Value[0], 'comments')"
				>
					{{ $t('comments') }}
				</a>
				<a
					class="nav-link"
					:class="(study.flag.view === 'study')?'active':''"
					@click="setViewDetails(study.StudyInstanceUID.Value[0], 'study')"
				>
					{{ $t('study') }}
				</a>
			</nav>
		</div>
		<div
			v-if="study.flag.view === 'series'"
			class="col-sm-12 col-md-12 col-lg-12 col-xl-10"
		>
			<div class="row">
				<div
					v-for="serie in study.series"
					:key="serie.id"
					class="col-sm-12 col-md-12 col-lg-12 col-xl-6 mb-5"
				>
					<series-summary-data-model
						:study="study"
						:serie="serie"
						@selectedSeries="countSelectedSeries"
					/>
				</div>
			</div>
		</div>

		<div
			v-if="study.flag.view === 'comments'"
			class="col-sm-12 col-md-12 col-lg-12 col-xl-10"
		>
			<comments-and-notifications
				:id="study.StudyInstanceUID.Value[0]"
				scope="studies"
			/>
		</div>
		<div
			v-if="study.flag.view === 'study'"
			class="col-sm-12 col-md-12 col-lg-12 col-xl-10"
		>
			<study-metadata
				:id="study.StudyInstanceUID.Value[0]"
				scope="studies"
			/>
		</div>
	</div>
</template>

<script>
	import { HTTP } from '@/router/http'
	import studyMetadata from '@/components/study/studyMetadata.vue'
	import commentsAndNotifications from '@/components/comments/commentsAndNotifications'
	import seriesSummaryDataModel from '@/components/inbox/seriesSummaryDataModel'

	export default {
		name: 'ListItemDetails',
		components: { seriesSummaryDataModel, commentsAndNotifications, studyMetadata },
		props: {
			study: {
				type: Object,
				required: true,
				default: () => ({})
			}
		},
		data () {
			return {
			}
		},
		computed: {
		},

		watch: {
		},
		created () {
		},
		mounted () {
		},
		methods: {
			setViewDetails (StudyInstanceUID, flagView) {
				let viewSelected = flagView === '' ? 'series' : flagView
				let params = {
					StudyInstanceUID: StudyInstanceUID,
					flag: 'view',
					value: viewSelected
				}
				this.$store.dispatch('setFlagByStudyUID', params)
			},
			countSelectedSeries () {
				this.selectedSeriesNb = 0
				this.studies.filter(s => { return s.is_selected }).forEach(function (study) {
					if (study.series.length) this.selectedSeriesNb += study.series.filter(s => { return s.is_selected }).length
					else this.selectedSeriesNb += study.NumberOfStudyRelatedSeries[0]
				}.bind(this))
			}
		}
	}
</script>

<style scoped>
</style>
