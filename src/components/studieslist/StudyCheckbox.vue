<template>
  <b-form-checkbox
    v-model="flag.is_selected"
    :indeterminate="flag.is_indeterminate"
    class="mr-0"
    inline
    @change="setChecked()"
  />
</template>
<script>
import { mapGetters } from 'vuex';

export default {
  name: 'StudyCheckbox',
  components: {},
  props: {
    flag: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    studyInstanceUID: {
      type: String,
      required: true,
      default: '',
    },
  },
  computed: {
    ...mapGetters({
      studies: 'studies',
      series: 'series',
    }),
  },
  methods: {
    setChecked() {
      const value = this.flag.is_selected;
      const StudyInstanceUID = this.studyInstanceUID;
      const studyIndex = this.studies.findIndex((study) => study.StudyInstanceUID.Value[0] === StudyInstanceUID);
      const paramsSelected = this.createObjectFlag(StudyInstanceUID, studyIndex, 'is_selected', !value);
      this.$store.dispatch('setFlagByStudyUID', paramsSelected);
      const paramsIndeterminate = this.createObjectFlag(StudyInstanceUID, studyIndex, 'is_indeterminate', false);
      this.$store.dispatch('setFlagByStudyUID', paramsIndeterminate);
      if (this.series[StudyInstanceUID] !== undefined) {
        this.setSeriesCheck(this.series[StudyInstanceUID], paramsSelected);
      }
    },
    setSeriesCheck(series, params) {
      const paramsSetFlag = params;
      Object.keys(series).forEach((serieUID) => {
        paramsSetFlag.SeriesInstanceUID = serieUID;
        this.$store.dispatch('setFlagByStudyUIDSerieUID', paramsSetFlag);
      });
    },
    createObjectFlag(StudyInstanceUID, studyIndex, flag, value) {
      return {
        StudyInstanceUID,
        studyIndex,
        flag,
        value,
      };
    },
  },
};
</script>
