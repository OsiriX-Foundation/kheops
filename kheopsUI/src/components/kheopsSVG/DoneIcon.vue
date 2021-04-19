<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    :width="width"
    :height="height"
    class="kheopsicon-success"
    viewBox="0 0 24 24"
  >
    <path
      fill="none"
      d="M0 0h24v24H0z"
    />
    <path
      d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"
    />
  </svg>
</template>

<script>

export default {
  name: 'DoneIcon',
  props: {
    width: {
      type: String,
      required: true,
      default: '0px',
    },
    height: {
      type: String,
      required: true,
      default: '0px',
    },
  },
};
</script>
