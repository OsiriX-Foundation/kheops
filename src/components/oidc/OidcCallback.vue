<template>
  <div>
    <pulse-loader />
  </div>
</template>

<script>
import { mapActions } from 'vuex';
import PulseLoader from 'vue-spinner/src/PulseLoader.vue';

export default {
  name: 'OidcCallback',
  components: { PulseLoader },
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
