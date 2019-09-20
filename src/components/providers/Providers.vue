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
      v-if="(view === undefined || view === 'list') && writePermission"
      class="my-3 selection-button-container"
      style=" position: relative;"
    >
      <h4>
        <span
          class="link"
          @click="view='new'"
        >
          <v-icon
            name="plus"
            scale="1"
            class="mr-3"
          />{{ $t('newprovider') }}
        </span>
      </h4>
    </div>

    <new-provider
      v-if="view === 'new' && writePermission"
      :album-i-d="albumID"
      @done="view='list'"
    />
    <provider
      v-if="view === 'provider'"
      :album-i-d="albumID"
      :client-i-d="clientIdSelected"
      :write-permission="writePermission"
      @done="view='list'"
      @providerselectededit="editProvider"
    />
    <edit-provider
      v-if="view === 'edit' && writePermission"
      :album-i-d="albumID"
      :client-i-d="clientIdSelected"
      @done="view='list'"
    />
    <list-providers
      v-if="view === 'list' || view === undefined"
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
  watch: {
    view() {
      const query = JSON.parse(JSON.stringify(this.$route.query));
      query.settingview = this.view;
      if (this.view === 'provider' || this.view === 'edit') {
        query.object = this.clientIdSelected;
      }
      this.$router.push({ query });
    },
    '$route.query': function () {
      this.manageView(this.$route.query.settingview);
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
      this.view = 'provider';
    },
    editProvider(clientId) {
      this.$store.dispatch('initProvider');
      this.clientIdSelected = clientId;
      this.view = 'edit';
    },
  },
};
</script>

<style scoped>
.selection-button-container{
  height: 60px;
}
</style>
