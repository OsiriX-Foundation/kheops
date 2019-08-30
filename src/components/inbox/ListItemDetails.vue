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
      class="col-sm-12 col-md-12 col-lg-12 col-xl-10"
    >
      <div
        v-if="study.flag.view === 'series'"
      >
        <div
          v-if="loadingSerie === false"
          class="row"
        >
          <div
            v-for="serie in series[studyUID]"
            :key="serie.id"
            class="col-sm-12 col-md-12 col-lg-12 col-xl-6 mb-5"
          >
            <series-summary
              :serie="serie"
              :study="study"
            />
          </div>
        </div>
        <pulse-loader
          :loading="loadingSerie"
          color="white"
        />
      </div>

      <comments-and-notifications
        v-if="study.flag.view === 'comments'"
        :id="study.StudyInstanceUID.Value[0]"
        scope="studies"
      />
      <study-metadata
        v-if="study.flag.view === 'study'"
        :id="study.StudyInstanceUID.Value[0]"
        scope="studies"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import commentsAndNotifications from '@/components/comments/commentsAndNotifications'
import studyMetadata from '@/components/study/studyMetadata'
import PulseLoader from 'vue-spinner/src/PulseLoader.vue'
import SeriesSummary from '@/components/inbox/SeriesSummaryObject'

export default {
	name: 'ListItemDetails',
	components: { commentsAndNotifications, studyMetadata, PulseLoader, SeriesSummary },
	props: {
		studyUID: {
			type: String,
			required: true,
			default: ''
		},
		albumId: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {
			loadingSerie: true,
			includefield: ['00080021', '00080031']
		}
	},
	computed: {
		...mapGetters({
			series: 'series',
			sendingFiles: 'sending'
		}),
		study () {
			return this.$store.getters.getStudyByUID(this.studyUID)
		}
	},
	watch: {
		sendingFiles () {
			if (!this.sendingFiles) {
				this.getSeries()
			}
		}
	},
	created () {
		this.getSeries()
	},
	methods: {
		getSeries () {
			let params = {
				StudyInstanceUID: this.studyUID,
				studySelected: this.study.flag.is_selected,
				queries: {}
			}
			params.queries = this.getSource()
			params.queries.includefield = this.includefield
			this.loadingSerie = true
			this.$store.dispatch('getSeries', params).then(res => {
				if (res.status === 200) {
					this.loadingSerie = false
				}
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
		setViewDetails (StudyInstanceUID, flagView) {
			let viewSelected = flagView === '' ? 'series' : flagView
			let params = {
				StudyInstanceUID: StudyInstanceUID,
				flag: 'view',
				value: viewSelected
			}
			this.$store.dispatch('setFlagByStudyUID', params)
		}
	}
}
</script>

<style scoped>
</style>
