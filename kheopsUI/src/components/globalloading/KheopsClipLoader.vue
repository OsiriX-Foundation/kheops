<template>
  <clip-loader
    v-if="loading === true"
    :size="size"
    :color="color"
    :class="classLoader"
  />
</template>

<script>
import ClipLoader from 'vue-spinner/src/ClipLoader.vue';

export default {
  name: 'KheopsClipLoader',
  components: { ClipLoader },
  props: {
    loading: {
      type: Boolean,
      required: false,
      default: true,
    },
    size: {
      type: String,
      required: false,
      default: '40px',
    },
    color: {
      type: String,
      required: false,
      default: 'white',
    },
    classLoader: {
      type: String,
      required: false,
      default: '',
    },
  },
};
</script>
