<template>
  <div
    v-if="Object.entries(webhook).length > 0"
  >
    <div
      class="my-3 selection-button-container provider-position"
    >
      <h4>
        <button
          type="button"
          class="btn btn-link btn-sm d-md-none"
          @click.stop="done"
        >
          <span>
            <v-icon
              name="arrow-left"
              color="white"
            />
          </span>
        </button>
        {{ $t('webhook.editwebhook') }}
      </h4>
    </div>
    <form @submit.prevent="editWebhook">
      <div class="row mb-3">
        <div :class="classColRight">
          <b>{{ $t('webhook.namewebhook') }}</b>
        </div>
        <div :class="classColLeft">
          <input
            v-model="modelWebhook.name"
            v-focus
            type="text"
            :placeholder="$t('webhook.namewebhook')"
            class="form-control"
            required
            maxlength="255"
          >
          <field-obligatory :state="modelWebhook.name !== ''" />
        </div>
      </div>
      <div class="row mb-3">
        <div :class="classColRight">
          <b>{{ $t('webhook.urlwebhook') }}</b>
        </div>
        <div :class="classColLeft">
          <input
            v-model="modelWebhook.url"
            type="text"
            :placeholder="$t('webhook.urlwebhook')"
            class="form-control"
            required
            maxlength="1024"
          >
          <field-obligatory :state="modelWebhook.url !== ''" />
          <field-obligatory
            v-if="modelWebhook.url !== ''"
            :state="checkUrl(modelWebhook.url)"
            :text="$t('urlnotvalid')"
          />
        </div>
      </div>
      <div
        v-if="webhook.use_secret === false"
        class="row mb-3"
      >
        <div :class="classColRight">
          <b>{{ $t('webhook.secret') }}</b>
        </div>
        <div :class="classColLeft">
          <input
            v-model="modelWebhook.secret"
            type="text"
            :placeholder="$t('webhook.secret')"
            class="form-control"
            maxlength="1024"
          >
        </div>
      </div>
      <div class="row mb-3">
        <div :class="classColRight">
          <b>{{ $t('webhook.event') }}</b>
        </div>
        <div :class="classColLeft">
          <b-form-checkbox-group
            id="events"
            v-model="modelWebhook.event"
            :options="eventsDefined"
            stacked
          >
            <field-obligatory
              :state="state"
              :text="$t('invalidevent')"
            />
          </b-form-checkbox-group>
        </div>
      </div>
      <div class="row mb-3">
        <div :class="classColRight" />
        <div :class="classColLeft">
          <b-form-checkbox
            v-model="modelWebhook.enabled"
          >
            <b class="pointer">{{ $t('webhook.enabled') }}</b>
          </b-form-checkbox>
        </div>
      </div>
      <done-delete-button
        class-row="mb-2"
        class-col="offset-md-5 offset-lg-4 col-xs-12 col-sm-12 col-md-5 col-lg-4"
        class-col-warning-remove="offset-md-5 offset-lg-4 col-sm-12 col-md-6 col-lg-7"
        :text-warning-remove="$t('webhook.warningremove')"
        :disabled-done="disabledCreate"
        :loading="onedit || onLoading"
        @remove="remove"
      />
    </form>
  </div>
</template>

<script>
import DoneDeleteButton from '@/components/globalbutton/DoneDeleteButton';
import httpoperations from '@/mixins/httpoperations';
import FieldObligatory from '@/components/globals/FieldObligatory';
import { validator } from '@/mixins/validator.js';

export default {
  name: 'Webhook',
  components: { DoneDeleteButton, FieldObligatory },
  mixins: [validator],
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
    },
    webhook: {
      type: Object,
      required: true,
      default: () => {},
    },
    classColRight: {
      type: String,
      required: false,
      default: '',
    },
    classColLeft: {
      type: String,
      required: false,
      default: '',
    },
    onLoading: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  data() {
    return {
      eventsDefined: [
        { value: 'new_series', text: this.$t('webhook.new_series') },
        { value: 'new_user', text: this.$t('webhook.new_user') },
        { value: 'remove_series', text: this.$t('webhook.remove_series') },
        { value: 'delete_album', text: this.$t('webhook.delete_album') },
      ],
      modelWebhook: {
        name: '',
        url: '',
        event: [],
        enabled: false,
      },
      onedit: false,
    };
  },
  computed: {
    state() {
      if (this.modelWebhook.event !== undefined && this.modelWebhook.event !== null) {
        return this.modelWebhook.event.length >= 1;
      }
      return false;
    },
    webhookId() {
      return this.$route.params.id;
    },
    disabledCreate() {
      return (this.modelWebhook.name === ''
      || this.modelWebhook.url === ''
      || !this.checkUrl(this.modelWebhook.url)
      || this.modelWebhook.event.length === 0
      || this.onedit);
    },
  },
  watch: {
    webhook() {
      this.setModelWebhook(this.webhook);
    },
  },
  created() {
    this.setModelWebhook(this.webhook);
  },
  methods: {
    setModelWebhook(webhook) {
      this.modelWebhook.name = webhook.name;
      this.modelWebhook.url = webhook.url;
      this.modelWebhook.event = webhook.events;
      this.modelWebhook.enabled = webhook.enabled;
      if (webhook.use_secret === false) {
        this.modelWebhook.secret = '';
      }
    },
    done() {
      this.$emit('done');
    },
    remove() {
      this.$emit('remove', this.webhookId);
    },
    editWebhook() {
      this.onedit = true;
      const params = {
        queries: this.modelWebhook,
        albumId: this.albumId,
        webhookId: this.webhookId,
      };
      this.$store.dispatch('editWebhook', params).then(() => {
        this.$emit('webhook', this.webhookId);
      }).catch((err) => {
        const status = httpoperations.getStatusError(err);
        if (status === 401 || status === 403) {
          this.$snotify.error(this.$t('texterror.unauthorized'));
        } else {
          this.$snotify.error(this.$t('sorryerror'));
        }
        this.onedit = false;
      });
    },
  },
};
</script>
