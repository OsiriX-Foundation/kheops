<template>
  <span>
    <div
      v-if="comment.mutation_type === 'CREATE_WEBHOOK'"
      class=" flex-grow-1 bd-highlight"
    >
      {{ $t('comment.createwebhook', {user: getName(comment.source)}) }}
    </div>

    <div
      v-if="comment.mutation_type === 'DELETE_WEBHOOK'"
      class=" flex-grow-1 bd-highlight"
    >
      {{ $t('comment.deletewebhook', {user: getName(comment.source)}) }}
    </div>

    <div
      v-if="comment.mutation_type === 'EDIT_WEBHOOK'"
      class=" flex-grow-1 bd-highlight"
    >
      {{ $t('comment.editwebhook', {user: getName(comment.source)}) }}
    </div>

    <div
      v-if="comment.mutation_type === 'TRIGGER_WEBHOOK'"
      class=" flex-grow-1 bd-highlight"
    >
      {{ $t('comment.triggerwebhook', {user: getName(comment.source)}) }}
    </div>
  </span>
</template>
<script>
export default {
  name: 'NotificationsWebhook',
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
