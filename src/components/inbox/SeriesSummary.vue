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
      <div class="mb-2 preview">
        <div
          class="d-flex flex-row justify-content-center"
        >
          <img
            v-if="!loadingImage"
            :class="checkSR ? 'pointer' : ''"
            :src="serie.imgSrc"
            width="150"
            height="150"
            @click="checkSR ? openTab(serie) : ''"
          >
          <bounce-loader
            :loading="loadingImage"
            color="white"
          />
        </div>
      </div>
    </div>
    <div class="row mb-2 ml-2">
      <b-form-checkbox
        v-model="isSelected"
      >
        <span
          class="pointer word-break font-white"
        >
          {{ imageTitle }}  <br>
          {{ serieDate }}
        </span>
      </b-form-checkbox>
    </div>
  </div>
</template>

<script>
import Vue from 'vue';
import { mapGetters } from 'vuex';
import BounceLoader from 'vue-spinner/src/BounceLoader.vue';
import { ViewerToken } from '../../mixins/tokens.js';
import { CurrentUser } from '../../mixins/currentuser.js';
import { Viewer } from '@/mixins/viewer.js';

export default {
  name: 'SeriesSummary',
  components: { BounceLoader },
  mixins: [ViewerToken, CurrentUser, Viewer],
  props: {
    serie: {
      type: Object,
      required: true,
      default: () => {},
    },
    study: {
      type: Object,
      required: true,
      default: () => {},
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {};
  },
  computed: {
    ...mapGetters({
      studies: 'studies',
      series: 'series',
    }),
    checkSR() {
      if (this.serie.Modality !== undefined && this.serie.Modality.Value !== undefined) {
        return !this.serie.Modality.Value[0].includes('SR');
      }
      return true;
    },
    imageTitle() {
      let modality = '';
      let description = '';
      let numberInstances = '';
      if (this.serie.Modality !== undefined && this.serie.Modality.Value !== undefined) {
        [modality] = this.serie.Modality.Value;
      }
      if (this.serie.NumberOfSeriesRelatedInstances !== undefined && this.serie.NumberOfSeriesRelatedInstances.Value !== undefined) {
        numberInstances = `[ ${this.serie.NumberOfSeriesRelatedInstances.Value[0]} ]`;
      }
      if (this.serie.SeriesDescription !== undefined && this.serie.SeriesDescription.Value !== undefined) {
        [description] = this.serie.SeriesDescription.Value;
      }
      return `${modality} - ${description} ${numberInstances}`;
    },
    serieDate() {
      let seriesDate = '';
      let seriesTime = '';
      if (this.serie.SeriesDate && this.serie.SeriesDate.Value !== undefined) {
        seriesDate = Vue.options.filters.formatDate(this.serie.SeriesDate.Value[0]);
      }
      if (this.serie.SeriesTime && this.serie.SeriesTime.Value !== undefined) {
        seriesTime = Vue.options.filters.formatTM(this.serie.SeriesTime.Value[0]);
      }
      return `${seriesDate} ${seriesTime}`;
    },
    seriesInstanceUID() {
      return this.serie.SeriesInstanceUID.Value[0];
    },
    studyInstanceUID() {
      return this.study.StudyInstanceUID.Value[0];
    },
    selected() {
      return this.serie.flag.is_selected;
    },
    isSelected: {
      // getter
      get() {
        return this.selected;
      },
      // setter
      set(newValue) {
        const params = {
          StudyInstanceUID: this.studyInstanceUID,
          SeriesInstanceUID: this.seriesInstanceUID,
          flag: 'is_selected',
          value: newValue,
        };
        if (this.serie.flag.is_selected !== newValue) {
          this.$store.dispatch('setFlagByStudyUIDSerieUID', params).then(() => {
            this.setCheckBoxStudy();
          });
        }
      },
    },
    loadingImage() {
      return (this.serie.imgSrc === '');
    },
  },
  watch: {
  },
  created() {
    // this.setImageSerie()
  },
  methods: {
    /*
    setImageSerie () {
      let params = {
        StudyInstanceUID: this.studyInstanceUID,
        SeriesInstanceUID: this.seriesInstanceUID
      }
      this.loading = true
      this.$store.dispatch('setSerieImage', params).then(res => {
        this.loading = false
      })
    },
    */
    setCheckBoxStudy() {
      if (this.checkAllSerieSelected(this.study, true)) {
        this.$store.dispatch('setFlagByStudyUID', {
          StudyInstanceUID: this.studyInstanceUID,
          flag: 'is_indeterminate',
          value: false,
        });
        this.$store.dispatch('setFlagByStudyUID', {
          StudyInstanceUID: this.studyInstanceUID,
          flag: 'is_selected',
          value: true,
        });
      } else if (this.checkAllSerieSelected(this.study, false)) {
        this.$store.dispatch('setFlagByStudyUID', {
          StudyInstanceUID: this.studyInstanceUID,
          flag: 'is_indeterminate',
          value: false,
        });
        this.$store.dispatch('setFlagByStudyUID', {
          StudyInstanceUID: this.studyInstanceUID,
          flag: 'is_selected',
          value: false,
        });
      } else {
        this.$store.dispatch('setFlagByStudyUID', {
          StudyInstanceUID: this.studyInstanceUID,
          flag: 'is_indeterminate',
          value: true,
        });
        this.$store.dispatch('setFlagByStudyUID', {
          StudyInstanceUID: this.studyInstanceUID,
          flag: 'is_selected',
          value: false,
        });
      }
    },
    checkAllSerieSelected(study, value) {
      let allSelected = true;
      Object.keys(this.series[this.studyInstanceUID]).forEach((serieUID) => {
        allSelected = allSelected && (this.series[this.studyInstanceUID][serieUID].flag.is_selected === value);
      });
      return allSelected;
    },
    getSourceQueries() {
      if (Object.keys(this.source).length > 0) {
        return `${encodeURIComponent(this.source.key)}=${encodeURIComponent(this.source.value)}`;
      }
      return '';
    },
    openTab(series) {
      const SOPVideo = '1.2.840.10008.5.1.4.1.1.77.1.4.1';
      const SOPPdf = '1.2.840.10008.5.1.4.1.1.104.1';
      const token = this.currentuserAccessToken();
      const openWSI = series.Modality !== undefined
        && series.Modality.Value !== undefined
        && series.Modality.Value[0] === 'SM';
      const windowProps = {};
      if (series.SOPClassUID !== undefined && (series.SOPClassUID.Value[0] === SOPPdf || series.SOPClassUID.Value[0] === SOPVideo)) {
        windowProps.name = `WADO-${this.seriesInstanceUID}`;
        windowProps.id = 'WADO';
      } else if (openWSI) {
        windowProps.name = `WSI-${this.studyInstanceUID}`;
        windowProps.id = 'WSI';
      } else if (this.checkSR) {
        windowProps.name = `OHIF-${this.studyInstanceUID}`;
        windowProps.id = 'OHIF';
      }
      const openWindow = window.open('', windowProps.name);
      this.getViewerToken(token, this.studyInstanceUID, this.source).then((res) => {
        const viewerToken = res.data.access_token;
        let url = '';
        if (windowProps.id === 'WADO') {
          const contentType = series.SOPClassUID.Value[0] === SOPPdf ? 'application/pdf' : 'video/mp4';
          const queryparams = `?studyUID=${this.studyInstanceUID}&seriesUID=${this.seriesInstanceUID}&requestType=WADO&contentType=${contentType}`;
          url = this.openWADO(this.studyInstanceUID, viewerToken, queryparams);
          openWindow.location.href = url;
        } else if (windowProps.id === 'WSI') {
          url = this.openWSI(this.studyInstanceUID, viewerToken);
          openWindow.location.href = url;
        } else if (windowProps.id === 'OHIF') {
          const sourceQuery = this.getSourceQueries();
          url = this.openOhif(this.studyInstanceUID, viewerToken, sourceQuery);
          openWindow.location.href = url;
        }
      }).catch((err) => {
        console.log(err);
      });
    },
  },
};

</script>
