<template>
  <div>
    <loading
      class="mt-5"
    />
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'OidcCallback',
  components: { Loading },
  computed: {
    ...mapGetters('oidcStore', [
      'oidcIsAuthenticated',
    ]),
  },
  mounted() {
    this.oidcSignInCallback()
      .then((redirectPath) => {
        this.$router.push(redirectPath);
      })
      .catch(() => {
        if (!this.oidcIsAuthenticated) {
          this.$router.push('/oidc-callback-error'); // Handle errors any way you want
        } else {
          this.$router.push('/');
        }
      });
  },
  methods: {
    ...mapActions('oidcStore', [
      'oidcSignInCallback',
    ]),
  },
};
</script>
