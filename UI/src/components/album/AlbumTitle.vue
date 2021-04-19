<template>
  <h3
    class="album-title word-break"
  >
    <v-icon
      name="book"
      scale="2"
    />
    <span class="p-2">
      {{ title }}
    </span>

    <slot />
  </h3>
</template>

<script>
export default {
  name: 'AlbumTitle',
  props: {
    title: {
      type: String,
      required: true,
      default: '',
    },
  },
};
</script>
