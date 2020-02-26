<i18n>
{
  "en": {
    "fieldobligatory": "Field obligatory"
  },
  "fr": {
    "fieldobligatory": "Champs obligatoire"
  }
}
</i18n>

<template>
  <b-form-invalid-feedback :state="state">
    <span
      v-if="text !== ''"
    >
      {{ text }}
    </span>
    <span
      v-else
    >
      {{ $t('fieldobligatory') }}
    </span>
  </b-form-invalid-feedback>
</template>
<script>
export default {
  name: 'FieldObligatory',
  props: {
    state: {
      type: Boolean,
      required: true,
      default: false,
    },
    text: {
      type: String,
      required: false,
      default: '',
    },
  },
};
</script>
