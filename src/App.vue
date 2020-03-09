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
import { CurrentUser } from '@/mixins/currentuser.js';

export default {
  name: 'App',
  components: { navHeader, navBar, SendStudies },
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
      document.title = this.$t(to.meta.title, { appTitle: this.appTitle }) || this.appTitle;
    },
    oidcAccessToken() {
      this.accessTokenLoaded(this.oidcAccessToken);
    },
  },
  mounted() {
    window.addEventListener('vuexoidc:userSignedOut', this.userSignOut);
  },
  created() {
    document.title = this.$t(this.$route.meta.title, { appTitle: this.appTitle }) || this.appTitle;
  },
  destroyed() {
    window.removeEventListener('vuexoidc:userSignedOut', this.userSignOut);
  },
  methods: {
    userSignOut() {
      const redirect = `${process.env.VUE_APP_URL_ROOT}${this.$route.path}`;
      const signOut = { path: '/oidc-logout', name: 'oidcLogout', params: { redirect } };
      this.$router.push(signOut);
    },
    accessTokenLoaded(accessToken) {
      if (this.userSend === false) {
        if (accessToken !== null) {
          this.postAccessToken(accessToken).then((res) => {
            if (res.status === 200) {
              this.userSend = true;
            }
          }).catch((err) => {
            console.log(err);
          });
        }
      }
    },
  },
};
</script>
