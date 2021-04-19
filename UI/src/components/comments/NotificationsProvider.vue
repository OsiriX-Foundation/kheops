<template>
  <span>
    <div
      v-if="comment.mutation_type === 'CREATE_REPORT_PROVIDER' && comment.report_provider"
      class=" flex-grow-1 bd-highlight"
    >
      {{ $t('comment.createreportprovider', {user: getName(comment.source), reportname: comment.report_provider.name}) }}
    </div>
    <div
      v-if="comment.mutation_type === 'EDIT_REPORT_PROVIDER' && comment.report_provider"
      class=" flex-grow-1 bd-highlight"
    >
      {{ $t('comment.editreportprovider', {user: getName(comment.source), reportname: comment.report_provider.name}) }}
    </div>
    <div
      v-if="comment.mutation_type === 'DELETE_REPORT_PROVIDER' && comment.report_provider"
      class=" flex-grow-1 bd-highlight"
    >
      {{ $t('comment.deletereportprovider', {user: getName(comment.source), reportname: comment.report_provider.name}) }}
    </div>
  </span>
</template>
<script>
export default {
  name: 'NotificationsProvider',
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
