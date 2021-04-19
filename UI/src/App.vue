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
      v-if="userSend === true || oidcIsAuthenticated === false"
      :style="oidcIsAuthenticated ? 'margin: 25px auto' : 'margin: 75px auto'"
    />
    <loading
      v-else
    />
    <send-studies />
    <!-- footer -->
    <footer />
  </div>
</template>

<script>

import { mapGetters, mapActions } from 'vuex';
import navHeader from '@/components/navheader';
import navBar from '@/components/navbar';
import SendStudies from '@/components/study/SendStudies';
import { CurrentUser } from '@/mixins/currentuser.js';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'App',
  components: {
    navHeader,
    navBar,
    SendStudies,
    Loading,
  },
  mixins: [CurrentUser],
  data() {
    return {
      appTitle: 'KHEOPS',
      userSend: false,
    };
  },
  computed: {
    ...mapGetters('oidcStore', [
      'oidcIsAuthenticated',
      'oidcAccessToken',
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
      document.title = to.meta.title === undefined ? this.appTitle : this.$t(`doctitle.${to.meta.title}`, { appTitle: this.appTitle });
    },
    oidcAccessToken() {
      this.accessTokenLoaded(this.oidcAccessToken);
    },
  },
  mounted() {
    window.addEventListener('vuexoidc:userSignedOut', this.userSignOut);
  },
  created() {
    document.title = this.$route.meta.title === undefined ? this.appTitle : this.$t(`doctitle.${this.$route.meta.title}`, { appTitle: this.appTitle });
  },
  destroyed() {
    window.removeEventListener('vuexoidc:userSignedOut', this.userSignOut);
  },
  methods: {
    ...mapActions('oidcStore', [
      'authenticateOidc',
    ]),
    userSignOut() {
      this.authenticateOidc();
    },
    accessTokenLoaded(accessToken) {
      if (this.userSend === false) {
        if (accessToken !== null) {
          this.postAccessToken(accessToken).then((res) => {
            if (res.status === 200) {
              this.userSend = true;
            }
          }).catch(() => {
            this.userSend = true;
          });
        }
      }
    },
  },
};
</script>
