<i18n>
  {
    "en": {
      "unauthorized": "You don't have the permissions",
      "unknownerror": "An unknown error occur",
      "notfound": "Not found"

    },
    "fr": {
      "unauthorized": "Vous n'avez pas les permissions",
      "unknownerror": "Une erreur inconnue est survenue",
      "notfound": "Pas trouvé"
    }
  }
</i18n>
<template>
  <span>
    <span
      v-if="status === 401 || status === 403"
    >
      {{ $t('unauthorized') }}
    </span>
    <span
      v-if="status === 404"
    >
      {{ $t('notfound') }}
    </span>
    <span
      v-else
    >
      {{ $t('unknownerror') }}
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
