<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    :width="width"
    :height="height"
    class="kheopsicon"
    viewBox="0 0 24 24"
  >
    <path
      d="M19 13H5v-2h14v2z"
    />
    <path
      d="M0 0h24v24H0z"
      fill="none"
    />
  </svg>
</template>

<script>

export default {
  name: 'RemoveIcon',
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
