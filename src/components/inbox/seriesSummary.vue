<i18n>{
	"en": {
		"modality": "Modality",
		"numberimages": "Number of images",
		"description": "Description",
		"seriesdate": "Series date",
		"seriestime": "Series time",
		"openviewer": "Open OHIF viewer",
		"applicationentity": "Application entity"
	},
	"fr": {
		"modality": "Modalité",
		"numberimages": "Nombre d'images",
		"description": "Description",
		"seriesdate": "Date de la série",
		"seriestime": "Heure de la série",
		"openviewer": "Ouvrir la visionneuse OHIF",
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
      <div class="mb-2 preview">
        <img
          :class="!series.Modality.includes('SR') ? 'cursor-img' : ''"
          :src="series.imgSrc"
          width="250"
          height="250"
          @click="openTab(series)"
        >
      </div>
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
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { ViewerToken } from '../../mixins/tokens.js'
import { CurrentUser } from '../../mixins/currentuser.js'

export default {
	name: 'SeriesSummary',
	mixins: [ ViewerToken, CurrentUser ],
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
		},
		source: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {}
	},
	computed: {
		...mapGetters({
			studies: 'studies'
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
						this.$store.dispatch('toggleSelected', { type: 'series', index: studyIndex + ':' + seriesIndex, selected: newValue }).then(res => {
							this.$emit('selectedSeries')
						})
					}
				}
			}
		}
	},
	methods: {
		toggleChecked () {
			this.isSelected = !this.isSelected
		},
		openTab (series) {
			const SOPVideo = '1.2.840.10008.5.1.4.1.1.77.1.4.1'
			const SOPPdf = '1.2.840.10008.5.1.4.1.1.104.1'

			if (series.SOPClassUID[0] === SOPPdf || series.SOPClassUID[0] === SOPVideo) {
				let contentType = series.SOPClassUID[0] === SOPPdf ? 'application/pdf' : 'video/mp4'
				this.openWADO(series, contentType)
			} else if (series.Modality[0] !== 'SR') {
				this.openViewer()
			}
			/*
			if (series.Modality[0] === 'DOC' && series.NumberOfSeriesRelatedInstances[0] === 1) {
				this.openWADO(series)
			} else if (series.Modality[0] !== 'SR') {
				this.openViewer()
			}
			*/
		},
		openViewer () {
			let ohifWindow = window.open('', 'OHIFViewer')
			this.getViewerToken(this.currentuserAccessToken, this.studyInstanceUID, this.source).then(res => {
				let url = `${process.env.VUE_APP_URL_API}/studies/${this.studyInstanceUID}/ohifmetadata?firstseries=${this.seriesInstanceUID}`
				ohifWindow.location.href = `${process.env.VUE_APP_URL_VIEWER}/?url=${encodeURIComponent(url)}#token=${res.data.access_token}`
			}).catch(err => {
				console.log(err)
			})
		},

		openWADO (series, contentType) {
			let wadoWindow = window.open('', 'WADO')
			this.getViewerToken(this.currentuserAccessToken, this.studyInstanceUID, this.source).then(res => {
				let queryparams = `?studyUID=${this.studyInstanceUID}&seriesUID=${this.seriesInstanceUID}&requestType=WADO&contentType=${contentType}`
				wadoWindow.location.href = `${process.env.VUE_APP_URL_API}/link/${res.data.access_token}/wado${queryparams}`
			}).catch(err => {
				console.log(err)
			})
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
