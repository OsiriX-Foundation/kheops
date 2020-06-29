<template>
  <span>
    <div
      v-if="comment.mutation_type === 'IMPORT_SERIES'"
      class=" flex-grow-1 bd-highlight"
    >
      <span
        v-if="comment.source.report_provider !== undefined"
      >
        {{ $t('comment.newreport', {user: getName(comment.source), reportname: comment.source.report_provider.name, serie: comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID, study: comment.study.description ? comment.study.description : comment.study.UID}) }}
      </span>
      <span
        v-else-if="comment.source.capability_token !== undefined"
      >
        <div
          class="text-warning"
        >
          {{ $t('comment.bycapabilitytoken') }}
        </div>
        {{ $t('comment.importserietoken', {user: getName(comment.source), serie: comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID, study: comment.study.description ? comment.study.description : comment.study.UID, title: comment.source.capability_token.title}) }}
      </span>
      <span
        v-else
      >
        <i>{{ comment.source|getUsername }}</i> {{ $t('comment.imported') }} {{ $t('comment.theseries') }} <b>{{ comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID }}</b> {{ $t('comment.in') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
      </span>
      <span
        v-if="comment.study.series[0].is_present_in_album === false"
        class="text-warning"
      >
        <br>{{ $t('comment.serienotpresentinalbum') }}
      </span>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_SERIES'"
      class=" flex-grow-1 bd-highlight"
    >
      <span
        v-if="comment.source.report_provider !== undefined"
      >
        {{ $t('comment.removereport', {user: getName(comment.source), reportname: comment.source.report_provider.name, serie: comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID, study: comment.study.description ? comment.study.description : comment.study.UID}) }}
      </span>
      <span
        v-else-if="comment.source.capability_token !== undefined"
      >
        <div
          class="text-warning"
        >
          {{ $t('comment.bycapabilitytoken') }}
        </div>
        {{ $t('comment.removeserietoken', {user: getName(comment.source), serie: comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID, study: comment.study.description ? comment.study.description : comment.study.UID, title: comment.source.capability_token.title}) }}
      </span>
      <span
        v-else
      >
        <i>{{ comment.source|getUsername }}</i> {{ $t('comment.removed') }} {{ $t('comment.theseries') }} <b>{{ comment.study.series[0].description ? comment.study.series[0].description : comment.study.series[0].UID }}</b> {{ $t('comment.in') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
      </span>
      <span
        v-if="comment.study.series[0].is_present_in_album === true"
        class="text-warning"
      >
        <br>{{ $t('comment.seriepresentinalbum') }}
      </span>
    </div>
  </span>
</template>
<script>
export default {
  name: 'NotificationsSeries',
  props: {
    comment: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  methods: {
    getName(user) {
      return this.$options.filters.getUsername(user);
    },
  },
};
</script>
