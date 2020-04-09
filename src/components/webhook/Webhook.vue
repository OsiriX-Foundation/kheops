<template>
  <div>
    <div
      class="row"
    >
      <div
        v-if="currentView === 'webhook'"
        class="col-12"
      >
        <webhook-details
          :album-id="albumId"
          :webhook="webhook"
          :on-loading="onLoading"
          class-col-right="col-xs-12 col-sm-12 col-md-5 col-lg-4"
          class-col-left="col-xs-12 col-sm-12 col-md-7 col-lg-8"
          @done="done"
          @edit="editWebhook"
          @remove="removeWebhook"
        />
      </div>
      <div
        v-if="currentView === 'editwebhook'"
        class="col-12"
      >
        <edit-webhook
          :album-id="albumId"
          :webhook="webhook"
          :on-loading="onLoading"
          class-col-right="col-xs-12 col-sm-12 col-md-5 col-lg-4"
          class-col-left="col-xs-12 col-sm-12 col-md-7 col-lg-8"
          @done="done"
          @webhook="showWebhook"
          @remove="removeWebhook"
        />
      </div>
      <div
        class="col-12"
      >
        <list-triggers
          :triggers="webhook.triggers"
          :per-page="limit"
          :rows="webhook.number_of_triggers"
          :loading="loading"
          @manualtrigger="getWebhook()"
          @refresh="getWebhook()"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import httpoperations from '@/mixins/httpoperations';
import ListTriggers from '@/components/webhook/ListTriggers';
import WebhookDetails from '@/components/webhook/WebhookDetails';
import EditWebhook from '@/components/webhook/EditWebhook';

export default {
  name: 'Webhook',
  components: { ListTriggers, WebhookDetails, EditWebhook },
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      classColRight: 'col-xs-12 col-sm-12 col-md-5 col-lg-4',
      classColLeft: 'col-xs-12 col-sm-12 col-md-7 col-lg-8',
      loading: true,
      limit: 10,
      offset: 0,
      onLoading: false,
    };
  },
  computed: {
    ...mapGetters({
      webhook: 'webhook',
    }),
    webhookId() {
      return this.$route.params.id;
    },
    currentView() {
      return this.$route.params.action !== undefined ? this.$route.params.action : 'listwebhook';
    },
  },
  created() {
    this.getWebhook();
  },
  beforeDestroy() {
    this.$store.dispatch('initWebhook');
  },
  methods: {
    done() {
      this.$emit('done');
    },
    showWebhook(id) {
      this.getWebhook().then(() => {
        this.loadActionId('webhook', id);
      });
    },
    editWebhook(id) {
      this.loadActionId('editwebhook', id);
    },
    loadActionId(action, id) {
      this.$router.push({ name: 'albumsettingsactionid', params: { action, id } });
    },
    getWebhook() {
      this.loading = true;
      const params = {
        albumId: this.albumId,
        webhookId: this.webhookId,
      };
      return this.$store.dispatch('getWebhook', params).then(() => {
        this.loading = false;
      }).catch((err) => {
        this.loading = false;
        this.manageError(err);
        this.done();
      });
    },
    removeWebhook(webhookId) {
      this.onLoading = true;
      const params = {
        albumId: this.albumId,
        webhookId,
      };
      this.$store.dispatch('removeWebhook', params).then(() => {
        this.done();
      }).catch((err) => {
        this.onLoading = false;
        this.manageError(err);
      });
    },
    manageError(err) {
      const status = httpoperations.getStatusError(err);
      if (status === 401 || status === 403) {
        this.$snotify.error(this.$t('texterror.unauthorized'));
      } else {
        this.$snotify.error(this.$t('sorryerror'));
      }
    },
  },
};
</script>
