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
    "StudyInstanceUID": "Study Instance UID",
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
    "StudyInstanceUID": "Study Instance UID",
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
        <table
          class="table table-striped-color-reverse word-break table-nohover"
        >
          <tbody>
            <tr v-if="checkUndefined(metadata, 'PatientName') && metadata.PatientName.Value[0]['Alphabetic'] !== undefined">
              <th>{{ $t('patientname') }}</th>
              <td>{{ metadata.PatientName.Value[0]['Alphabetic'] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'PatientBirthDate') && matchNumbers(metadata.PatientBirthDate.Value[0])">
              <th>{{ $t('patientbirthdate') }}</th>
              <td>{{ getDate(metadata.PatientBirthDate.Value[0]) }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'PatientID')">
              <th>{{ $t('patientid') }}</th>
              <td>{{ metadata.PatientID.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'PatientSex') && matchSex(metadata.PatientSex)">
              <th>{{ $t('patientsex') }}</th>
              <td>{{ metadata.PatientSex.Value[0] }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="col-sm-12 col-md-6 col-lg-6 col-xl-5 mb-3">
        <h5>{{ $t('studyinfo') }}</h5>
        <table
          class="table table-striped-color-reverse word-break table-nohover"
        >
          <tbody>
            <tr v-if="checkUndefined(metadata, 'ModalitiesInStudy')">
              <th>{{ $t('modalitiesinstudy') }}</th>
              <td>{{ metadata.ModalitiesInStudy.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyDate') && matchNumbers(metadata.StudyDate.Value[0])">
              <th>{{ $t('studydate') }}</th>
              <td>{{ metadata.StudyDate.Value[0]|formatDate }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyID')">
              <th>{{ $t('studyid') }}</th>
              <td>{{ metadata.StudyID.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyInstanceUID')">
              <th>{{ $t('StudyInstanceUID') }}</th>
              <td>{{ metadata.StudyInstanceUID.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyTime') && matchNumbers(metadata.StudyTime.Value[0])">
              <th>{{ $t('studytime') }}</th>
              <td>{{ metadata.StudyTime.Value[0] | formatTM }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'NumberOfStudyRelatedSeries') && matchNumbers(metadata.NumberOfStudyRelatedSeries.Value[0])">
              <th>{{ $t('NumberOfStudyRelatedSeries') }}</th>
              <td>{{ metadata.NumberOfStudyRelatedSeries.Value[0] }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudyMetadata',
  props: {
    id: {
      type: String,
      required: true,
    },
  },
  data() {
    return {};
  },
  computed: {
    metadata() {
      return this.$store.getters.getStudyByUID(this.id);
    },
  },
  methods: {
    getDate(date) {
      const year = date.substr(0, 4);
      const month = date.substr(4, 2);
      const day = date.substr(6, 2);
      return `${day}/${month}/${year}`;
    },
    matchSex(sex) {
      return /m|M|o|O|f|F/.test(sex);
    },
    matchNumbers(number) {
      return /^[0-9]*([,.][0-9]*)?$/.test(number);
    },
    checkUndefined(value, id) {
      return value[id] !== undefined && value[id].Value !== undefined;
    },
  },
};

</script>
