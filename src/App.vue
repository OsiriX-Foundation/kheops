<i18n>
{
  "en": {
    "albums": "Kheops - Albums",
    "album": "Kheops - Album",
    "newalbum": "Kheops - New album",
    "user": "Kheops - User"
  },
  "fr": {
    "albums": "Kheops - Albums",
    "album": "Kheops - Album",
    "newalbum": "Kheops - Nouvel album",
    "user": "Kheops - Utilisateur"
  }
}
</i18n>
<template>
  <div id="app">
    <vue-snotify />
    <nav-header
      :logged="logged"
    />
    <nav-bar
      v-if="logged"
    />

    <!-- content -->
    <router-view
      :style="logged ? 'margin: 25px auto' : 'margin: 75px auto'"
    />

    <send-studies />
    <!-- footer -->
    <footer />
  </div>
</template>

<script>

import navHeader from '@/components/navheader';
import navBar from '@/components/navbar';
import SendStudies from '@/components/study/SendStudies';

export default {
  name: 'App',
  components: { navHeader, navBar, SendStudies },
  data() {
    return {
      appTitle: 'Kheops',
    };
  },
  computed: {
    year() {
      return new Date().getFullYear();
    },
    logged() {
      return this.$keycloak.authenticated;
    },
  },
  watch: {
    $route(to) {
      document.title = this.$t(to.meta.title) || this.appTitle;
    },
  },
  created() {
    document.title = 'Kheops';
  },
};
</script>
