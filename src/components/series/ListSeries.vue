<i18n>
{
  "en": {
    "errorSeries": "An error occured, please reload the series list",
    "reload": "Reload"
  },
  "fr": {
    "errorSeries": "Une erreur est survenue, veuillez recharger les séries.",
    "reload": "Recharger"
  }
}
</i18n>
<template>
  <span>
    <div
      v-if="loadingSerie === false && errorSeries === false"
      class="row"
    >
      <div
        v-for="serie in series[studyUID]"
        :key="serie.id"
        class="col-12 col-sm-6 col-md-4 col-lg-3 col-xl-2 mb-5"
      >
        <series-summary
          :serie="serie"
          :study="study"
          :source="source"
        />
      </div>
    </div>
    <pulse-loader
      :loading="loadingSerie"
      color="white"
    />
    <div
      v-if="loadingSerie === false && errorSeries === true"
    >
      <div
        class="d-flex flex-column justify-content-center align-items-center full-height"
      >
        <div class="mb-3">
          {{ $t('errorSeries') }}
        </div>
        <div class="">
          <button
            type="button"
            class=" btn btn-md"
            @click="getSeries()"
          >
            {{ $t('reload') }}
          </button>
        </div>
      </div>
    </div>
  </span>
</template>
<script>
import { mapGetters } from 'vuex';
import PulseLoader from 'vue-spinner/src/PulseLoader.vue';
import SeriesSummary from '@/components/series/SeriesSummary';

export default {
  name: 'ListSeries',
  components: {
    PulseLoader, SeriesSummary,
  },
  props: {
    study: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {
      loadingSerie: true,
      errorSeries: false,
      includefield: ['00080021', '00080031'],
    };
  },
  computed: {
    ...mapGetters({
      series: 'series',
      sendingFiles: 'sending',
    }),
    studyUID() {
      return this.study.StudyInstanceUID.Value[0];
    },
  },
  watch: {
    sendingFiles() {
      if (!this.sendingFiles) {
        this.getSeries();
      }
    },
  },
  created() {
    this.getSeries();
  },
  methods: {
    getSeries() {
      const params = {
        StudyInstanceUID: this.studyUID,
        studySelected: this.study.flag.is_selected,
        queries: {},
      };
      if (Object.keys(this.source).length > 0) {
        params.queries[this.source.key] = this.source.value;
      }
      params.queries.includefield = this.includefield;
      this.loadingSerie = true;
      this.$store.dispatch('getSeries', params).then((res) => {
        if (res.status === 200) {
          this.loadingSerie = false;
          this.errorSeries = false;
        }
      }).catch(() => {
        this.loadingSerie = false;
        this.errorSeries = true;
      });
    },
  },
};
</script>
