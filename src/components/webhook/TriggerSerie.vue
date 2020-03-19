<template>
  <span>
    <p
      v-if="errorSerie === false"
      class="word-break"
    >
      {{ $t('webhook.informationtrigger', { url: url }) }}
    </p>
    <span
      v-if="errorSerie === true"
    >
      <div
        class="text-warning mb-2"
      >
        {{ $t('webhook.errortrigger') }}
      </div>
      <div
        class="mb-2"
      >
        {{ $t('webhook.warningtrigger') }}
      </div>
      <div
        class="text-warning mb-2"
      >
        {{ $t('webhook.errorseries') }}
      </div>
      <ul
        class="mt-2"
      >
        <li
          v-for="errorSerieUID in errorSerieUIDS"
          :key="errorSerieUID.key"
          class="text-warning"
        >
          {{ errorSerieUID }}
        </li>
      </ul>
      <p
        v-if="errorSerieUIDS.length !== seriesUIDS.length"
      >
        {{ $t('webhook.seriepresent') }}
      </p>
    </span>
    <details-series
      v-if="trigger.event === 'new_series'"
      class-col="col-4 col-sm-3 col-md-4 col-lg-3 col-xl-2 mb-2 mt-2"
      class-row="serie-section card-main mb-3"
      height="75"
      width="75"
      :serie-uids="seriesUIDS"
      :study-uid="trigger.study.study_uid"
      :source="source"
      @missingseries="missingSeries"
    />
  </span>
</template>
<script>
import DetailsSeries from '@/components/series/DetailsSeries';

export default {
  name: 'TriggerSerie',
  components: { DetailsSeries },
  props: {
    trigger: {
      type: Object,
      required: true,
      default: () => {},
    },
    url: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      errorSerie: false,
      errorSerieUIDS: [],
    };
  },
  computed: {
    seriesUIDS() {
      if (this.trigger.event === 'new_series' && this.trigger.study !== undefined && this.trigger.study.series !== undefined) {
        return this.trigger.study.series.map((serie) => serie.series_uid);
      }
      return [];
    },
    source() {
      return {
        key: 'album',
        value: this.$route.params.album_id,
      };
    },
  },
  methods: {
    missingSeries(series) {
      this.errorSerie = true;
      this.errorSerieUIDS = series;
      this.$emit('missingseries');
    },
  },
};
</script>
