<template>
  <span>
    <done-icon
      v-if="done === true"
      :height="height"
      :width="width"
    />
    <v-icon
      v-if="warning === true"
      name="warning"
      :height="height"
      :width="width"
      color="orange"
    />
    <error-icon
      v-if="error === true"
      :height="height"
      :width="width"
      color="red"
    />
  </span>
</template>
<script>
import ErrorIcon from '@/components/kheopsSVG/ErrorIcon.vue';
import DoneIcon from '@/components/kheopsSVG/DoneIcon';

export default {
  name: 'StateIcons',
  components: {
    ErrorIcon, DoneIcon,
  },
  props: {
    done: {
      type: Boolean,
      required: true,
      default: false,
    },
    error: {
      type: Boolean,
      required: true,
      default: false,
    },
    warning: {
      type: Boolean,
      required: true,
      default: false,
    },
    height: {
      type: String,
      required: false,
      default: '20',
    },
    width: {
      type: String,
      required: false,
      default: '20',
    },
  },
};
</script>
