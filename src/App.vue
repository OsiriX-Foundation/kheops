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
      document.title = to.meta.title === undefined ? this.appTitle : this.$t(`doctitle.${to.meta.title}`, { appTitle: this.appTitle });
    },
  },
  created() {
    document.title = this.$route.meta.title === undefined ? this.appTitle : this.$t(`doctitle.${this.$route.meta.title}`, { appTitle: this.appTitle });
  },
};
</script>
