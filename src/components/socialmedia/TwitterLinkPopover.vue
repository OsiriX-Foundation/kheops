<i18n scoped>
{
  "en": {
    "cancel": "Cancel",
    "revoke": "Do you want disable the latest sharing link created ?",
    "valid": "Valid",
    "title": "Twitter link",
    "disable": "Revoke"
  },
  "fr": {
    "cancel": "Annuler",
    "revoke": "Voulez-vous désactiver le dernier lien de partage créé ?",
    "valid": "Valider",
    "title": "Lien twitter",
    "disable": "Désactiver"
  }
}
</i18n>
<template>
  <span>
    <div>
      <div class="row mt-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <label>
            {{ $t('revoke') }}
          </label>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            type="submit"
            class="btn btn-danger btn-block"
            @click="revoke"
          >
            {{ $t('disable') }}
          </button>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            type="reset"
            class="btn btn-secondary btn-block"
            @click="cancel"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </div>
  </span>
</template>
<script>
export default {
  name: 'TwitterLinkPopover',
  props: {
    tokens: {
      type: Array,
      required: true,
      default: () => [],
    },
  },
  data() {
    return {
      toggle_revoke: false,
    };
  },
  methods: {
    revoke() {
      this.$emit('revoke', this.tokens);
    },
    cancel() {
      this.$emit('cancel');
    },
  },
};
</script>
