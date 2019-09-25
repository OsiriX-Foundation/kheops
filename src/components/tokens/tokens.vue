<i18n>
  {
    "en": {
      "newtoken": "New token",
      "showrevokedtoken": "Show revoked tokens",
      "showinvalidtoken": "Show invalid tokens",
      "revoke": "revoke",
      "revoked": "revoked",
      "active": "active",
      "expired": "expired",
      "revokedsuccess": "revoked successfully",
      "expiration date": "expiration date",
      "status": "status",
      "description": "description",
      "scope": "scope",
      "create date": "create date",
      "last used": "last used",
      "permission": "permission"

    },
    "fr": {
      "newtoken": "Nouveau token",
      "showrevokedtoken": "Afficher les tokens révoqués",
      "showinvalidtoken": "Afficher les tokens invalides",
      "revoke": "révoquer",
      "revoked": "révoqué",
      "active": "actif",
      "expired": "expiré",
      "revokedsuccess": "révoqué avec succès",
      "expiration date": "date d'expiration",
      "scope": "application",
      "create date": "créé le",
      "last used": "dern. utilisation",
      "permission": "permission"
    }
  }
</i18n>

<template>
  <div class="tokens">
    <div
      v-if="currentView === 'listtokens'"
    >
      <list-tokens
        :scope="scope"
        :albumid="albumid"
      />
    </div>
    <div
      v-if="currentView === 'newtoken'"
    >
      <new-token
        :scope="scope"
        :albumid="albumid"
        @done="loadView('listtokens')"
      />
    </div>
    <div
      v-if="currentView === 'token'"
    >
      <token
        @done="loadView('listtokens')"
      />
    </div>
  </div>
</template>

<script>
import Vue from 'vue';
import VueClipboard from 'vue-clipboard2';
import ListTokens from '@/components/tokens/ListTokens';
import newToken from '@/components/tokens/newToken';
import token from '@/components/tokens/token';

VueClipboard.config.autoSetContainer = true; // add this line
Vue.use(VueClipboard);
export default {
  name: 'Tokens',
  components: { newToken, token, ListTokens },
  props: {
    scope: {
      type: String,
      required: true,
    },
    albumid: {
      type: String,
      required: false,
      default: '',
    },
  },
  data() {
    return {};
  },
  computed: {
    currentView() {
      return this.$route.params.action !== undefined ? this.$route.params.action : 'listtokens';
    },
  },
  watch: {
  },
  created() {
  },
  methods: {
    loadView(action) {
      if (this.scope === 'album') {
        this.$router.push({ name: 'albumsettingsaction', params: { action } });
      } else if (this.scope === 'user') {
        this.$router.push({ name: 'useraction', params: { action } });
      }
    },
  },
};
</script>

<style scoped>
.selection-button-container{
height: 60px;
}
.toggle-label{
vertical-align: top;
}
dt{
text-align: right;
}

tr:hover {
visibility: visible;
}

</style>
