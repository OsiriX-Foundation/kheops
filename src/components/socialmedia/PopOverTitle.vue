<template>
  <div
    class="d-flex"
  >
    <div
      class="mt-1"
    >
      <b>{{ title }}</b>
    </div>
    <div
      class="ml-auto"
    >
      <a
        @click="cancel()"
      >
        <close-icon
          width="20"
          height="20"
        />
      </a>
    </div>
  </div>
</template>
<script>
import CloseIcon from '@/components/kheopsSVG/CloseIcon';

export default {
  name: 'PopOverTitle',
  components: { CloseIcon },
  props: {
    title: {
      type: String,
      required: true,
      default: '',
    },
  },
  methods: {
    cancel() {
      this.$emit('cancel');
    },
  },
};
</script>
