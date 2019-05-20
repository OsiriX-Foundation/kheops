<i18n>
{
	"en": {
		"patientname": "Patient name",
		"patientbirthdate": "Birth date",
		"patientid": "Patient ID",
		"patientsex": "Patient sex",
		"modalitiesinstudy": "Modalities in study",
		"studydate": "Study date",
		"studyid": "Study ID",
		"studytime": "Study time",
		"patientinfo": "Patient details",
		"studyinfo": "Study details",
		"NumberOfStudyRelatedInstances": "Number of instances",
		"NumberOfStudyRelatedSeries": "Number of series"
	},
	"fr": {
		"patientname": "Nom de patient",
		"patientbirthdate": "Année de naissance",
		"patientid": "ID patient",
		"patientsex": "Sexe du patient",
		"modalitiesinstudy": "Modalité d'étude",
		"studydate": "Date de l'étude",
		"studyid": "ID étude",
		"studytime": "Temps d'étude",
		"patientinfo": "Informations du patient",
		"studyinfo": "Information de l'étude",
		"NumberOfStudyRelatedInstances": "Nombre d'instances",
		"NumberOfStudyRelatedSeries": "Nombre de séries"
	}
}
</i18n>

<template>
  <div class="studyMetadataContainer">
    <div class="row">
      <div class="col-xl-1" />
      <div class="col-sm-12 col-md-6 col-lg-6 col-xl-5 mb-3">
        <h5>{{ $t('patientinfo') }}</h5>
        <table class="table table-striped">
          <tbody>
            <tr v-if="metadata.PatientName">
              <th>{{ $t('patientname') }}</th>
              <td>{{ metadata.PatientName }}</td>
            </tr>
            <tr v-if="matchNumbers(metadata.PatientBirthDate)">
              <th>{{ $t('patientbirthdate') }}</th>
              <td>{{ getDate(metadata.PatientBirthDate[0]) }}</td>
            </tr>
            <tr v-if="metadata.PatientID">
              <th>{{ $t('patientid') }}</th>
              <td>{{ metadata.PatientID[0] }}</td>
            </tr>
            <tr v-if="matchSex(metadata.PatientSex[0])">
              <th>{{ $t('patientsex') }}</th>
              <td>{{ metadata.PatientSex[0] }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="col-sm-12 col-md-6 col-lg-6 col-xl-5 mb-3">
        <h5>{{ $t('studyinfo') }}</h5>
        <table class="table table-striped">
          <tbody>
            <tr v-if="metadata.ModalitiesInStudy">
              <th>{{ $t('modalitiesinstudy') }}</th>
              <td>{{ metadata.ModalitiesInStudy[0] }}</td>
            </tr>
            <tr v-if="matchNumbers(metadata.StudyDate)">
              <th>{{ $t('studydate') }}</th>
              <td>{{ metadata.StudyDate[0]|formatDate }}</td>
            </tr>
            <tr v-if="metadata.StudyID">
              <th>{{ $t('studyid') }}</th>
              <td>{{ metadata.StudyID[0] }}</td>
            </tr>
            <tr v-if="matchNumbers(metadata.StudyTime)">
              <th>{{ $t('studytime') }}</th>
              <td>{{ metadata.StudyTime[0] }}</td>
            </tr>
            <tr v-if="matchNumbers(metadata.NumberOfStudyRelatedSeries)">
              <th>{{ $t('NumberOfStudyRelatedSeries') }}</th>
              <td>{{ metadata.NumberOfStudyRelatedSeries[0] }}</td>
            </tr>
            <tr v-if="matchNumbers(metadata.NumberOfStudyRelatedInstances)">
              <th>{{ $t('NumberOfStudyRelatedInstances') }}</th>
              <td>{{ metadata.NumberOfStudyRelatedInstances[0] }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
	name: 'StudyMetadata',
	props: {
		id: {
			type: String,
			required: true
		}
	},
	data () {
		return {}
	},
	computed: {
		...mapGetters({
			studies: 'studies'
		}),
		metadata () {
			let studyIdx = _.findIndex(this.studies, s => {
				return s.StudyInstanceUID[0] === this.id
			})
			if (studyIdx > -1) {
				return this.studies[studyIdx]
			}
			return {}
		}
	},
	methods: {
		getDate (date) {
			var year = date.substr(0, 4)
			var month = date.substr(4, 2)
			var day = date.substr(6, 2)
			return day + '/' + month + '/' + year
		},
		matchSex (sex) {
			return /m|M|o|O|f|F/.test(sex)
		},
		matchNumbers (number) {
			return /^[0-9]*([,.][0-9]*)?$/.test(number)
		}
	}
}

</script>

<style scoped>
div.description {
	width: 290px;
	padding: 0 20px;
	float: left;
}
label {
	font-size: 130%;
}
</style>
