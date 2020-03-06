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
  name: 'OidcCallback',
  components: { Loading },
  mounted() {
    this.oidcSignInCallback()
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
      'oidcSignInCallback',
    ]),
  },
};
</script>
