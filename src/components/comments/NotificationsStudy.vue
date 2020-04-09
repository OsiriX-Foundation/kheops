<template>
  <span>
    <div
      v-if="comment.mutation_type === 'IMPORT_STUDY'"
      class="flex-grow-1 bd-highlight"
    >
      <i>{{ comment.origin|getUsername }}</i> {{ $t('comment.imported') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_STUDY'"
      class="flex-grow-1 bd-highlight"
    >
      <i>{{ comment.origin|getUsername }}</i> {{ $t('comment.removed') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
    <div
      v-if="comment.mutation_type === 'ADD_FAV' && comment.study"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.origin|getUsername }}</i> {{ $t('comment.addfavorite') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_FAV' && comment.study"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.origin|getUsername }}</i> {{ $t('comment.removefavorite') }} {{ $t('comment.thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
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
};
</script>
