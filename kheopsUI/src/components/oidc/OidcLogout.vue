<template>
  <div>
    <loading
      class="mt-5"
    />
  </div>
</template>

<script>
import { mapActions } from 'vuex';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'OidcLogout',
  components: { Loading },
  mounted() {
    // eslint-disable-next-line camelcase
    const post_logout_redirect_uri = this.$route.params.redirect !== undefined ? this.$route.params.redirect : process.env.VUE_APP_URL_ROOT;
    this.signOutOidc({ post_logout_redirect_uri });
  },
  methods: {
    ...mapActions('oidcStore', [
      'signOutOidc',
    ]),
  },
};
</script>
