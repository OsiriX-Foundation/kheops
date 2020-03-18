<template>
  <span>
    <div
      v-if="comment.mutation_type === 'IMPORT_SERIES'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.origin|getUsername }}</i> {{ $t('comment.imported') }} {{ $t('comment.theseries') }} <b>{{ comment.series.description ? comment.series.description : comment.series.UID }}</b> {{ $t('comment.in') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_SERIES'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.origin|getUsername }}</i> {{ $t('comment.removed') }} {{ $t('comment.theseries') }} <b>{{ comment.series.description ? comment.series.description : comment.series.UID }}</b> {{ $t('comment.in') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
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
};
</script>
