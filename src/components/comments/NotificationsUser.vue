<template>
  <span>
    <div
      v-if="comment.mutation_type === 'ADD_USER'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.hasadd') }} {{ $t('comment.theuser') }} <i>{{ comment.target|getUsername }}</i>
    </div>
    <div
      v-if="comment.mutation_type === 'ADD_ADMIN'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.hasadd') }} {{ $t('comment.theadmin') }} <i>{{ comment.target|getUsername }}</i>
    </div>
    <div
      v-if="comment.mutation_type === 'REMOVE_USER'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.removed') }} {{ $t('comment.theuser') }} <i>{{ comment.target|getUsername }}</i>
    </div>
    <div
      v-if="comment.mutation_type === 'PROMOTE_ADMIN'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.hasgranted') }} {{ $t('comment.adminrights') }} {{ $t('comment.to') }} <i>{{ comment.target|getUsername }}</i>
    </div>
    <div
      v-if="comment.mutation_type === 'DEMOTE_ADMIN'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.hasremoved') }} {{ $t('comment.adminrights') }} {{ $t('comment.to') }} <i>{{ comment.target|getUsername }}</i>
    </div>
    <div
      v-if="comment.mutation_type === 'LEAVE_ALBUM'"
      class=" flex-grow-1 bd-highlight"
    >
      <i>{{ comment.source|getUsername }}</i> {{ $t('comment.hasleft') }}
    </div>
  </span>
</template>
<script>
export default {
  name: 'NotificationsUser',
  props: {
    comment: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
};
</script>
