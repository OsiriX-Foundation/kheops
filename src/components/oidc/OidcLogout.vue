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
    this.signOutOidc({
      post_logout_redirect_uri: 'http://localhost:8080',
    })
      .then((redirectPath) => {
        this.$router.push(redirectPath);
      })
      .catch((err) => {
        console.error(err);
        this.$router.push('/oidc-callback-error'); // Handle errors any way you want
      });
  },
  methods: {
    ...mapActions('oidcStore', [
      'signOutOidc',
    ]),
  },
};
</script>
