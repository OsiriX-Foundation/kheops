<template>
  <nav class="nav nav-pills nav-fill flex-column flex-lg-row text-center justify-content-lg-end">
    <router-link
      :to="{ name: 'albumview', params: { view: 'studies' }}"
      class="nav-link"
      :class="(currentView === 'studies' || currentView === undefined)?'active':''"
      active-class="active"
    >
      {{ $t('album.studies') }}
    </router-link>
    <router-link
      :to="{ name: 'albumview', params: { view: 'comments' }}"
      class="nav-link"
      active-class="active"
    >
      {{ $t('album.comments') }}
    </router-link>
    <router-link
      :to="{ name: 'albumview', params: { view: 'settings' }}"
      class="nav-link"
      active-class="active"
    >
      {{ $t('album.settings') }}
    </router-link>
  </nav>
</template>

<script>
export default {
  name: 'AlbumMenu',
  computed: {
    currentView() {
      return this.$route.params.view;
    },
  },
};
</script>
