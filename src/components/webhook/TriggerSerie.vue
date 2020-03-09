<i18n>
{
  "en": {
    "informationtrigger": "The below series will be delivered to {url} using the current webhook configuraton.",
    "errortrigger": "You can't redeliver this trigger.",
    "warningtrigger": "Please verifiy if you have always access at the studies or the series from this album.",
    "errorseries": "The following series can't be loaded :",
    "seriepresent": "The following series are always present in your album."
  },
  "fr": {
    "informationtrigger": "Les séries ci-dessous seront livrées à {url} en utilisant la configuration actuelle.",
    "errortrigger": "Vous ne pouvez pas rédéclencher cet évènement.",
    "warningtrigger": "Veuillez vérifier si vous avez toujours accès à l'étude ou aux séries depuis cet album.",
    "errorseries": "Les séries suivant ne peux pas être chargée :",
    "seriepresent": "Les séries suivantes sont toujours présentes dans l'album."
  }
}
</i18n>
<template>
  <span>
    <p
      v-if="errorSerie === false"
      class="word-break"
    >
      {{ $t('informationtrigger', { url: url }) }}
    </p>
    <span
      v-if="errorSerie === true"
    >
      <div
        class="text-warning mb-2"
      >
        {{ $t('errortrigger') }}
      </div>
      <div
        class="mb-2"
      >
        {{ $t('warningtrigger') }}
      </div>
      <div
        class="text-warning mb-2"
      >
        {{ $t('errorseries') }}
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
        {{ $t('seriepresent') }}
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
