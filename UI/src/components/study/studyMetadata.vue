<template>
  <div class="studyMetadataContainer">
    <div class="row">
      <div class="col-xl-1" />
      <div class="col-sm-12 col-md-6 col-lg-6 col-xl-5 mb-3">
        <h5>{{ $t('study.patientinfo') }}</h5>
        <table
          class="table table-striped-color-reverse word-break table-nohover"
        >
          <tbody>
            <tr v-if="checkUndefined(metadata, 'PatientName') && metadata.PatientName.Value[0]['Alphabetic'] !== undefined">
              <th>{{ $t('study.PatientName') }}</th>
              <td>{{ metadata.PatientName.Value[0]['Alphabetic'] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'PatientBirthDate') && matchNumbers(metadata.PatientBirthDate.Value[0])">
              <th>{{ $t('study.PatientBirthdate') }}</th>
              <td>{{ getDate(metadata.PatientBirthDate.Value[0]) }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'PatientID')">
              <th>{{ $t('study.PatientID') }}</th>
              <td>{{ metadata.PatientID.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'PatientSex') && matchSex(metadata.PatientSex)">
              <th>{{ $t('study.PatientSex') }}</th>
              <td>{{ metadata.PatientSex.Value[0] }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="col-sm-12 col-md-6 col-lg-6 col-xl-5 mb-3">
        <h5>{{ $t('study.studyinfo') }}</h5>
        <table
          class="table table-striped-color-reverse word-break table-nohover"
        >
          <tbody>
            <tr v-if="checkUndefined(metadata, 'ModalitiesInStudy')">
              <th>{{ $t('study.Modality') }}</th>
              <td>{{ metadata.ModalitiesInStudy.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyDate') && matchNumbers(metadata.StudyDate.Value[0])">
              <th>{{ $t('study.StudyDate') }}</th>
              <td>{{ metadata.StudyDate.Value[0]|formatDate }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyID')">
              <th>{{ $t('study.StudyId') }}</th>
              <td>{{ metadata.StudyID.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyInstanceUID')">
              <th>{{ $t('study.StudyInstanceUID') }}</th>
              <td>{{ metadata.StudyInstanceUID.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'StudyTime') && matchNumbers(metadata.StudyTime.Value[0])">
              <th>{{ $t('study.StudyTime') }}</th>
              <td>{{ metadata.StudyTime.Value[0] | formatTM }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'NumberOfStudyRelatedSeries') && matchNumbers(metadata.NumberOfStudyRelatedSeries.Value[0])">
              <th>{{ $t('study.NumberOfStudyRelatedSeries') }}</th>
              <td>{{ metadata.NumberOfStudyRelatedSeries.Value[0] }}</td>
            </tr>
            <tr v-if="checkUndefined(metadata, 'NumberOfStudyRelatedInstances') && matchNumbers(metadata.NumberOfStudyRelatedInstances.Value[0])">
              <th>{{ $t('study.NumberOfStudyRelatedInstances') }}</th>
              <td>{{ metadata.NumberOfStudyRelatedInstances.Value[0] }}</td>
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
