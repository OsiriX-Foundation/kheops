<i18n>
  {
    "en": {
      "newprovider": "New report provider"

    },
    "fr": {
      "newprovider": "Nouveau report provider"
    }
  }
</i18n>

<template>
  <div>
    <div
      v-if="(currentView === 'listproviders') && writePermission"
      class="my-3 selection-button-container provider-position"
    >
      <button
        class="btn btn-secondary"
        @click="loadAction('newprovider')"
      >
        <v-icon
          name="plus"
          class="mr-2"
        />
        {{ $t('newprovider') }}
      </button>
    </div>

    <new-provider
      v-if="currentView === 'newprovider' && writePermission"
      :album-i-d="albumID"
      @done="loadAction('listproviders')"
    />
    <provider
      v-if="currentView === 'provider'"
      :album-i-d="albumID"
      :client-i-d="clientIdSelected"
      :write-permission="writePermission"
      @done="loadAction('listproviders')"
      @providerselectededit="editProvider"
    />
    <edit-provider
      v-if="currentView === 'editprovider' && writePermission"
      :album-i-d="albumID"
      :client-i-d="clientIdSelected"
      @done="loadAction('listproviders')"
    />
    <list-providers
      v-if="currentView === 'listproviders'"
      :album-i-d="albumID"
      :write-permission="writePermission"
      @providerselectedshow="showProvider"
      @providerselectededit="editProvider"
    />
  </div>
</template>

<script>
import NewProvider from '@/components/providers/NewProvider';
import Provider from '@/components/providers/Provider';
import ListProviders from '@/components/providers/ListProviders';
import EditProvider from '@/components/providers/EditProvider';

export default {
  name: 'Providers',
  components: {
    NewProvider, Provider, ListProviders, EditProvider,
  },
  props: {
    albumID: {
      type: String,
      required: true,
      default: '',
    },
    writePermission: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {
      view: 'list',
      clientIdSelected: '',
    };
  },
  computed: {
    currentView() {
      return this.$route.params.action !== undefined ? this.$route.params.action : 'listproviders';
    },
  },
  created() {
  },
  methods: {
    manageView(view) {
      if (view === 'edit') {
        this.editProvider(this.$route.query.object);
      } else if (view === 'provider') {
        this.showProvider(this.$route.query.object);
      } else {
        this.view = view;
      }
    },
    showProvider(clientId) {
      this.$store.dispatch('initProvider');
      this.clientIdSelected = clientId;
      this.loadActionId('provider', clientId);
    },
    editProvider(clientId) {
      this.$store.dispatch('initProvider');
      this.clientIdSelected = clientId;
      this.loadActionId('editprovider', clientId);
    },
    loadAction(action) {
      this.$router.push({ name: 'albumsettingsaction', params: { action } });
    },
    loadActionId(action, id) {
      this.$router.push({ name: 'albumsettingsactionid', params: { action, id } });
    },
  },
};
</script>
