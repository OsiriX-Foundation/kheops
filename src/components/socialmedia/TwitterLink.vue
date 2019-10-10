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
    <div
      class="closeBtn d-flex"
    >
      <div
        class="p-2"
      >
        <b>{{ $t('title') }}</b>
      </div>
      <div
        class="ml-auto p-1"
      >
        <button
          type="button"
          class="btn btn-link btn-sm"
          @click="cancel()"
        >
          <close-icon
            width="20"
            height="20"
          />
        </button>
      </div>
    </div>
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
import CloseIcon from '@/components/kheopsSVG/CloseIcon';

export default {
  name: 'TwitterLink',
  components: { CloseIcon },
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
  computed: {
  },
  watch: {
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
    cancel() {
      this.$emit('cancel');
    },
    revoke() {
      this.$emit('revoke', this.tokens);
    },
  },
};
</script>
<style>
div.calendar-wrapper{
  color: #333;
}
.chat-popup {
  background: #303030;
  border: 3px solid #f1f1f1;
  z-index: 9;
  max-width: 550px;
  opacity: 1;
  display: block;
}
.closeBtn {
  position: relative;
  border-bottom: 1px solid #f1f1f1;
}
</style>
