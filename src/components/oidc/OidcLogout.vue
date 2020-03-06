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
    this.signOutOidc();
  },
  methods: {
    ...mapActions('oidcStore', [
      'signOutOidc',
    ]),
  },
};
</script>
