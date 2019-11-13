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
            v-if="serie.SeriesDescription && serie.SeriesDescription.Value"
            class="pointer word-break"
          >
            {{ serie.SeriesDescription.Value[0] }}
          </span>
          <span
            v-else
            class="pointer"
          >
            No description
          </span>
        </b-form-checkbox>
      </div>
    </div>

    <div class="row justify-content-center">
      <div class="mb-2 preview">
        <div
          class="d-flex flex-row justify-content-center align-items-center full-height"
        >
          <div class="p-2">
            <img
              v-if="!loadingImage"
              :class="!serie.Modality.Value[0].includes('SR') ? 'pointer' : ''"
              :src="serie.imgSrc"
              width="250"
              height="250"
              @click="openTab(serie)"
            >
            <bounce-loader
              :loading="loadingImage"
              color="white"
            />
          </div>
        </div>
      </div>
      <div class="col col-mb-2 col-sm-10 col-md-8 col-lg-6 description">
        <table class="table table-striped-color-reverse table-nohover">
          <tbody>
            <tr v-if="serie.Modality && serie.Modality.Value !== undefined">
              <th>{{ $t('modality') }}</th>
              <td>{{ serie.Modality.Value[0] }}</td>
            </tr>
            <tr v-if="serie.RetrieveAETitle && serie.RetrieveAETitle.Value !== undefined">
              <th>{{ $t('applicationentity') }}</th>
              <td>{{ serie.RetrieveAETitle.Value[0] }}</td>
            </tr>
            <tr v-if="serie.NumberOfSeriesRelatedInstances && serie.NumberOfSeriesRelatedInstances.Value !== undefined">
              <th>{{ $t('numberimages') }}</th>
              <td>{{ serie.NumberOfSeriesRelatedInstances.Value[0] }}</td>
            </tr>
            <tr v-if="serie.SeriesDescription && serie.SeriesDescription.Value !== undefined">
              <th>{{ $t('description') }}</th>
              <td
                class="word-break"
              >{{ serie.SeriesDescription.Value[0] }}</td>
            </tr>
            <tr v-if="serie.SeriesDate && serie.SeriesDate.Value !== undefined">
              <th>{{ $t('seriesdate') }}</th>
              <td>{{ serie.SeriesDate.Value[0] | formatDate }}</td>
            </tr>
            <tr v-if="serie.SeriesTime && serie.SeriesTime.Value !== undefined">
              <th>{{ $t('seriestime') }}</th>
              <td>{{ serie.SeriesTime.Value[0] | formatTM }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
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
      let openWSI = this.study.ModalitiesInStudy !== undefined
        && this.study.ModalitiesInStudy.Value.length === 1
        && this.study.ModalitiesInStudy.Value[0] === 'SM';
      let windowProps = {}
      if (series.SOPClassUID !== undefined && (series.SOPClassUID.Value[0] === SOPPdf || series.SOPClassUID.Value[0] === SOPVideo)) {
        windowProps['name'] = `WADO-${this.seriesInstanceUID}`;
        windowProps['id'] = 'WADO'
      } else if (openWSI) {
        windowProps['name'] = `WSI-${this.studyInstanceUID}`;
        windowProps['id'] = 'WSI'
      } else if (series.Modality.Value[0] !== 'SR') {
        windowProps['name'] = `OHIF-${this.studyInstanceUID}`;
        windowProps['id'] = 'OHIF'
      }
      let openWindow = window.open('', windowProps.name)
      this.getViewerToken(this.currentuserAccessToken, this.studyInstanceUID, this.source).then((res) => {
        const viewerToken = res.data.access_token;
        const sourceQuery = this.getSourceQueries();
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
