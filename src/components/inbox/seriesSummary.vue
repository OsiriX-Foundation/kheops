<i18n>{
	"en": {
		"modality": "Modality",
		"numberimages": "Number of images",
		"description": "Description",
		"seriesdate": "Series date",
		"seriestime": "Series time",
		"openviewer": "Open viewer",
		"applicationentity": "Application entity"
	},
	"fr": {
		"modality": "Modalité",
		"numberimages": "Nombre d'images",
		"description": "Description",
		"seriesdate": "Date de la série",
		"seriestime": "Heure de la série",
		"openviewer": "Ouvrir une visionneuse",
		"applicationentity": "Application entity"
	}
}
</i18n>

<template>
  <div class="seriesSummaryContainer">
    <div class="row justify-content-center">
      <div class="mb-2">
        <b-form-checkbox
          v-if="series.SeriesDescription"
          v-model="isSelected"
        >
          {{ series.SeriesDescription[0] }}
        </b-form-checkbox>

        <b-form-checkbox
          v-else
          v-model="isSelected"
        >
          No description
        </b-form-checkbox>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="preview">
        <img
          v-if="series.Modality !== 'SR'"
          class="cursor-img"
          :src="previewImg()"
          width="250"
          height="250"
          @click="openViewer"
        >
        <img
          v-else
          :src="previewImg()"
          width="250"
          height="250"
        >
      </div>
      <!--
				div - col col-mb-2 col-md-auto description
				table - table table-striped col-md-auto
			-->
      <div class="col col-mb-2 col-sm-10 col-md-8 col-lg-6 description">
        <table class="table table-striped">
          <tbody>
            <tr v-if="series.Modality">
              <th>{{ $t('modality') }}</th>
              <td>{{ series.Modality[0] }}</td>
            </tr>
            <tr v-if="series.RetrieveAETitle">
              <th>{{ $t('applicationentity') }}</th>
              <td>{{ series.RetrieveAETitle[0] }}</td>
            </tr>
            <tr v-if="series.NumberOfSeriesRelatedInstances">
              <th>{{ $t('numberimages') }}</th>
              <td>{{ series.NumberOfSeriesRelatedInstances[0] }}</td>
            </tr>
            <tr v-if="series.SeriesDescription">
              <th>{{ $t('description') }}</th>
              <td>{{ series.SeriesDescription[0] }}</td>
            </tr>
            <tr v-if="series.SeriesDate">
              <th>{{ $t('seriesdate') }}</th>
              <td>{{ series.SeriesDate[0]|formatDate }}</td>
            </tr>
            <tr v-if="series.SeriesTime">
              <th>{{ $t('seriestime') }}</th>
              <td>{{ series.SeriesTime[0] }}</td>
            </tr>
          </tbody>
        </table>

        <dl
          v-if="series.Modality !== 'SR'"
          class="row justify-content-center"
        >
          <dd>
            <button
              type="button"
              class="btn-primary btn-sm"
              @click="openViewer"
            >
              {{ $t('openviewer') }}
            </button>
          </dd>
        </dl>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
	name: 'SeriesSummary',
	props: {
		seriesInstanceUID: {
			type: String,
			required: true
		},
		studyInstanceUID: {
			type: String,
			required: true
		},
		selected: {
			type: Boolean,
			required: true
		}
	},
	data () {
		return {}
	},
	computed: {
		...mapGetters({
			studies: 'studies',
			user: 'currentUser'
		}),
		series () {
			let studyIndex = _.findIndex(this.studies, s => { return s.StudyInstanceUID[0] === this.studyInstanceUID })
			if (studyIndex > -1) {
				let seriesIndex = _.findIndex(this.studies[studyIndex].series, d => { return d.SeriesInstanceUID[0] === this.seriesInstanceUID })
				if (seriesIndex > -1) return this.studies[studyIndex].series[seriesIndex]
			}
			return {}
		},
		isSelected: {
			// getter
			get: function () {
				return this.selected
			},
			// setter
			set: function (newValue) {
				let studyIndex = _.findIndex(this.studies, s => { return s.StudyInstanceUID[0] === this.studyInstanceUID })
				if (studyIndex > -1) {
					let seriesIndex = _.findIndex(this.studies[studyIndex].series, d => { return d.SeriesInstanceUID[0] === this.seriesInstanceUID })
					if (seriesIndex > -1) {
						this.$store.dispatch('toggleSelected', { type: 'series', index: studyIndex + ':' + seriesIndex, selected: newValue })
					}
				}
			}
		}
	},
	methods: {
		toggleChecked () {
			this.isSelected = !this.isSelected
		},
		previewImg () {
			return this.series.imgSrc
		},
		openViewer () {
			let url = `${process.env.VUE_APP_URL_API}/studies/${this.studyInstanceUID}/ohifmetadata?firstseries=${this.seriesInstanceUID}`
			window.open(`${process.env.VUE_APP_URL_VIEWER}/?url=${encodeURIComponent(url)}#token=${this.user.jwt}`, 'OHIFViewer')
		}
	}
}

</script>

<style scoped>
div.preview{
	width: 290px;
	padding: 0 20px;
	float: left;
}
div.seriesSummaryContainer{
	font-size: 90%;
	line-height: 1.5em;
}
label{
	font-size: 130%;
}
.cursor-img{
	cursor: pointer;
}

</style>
