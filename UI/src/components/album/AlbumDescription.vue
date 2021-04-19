<template>
  <div
    class="card-body album-description"
  >
    <p
      v-for="line in description.split('\n')"
      :key="line.id"
      class="pl-3 py-0 my-0"
    >
      {{ line }}
    </p>
  </div>
</template>

<script>
export default {
  name: 'AlbumDescription',
  props: {
    description: {
      type: String,
      required: true,
      default: '',
    },
  },
};
</script>
