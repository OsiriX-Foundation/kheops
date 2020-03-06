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
      v-if="showNavHeader"
      :logged="oidcIsAuthenticated"
    />
    <nav-bar
      v-if="oidcIsAuthenticated"
    />

    <!-- content -->
    <router-view
      :style="oidcIsAuthenticated ? 'margin: 25px auto' : 'margin: 75px auto'"
    />
    <send-studies />
    <!-- footer -->
    <footer />
  </div>
</template>

<script>

import { mapGetters } from 'vuex';
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
    ...mapGetters('oidcStore', [
      'oidcIsAuthenticated',
    ]),
    year() {
      return new Date().getFullYear();
    },
    showNavHeader() {
      return this.$route.meta.header === undefined ? true : this.$route.meta.header;
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
