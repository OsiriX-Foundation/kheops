<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    :width="width"
    :height="height"
    class="icon"
    viewBox="0 0 24 24"
  >
    <path
      d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"
    />
    <path
      d="M0 0h24v24H0z"
      fill="none"
    />
  </svg>
</template>

<script>

export default {
  name: 'AddIcon',
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
<style scoped>
  .icon{
    fill: white;
  }

  .icon:hover{
    fill: grey;
  }
</style>
