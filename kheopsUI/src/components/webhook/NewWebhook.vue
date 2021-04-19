<template>
  <div>
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
        {{ $t('webhook.newwebhook') }}
      </h4>
    </div>
    <form @submit.prevent="createWebhook">
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('webhook.namewebhook') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="webhook.name"
            v-focus
            type="text"
            :placeholder="$t('webhook.namewebhook')"
            class="form-control"
            required
            maxlength="255"
          >
          <field-obligatory :state="webhook.name !== ''" />
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('webhook.urlwebhook') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="webhook.url"
            type="text"
            :placeholder="$t('webhook.urlwebhook')"
            class="form-control"
            required
            maxlength="1024"
          >
          <field-obligatory :state="webhook.url !== ''" />
          <field-obligatory
            v-if="webhook.url !== ''"
            :state="checkUrl(webhook.url)"
            :text="$t('urlnotvalid')"
          />
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('webhook.secret') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="webhook.secret"
            type="text"
            :placeholder="$t('webhook.secret')"
            class="form-control"
            maxlength="1024"
          >
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('webhook.event') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <b-form-checkbox-group
            id="events"
            v-model="webhook.event"
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
        <div class="col-xs-12 col-sm-12 col-md-3" />
        <div class="col-xs-12 col-sm-12 col-md-9">
          <b-form-checkbox
            v-model="webhook.enabled"
          >
            <b>{{ $t('webhook.enabled') }}</b>
          </b-form-checkbox>
        </div>
      </div>
      <create-cancel-button
        :disabled="disabledCreate"
        :loading="oncreate"
        class-col="offset-md-3 col-md-9"
        @cancel="done"
      />
    </form>
  </div>
</template>

<script>
import CreateCancelButton from '@/components/globalbutton/CreateCancelButton';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';
import FieldObligatory from '@/components/globals/FieldObligatory';
import { validator } from '@/mixins/validator.js';

export default {
  name: 'NewWebhook',
  components: { CreateCancelButton, FieldObligatory },
  mixins: [validator],
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
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
      webhook: {
        url: '',
        name: '',
        secret: '',
        event: [],
        enabled: true,
      },
      oncreate: false,
    };
  },
  computed: {
    state() {
      return this.webhook.event.length >= 1;
    },
    disabledCreate() {
      return (this.webhook.name === ''
      || this.webhook.url === ''
      || !this.checkUrl(this.webhook.url)
      || this.webhook.event.length === 0
      || this.oncreate
      );
    },
  },
  methods: {
    createWebhook() {
      this.oncreate = true;
      const queries = httpoperations.getFormData(this.webhook);
      const url = `albums/${this.albumId}/webhooks`;
      HTTP.post(url, queries).then(() => {
        this.done();
      }).catch((err) => {
        const status = httpoperations.getStatusError(err);
        if (status === 401 || status === 403) {
          this.$snotify.error(this.$t('texterror.unauthorized'));
        } else {
          this.$snotify.error(this.$t('sorryerror'));
        }
        this.oncreate = false;
      });
    },
    done() {
      this.$emit('done');
    },
  },
};
</script>
