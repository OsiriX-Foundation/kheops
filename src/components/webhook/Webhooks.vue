<template>
  <div>
    <div
      v-if="(currentView === 'listwebhook')"
      class="my-3 selection-button-container provider-position"
    >
      <router-link
        :to="{
          name: 'albumsettingsaction',
          params: { action: 'newwebhook' }
        }"
        class="btn btn-secondary"
      >
        <v-icon
          name="plus"
          class="mr-2"
        />
        {{ $t('webhook.newwebhook') }}
      </router-link>
    </div>
    <list-webhook
      v-if="currentView === 'listwebhook'"
      :album-id="albumId"
      @webhookselectedshow="showWebhook"
      @webhookselectededit="editWebhook"
    />
    <new-webhook
      v-if="currentView === 'newwebhook'"
      :album-id="albumId"
      @done="loadAction('listwebhook')"
    />
    <webhook
      v-if="currentView === 'webhook' || currentView === 'editwebhook'"
      :album-id="albumId"
      @done="loadAction('listwebhook')"
      @edit="editWebhook"
    />
  </div>
</template>

<script>
import ListWebhook from '@/components/webhook/ListWebhook';
import NewWebhook from '@/components/webhook/NewWebhook';
import Webhook from '@/components/webhook/Webhook';

export default {
  name: 'Webhooks',
  components: {
    ListWebhook, NewWebhook, Webhook,
  },
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
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
      return this.$route.params.action !== undefined ? this.$route.params.action : 'listwebhook';
    },
  },
  created() {
  },
  methods: {
    showWebhook(id) {
      this.loadActionId('webhook', id);
    },
    editWebhook(id) {
      this.loadActionId('editwebhook', id);
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
