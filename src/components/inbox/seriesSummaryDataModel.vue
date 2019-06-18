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
					v-model="isSelected"
        >
					<span
          	v-if="serie.SeriesDescription"
					>
          {{ serie.SeriesDescription.Value[0] }}
					</span>
					<span
						v-else
					>
          	No description
					</span>
        </b-form-checkbox>
      </div>
    </div>

    <div class="row justify-content-center">
      <div class="mb-2 preview">
				<img
          :class="!serieTest.Modality.Value[0].includes('SR') ? 'cursor-img' : ''"
          :src="serieTest.imgSrc"
          width="250"
          height="250"
          @click="openTab(serieTest)"
        >
      </div>
      <div class="col col-mb-2 col-sm-10 col-md-8 col-lg-6 description">
        <table class="table table-striped">
          <tbody>
            <tr v-if="serie.Modality">
              <th>{{ $t('modality') }}</th>
              <td>{{ serie.Modality.Value[0] }}</td>
            </tr>
            <tr v-if="serie.RetrieveAETitle">
              <th>{{ $t('applicationentity') }}</th>
              <td>{{ serie.RetrieveAETitle.Value[0] }}</td>
            </tr>
            <tr v-if="serie.NumberOfSeriesRelatedInstances">
              <th>{{ $t('numberimages') }}</th>
              <td>{{ serie.NumberOfSeriesRelatedInstances.Value[0] }}</td>
            </tr>
            <tr v-if="serie.SeriesDescription">
              <th>{{ $t('description') }}</th>
              <td>{{ serie.SeriesDescription.Value[0] }}</td>
            </tr>
            <tr v-if="serie.SeriesDate">
              <th>{{ $t('seriesdate') }}</th>
              <td>{{ serie.SeriesDate.Value[0]|formatDate }}</td>
            </tr>
            <tr v-if="serie.SeriesTime">
              <th>{{ $t('seriestime') }}</th>
              <td>{{ serie.SeriesTime.Value[0] }}</td>
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
		study: {
			type: Object,
			required: true,
			default: () => ({})
		},
		serie: {
			type: Object,
			required: true,
			default: () => ({})
		}
	},
	data () {
		return {
			serieTest: {}
		}
	},
	computed: {
		...mapGetters({
			studies: 'studiesTest'
		}),
		series() {
			let idx = _.findIndex(this.studies, s => { return s.StudyInstanceUID.Value[0] === this.studyInstanceUID })
			if (idx > -1) {
				let sidx = _.findIndex(this.studies[idx].series, s => { return s.SeriesInstanceUID.Value[0] === this.seriesInstanceUID })
				return this.studies[idx].series[sidx]
			}
			return {}
		},
		studyInstanceUID () {
			return this.study.StudyInstanceUID.Value[0]
		},
		seriesInstanceUID () {
			return this.serie.SeriesInstanceUID.Value[0]
		},
		selected () {
			console.log(this.serieTest)
			return this.serieTest.flag.is_selected
		},
		source () {
			return $route.params.album_id ? $route.params.album_id : 'inbox'
		},
		isSelected: {
			// getter
			get: function () {
				return this.selected
			},
			// setter
			set: function (newValue) {
				let params = {
					StudyInstanceUID: this.studyInstanceUID,
					SeriesInstanceUID: this.seriesInstanceUID,
					flag: 'is_selected',
					value: newValue
				}
				this.$store.dispatch('setFlagByStudyUIDSerieUID', params)
				this.setCheckBoxStudy(newValue)	
			}
		}
	},
	watch: {
		studies: {
			handler: function (studies) {
				// console.log(studies[0].series[0])
				this.setSeries()
			},
			deep: true
		}
	},
	created () {
		this.setSeries()
	},
	methods: {
		setSeries () {
			let idx = _.findIndex(this.studies, s => { return s.StudyInstanceUID.Value[0] === this.studyInstanceUID })
			if (idx > -1) {
				let sidx = _.findIndex(this.studies[idx].series, s => { return s.SeriesInstanceUID.Value[0] === this.seriesInstanceUID })
				console.log(this.studies[idx].series[sidx].imgSrc)
				this.serieTest = this.studies[idx].series[sidx]
			}
		},
		setCheckBoxStudy (value) {
			let paramsStudy = {
				StudyInstanceUID: this.studyInstanceUID,
				flag: 'is_selected',
				value: value
			}
			if (!this.study.flag.is_selected && value === true) {
				this.$store.dispatch('setFlagByStudyUID', paramsStudy)
			} else if (this.noSeriesSelected()) {
				this.$store.dispatch('setFlagByStudyUID', paramsStudy)
			}
		},
		noSeriesSelected () {
			return this.study.series.every(serie => { return serie.flag.is_selected === false })
		},
		openTab (series) {
			const SOPVideo = '1.2.840.10008.5.1.4.1.1.77.1.4.1'
			const SOPPdf = '1.2.840.10008.5.1.4.1.1.104.1'

			if (series.SOPClassUID !== undefined && (series.SOPClassUID[0] === SOPPdf || series.SOPClassUID[0] === SOPVideo)) {
				let contentType = series.SOPClassUID[0] === SOPPdf ? 'application/pdf' : 'video/mp4'
				this.openWADO(series, contentType)
			} else if (series.Modality[0] !== 'SR') {
				this.openViewer()
			}
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
