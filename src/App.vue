<i18n>
{
  "en": {
    "albums": "{appTitle} - Albums",
    "album": "{appTitle} - Album",
    "newalbum": "{appTitle} - New album",
    "user": "{appTitle} - User"
  },
  "fr": {
    "albums": "{appTitle} - Albums",
    "album": "{appTitle} - Album",
    "newalbum": "{appTitle} - Nouvel album",
    "user": "{appTitle} - Utilisateur"
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
      appTitle: 'KHEOPS',
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
      document.title = this.$t(to.meta.title, { appTitle: this.appTitle }) || this.appTitle;
    },
  },
  created() {
    document.title = this.$t(this.$route.meta.title, { appTitle: this.appTitle }) || this.appTitle;
  },
};
</script>
