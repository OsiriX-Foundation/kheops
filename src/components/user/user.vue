<i18n>
{
  "en": {
    "usersettings": "User settings",
    "general": "General",
    "tokens": "Tokens",
    "provider": "Provider"
  },
  "fr": {
    "usersettings": "Préférences utilisateur",
    "general": "Général",
    "tokens": "Tokens",
    "provider": "Fournisseur"
  }
}
</i18n>

<template>
  <div
    id="user"
    class="container"
  >
    <h3 class="pb-3">
      {{ $t('usersettings') }}
    </h3>
    <div class="row">
      <div class="col-lg-2 col-sm-12 col-xs-12 mb-2">
        <nav class="nav nav-pills nav-justified flex-column text-center text-lg-left">
          <a
            v-for="(cat,idx) in categories"
            :key="idx"
            class="nav-link"
            :class="(currentCategory === cat) ? 'active':''"
            @click="loadview(cat)"
          >
            {{ $t(cat) }}
          </a>
        </nav>
      </div>
      <div class="col-lg-10 col-sm-12 col-xs-12">
        <user-settings-general v-if="currentCategory === 'general'" />
        <user-settings-token v-if="currentCategory === 'tokens'" />
      </div>
    </div>
  </div>
</template>

<script>

import userSettingsGeneral from '@/components/user/userSettingsGeneral';
import userSettingsToken from '@/components/user/userSettingsToken';

export default {
  name: 'User',
  components: { userSettingsGeneral, userSettingsToken },
  data() {
    return {
      // categories: ['general', 'tokens', 'provider'],
      categories: ['general', 'tokens'],
    };
  },
  computed: {
    currentCategory() {
      return this.$route.params.category !== undefined ? this.$route.params.category : 'general';
    },
  },
  watch: {
  },
  mounted() {
  },
  methods: {
    loadview(category) {
      this.$router.push({ name: 'usercategory', params: { category } });
    },
  },
};
</script>
