<template>
  <span>
    <span
      v-if="status === 401 || status === 403"
    >
      {{ $t('texterror.unauthorized') }}
    </span>
    <span
      v-if="status === 404"
    >
      {{ $t('texterror.notfound') }}
    </span>
    <span
      v-else
    >
      {{ $t('texterror.unknownerror') }}
    </span>
  </span>
</template>
<script>
export default {
  name: 'TextError',
  props: {
    status: {
      type: Number,
      required: true,
      default: 0,
    },
  },
  methods: {
  },
};
</script>
