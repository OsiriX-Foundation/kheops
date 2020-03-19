<template>
  <nav class="nav nav-pills nav-justified flex-column">
    <router-link
      v-for="(cat, idx) in categories"
      :key="idx"
      :to="{ name: 'albumsettings', params: { category: cat }}"
      class="nav-link"
      :class="(currentCategory === cat || (cat === 'general' && currentCategory === undefined)) ? 'active' : ''"
      active-class="active"
    >
      {{ $t(`albumsettingsmenu.${cat}`) }}
    </router-link>
  </nav>
</template>
<script>
export default {
  name: 'AlbumSettingsMenuNav',
  props: {
    categories: {
      type: Array,
      required: true,
      default: () => [],
    },
  },
  computed: {
    currentCategory() {
      return this.$route.params.category !== undefined ? this.$route.params.category : this.categories[0];
    },
  },
};
</script>
