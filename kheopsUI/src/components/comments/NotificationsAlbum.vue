<template>
  <span>
    <div
      v-if="comment.mutation_type === 'CREATE_ALBUM'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.hascreated') }} {{ $t('comment.thealbum') }}
    </div>

    <!-- EDIT_ALBUM -->
    <div
      v-if="comment.mutation_type === 'EDIT_ALBUM'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.hasedited') }} {{ $t('comment.thealbum') }}
    </div>
  </span>
</template>
<script>
export default {
  name: 'NotificationsAlbum',
  props: {
    comment: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
};
</script>
