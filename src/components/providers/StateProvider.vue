<i18n>
{
  "en": {
    "urlWorking": "This provider is on",
    "urlNotWorking": "This provider is off"
  },
  "fr": {
    "urlWorking": "This provider is on",
    "urlNotWorking": "This provider is off"
  }
}
</i18n>

<template>
  <span
    v-if="loading"
    class="nowrap"
    :title="$t('urlWorking')"
  >
    <clip-loader
      :loading="loading"
      :size="'20px'"
      :color="'white'"
      :class="classIcon"
    />
  </span>
  <span
    v-else-if="checkURL"
    class="nowrap"
    :title="$t('urlWorking')"
  >
    <v-icon
      name="check-circle"
      :class="classIcon"
      :color="'#41B883'"
    />
  </span>
  <span
    v-else
    class="nowrap"
    :title="$t('urlNotWorking')"
  >
    <v-icon
      name="ban"
      :class="classIcon"
      :color="'red'"
    />
  </span>
</template>

<script>
import ClipLoader from 'vue-spinner/src/ClipLoader.vue';

export default {
  name: 'StateProvider',
  components: { ClipLoader },
  props: {
    loading: {
      type: Boolean,
      required: true,
      default: false,
    },
    checkURL: {
      type: Boolean,
      required: true,
      default: false,
    },
    classIcon: {
      type: String,
      requried: false,
      default: '',
    },
  },
};
</script>
