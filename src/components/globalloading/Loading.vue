<template #table-busy>
  <div class="text-center text-danger my-2">
    <pulse-loader
      :color="color"
    />
  </div>
</template>
<script>
import PulseLoader from 'vue-spinner/src/PulseLoader.vue';

export default {
  name: 'ListTokens',
  components: { PulseLoader },
  props: {
    color: {
      type: String,
      required: false,
      default: 'white',
    },
  },
};
</script>
