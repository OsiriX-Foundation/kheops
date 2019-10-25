<i18n>
{
  "en": {
    "description": "Description",
    "upload": "Send"
  },
  "fr": {
    "description": "Description",
    "upload": "Envoyer"
  }
}
</i18n>

<template>
  <div>
    <!--
    <div
      v-if="createStudy"
    >
      <b>Study information</b>

      <input
        v-model="dicomValue.patientName.value"
        type="text"
        :placeholder="$t('patientName')"
        class="form-control mb-1"
      >
      <input
        v-model="dicomValue.patientID.value"
        type="text"
        :placeholder="$t('patientID')"
        class="form-control mb-1"
      >
      <input
        v-model="dicomValue.patientBirthDate.value"
        type="text"
        :placeholder="$t('patientBirthDate')"
        class="form-control mb-1"
      >
      <input
        v-model="dicomValue.patientSex.value"
        type="text"
        :placeholder="$t('patientSex')"
        class="form-control mb-1"
      >
      <input
        v-model="dicomValue.studyInstanceUID.value"
        type="text"
        :placeholder="$t('studyInstanceUID')"
        class="form-control mb-1"
      >
    </div>
    --------------------------------------------
    -->
    <div
      v-for="file in filesToDicomize"
      :key="file.id"
      class="mb-2"
    >
      <b class="word-break">
        {{ file.name }}
      </b>
      <br>
      <input
        v-model="manageFiles[file.name].description.value"
        type="text"
        :placeholder="$t('description')"
        class="form-control"
        @keydown.enter.prevent="validDicomValue"
      >
    </div>
    <div
      class="d-flex justify-content-center mt-2 mb-1"
    >
      <button
        class="btn btn-primary"
        @click="validDicomValue()"
      >
        {{ $t('upload') }}
      </button>
    </div>
  </div>
</template>

<script>

export default {
  name: 'InputDicomize',
  props: {
    filesToDicomize: {
      type: Array,
      required: true,
      default: () => [],
    },
    createStudy: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {
      dicomValue: {
        description: {
          tag: '0008103E',
          value: '',
          vr: 'LO',
        },
        patientName: {
          tag: '00100010',
          value: '',
          vr: 'PN',
        },
        patientID: {
          tag: '00100020',
          value: '',
          vr: 'LO',
        },
        patientBirthDate: {
          tag: '00100030',
          value: '',
          vr: 'DA',
        },
        patientSex: {
          tag: '00100040',
          value: '',
          vr: 'CS',
        },
        studyInstanceUID: {
          tag: '0020000D',
          value: '',
          vr: 'UI',
        },
        studyDate: {
          tag: '00080020',
          value: '',
          vr: 'DA',
        },
        studyTime: {
          tag: '00080030',
          value: '',
          vr: 'TM',
        },
        studyPhysician: {
          tag: '00080090',
          value: '',
          vr: 'PN',
        },
        studyID: {
          tag: '00200010',
          value: '',
          vr: 'SH',
        },
        accessionNumber: {
          tag: '00080050',
          value: '',
          vr: 'SH',
        },
      },
      manageFiles: {},
    };
  },
  computed: {
  },
  watch: {
  },
  created() {
    Object.keys(this.filesToDicomize).forEach((key) => {
      this.manageFiles[this.filesToDicomize[key].name] = _.cloneDeep(this.dicomValue);
    });
  },
  mounted() {
  },
  destroyed() {
  },
  methods: {
    validDicomValue() {
      this.$emit('valid-dicom-value', this.manageFiles);
    },
  },
};
</script>
