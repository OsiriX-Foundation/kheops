<template>
  <div
    v-if="writePermission"
    class="tokens"
  >
    <div
      v-if="(currentView === 'listtokens')"
      class="my-3 selection-button-container provider-position"
    >
      <router-link
        :to="{
          name: routename,
          params: { action: 'newtoken' }
        }"
        class="btn btn-secondary"
      >
        <v-icon
          name="plus"
          class="mr-2"
        />
        {{ $t('token.newtoken') }}
      </router-link>
    </div>
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
    writePermission: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {};
  },
  computed: {
    currentView() {
      return this.$route.params.action !== undefined ? this.$route.params.action : 'listtokens';
    },
    routename() {
      return this.scope === 'album' ? 'albumsettingsaction' : 'useraction';
    },
  },
  watch: {
  },
  created() {
  },
  methods: {
    loadView(action) {
      this.$router.push({ name: this.routename, params: { action } });
    },
  },
};
</script>
