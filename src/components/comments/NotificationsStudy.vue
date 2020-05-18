<template>
  <span>
    <div
      v-if="comment.mutation_type === 'IMPORT_STUDY'"
      class="flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.imported') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
      <span
        v-if="lengthNotPresent(comment.study.series) === comment.study.series.length"
        class="text-warning"
      >
        <br>{{ $t('comment.nomoreseriesinstudy', {study: comment.study.description ? comment.study.description : comment.study.UID}) }}
      </span>
      <span
        v-else
      >
        <span
          v-for="serie in isNotPresent(comment.study.series)"
          :key="serie.id"
          class="text-warning"
        >
          <br>{{ $t('comment.theserienotpresentinalbum', {serie: comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID}) }}
        </span>
      </span>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_STUDY'"
      class="flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.removed') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
      <span
        v-if="lengthPresent(comment.study.series) === comment.study.series.length"
        class="text-warning"
      >
        <br>{{ $t('comment.allseriesinstudy', {study: comment.study.description ? comment.study.description : comment.study.UID}) }}
      </span>
      <span
        v-else
      >
        <span
          v-for="serie in isPresent(comment.study.series)"
          :key="serie.id"
          class="text-warning"
        >
          <br>{{ $t('comment.theseriepresentinalbum', {serie: comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID}) }}
        </span>
      </span>
    </div>
    <div
      v-if="comment.mutation_type === 'ADD_FAV' && comment.study"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.addfavorite') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_FAV' && comment.study"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.removefavorite') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
  </span>
</template>
<script>
export default {
  name: 'NotificationsStudy',
  props: {
    comment: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  methods: {
    lengthNotPresent(series) {
      const seriesNotPresent = this.isNotPresent(series);
      return seriesNotPresent.length;
    },
    isNotPresent(series) {
      return series.filter((serie) => serie.is_present_in_album === false);
    },
    lengthPresent(series) {
      const seriesPresent = this.isPresent(series);
      return seriesPresent.length;
    },
    isPresent(series) {
      return series.filter((serie) => serie.is_present_in_album === true);
    },
  },
};
</script>
