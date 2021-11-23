<template>
  <div>
    <loading class="mt-5" />
  </div>
</template>

<script>
import { mapActions } from 'vuex';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'OidcInitiateLogin',
  components: { Loading },
  mounted() {
    const payload = {
      redirectPath: this.$route.query.target_link_uri,
      login_hint: this.$route.query.login_hint,
    };
    if (process.env.VUE_APP_AUTHROITY === this.$route.query.iss) {
      this.authenticateOidc(payload);
    } else {
      this.$snotify.error(this.$t('sorryerror'));
    }
  },
  methods: {
    ...mapActions('oidcStore', [
      'authenticateOidc',
    ]),
  },
};
</script>
