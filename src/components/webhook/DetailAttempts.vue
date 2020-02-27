<i18n>
{
  "en": {
    "new_series": "new serie",
    "new_user": "new user",
    "activate": "Trigger by adding {event}",
    "manualtrigger": "Manual trigger",
    "attempt":"Attempt",
    "status": "Status",
    "date": "Date",
    "-1": "An error occur"
  },
  "fr": {
    "new_series": "nouvelles série",
    "new_user": "nouvel utilisateur",
    "activate": "Déclenché par l'ajout de {event}",
    "manualtrigger": "Déclenchement manuel",
    "attempt":"Tentative",
    "status": "Status",
    "date": "Date",
    "-1": "Une erreur est survenue"
  }
}
</i18n>
<template>
  <span>
    <p>{{ $t('activate', {event: $t(trigger.event)}) }}</p>
    <p
      v-if="trigger.is_manual_trigger === true"
    >
      {{ $t('manualtrigger') }}
    </p>
    <span
      class="d-flex flex-wrap flex-row bd-highlight mb-3"
    >
      <span
        v-for="attempt in trigger.attempts"
        :key="attempt.id"
        class="p-2 bd-highlight"
      >
        <b>{{ $t('attempt') }} {{ attempt.attempt }}</b> <br>
        {{ $t('status') }}: {{ $t(attempt.status) }}<br>
        {{ $t('date') }}: {{ attempt.time | formatDateTimeDetails }}<br>
        <br>
      </span>
    </span>
  </span>
</template>

<script>

export default {
  name: 'DetailAttempts',
  components: { },
  props: {
    trigger: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  data() {
    return {
    };
  },
  computed: {
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
  },
};
</script>
