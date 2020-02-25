<i18n>
{
  "en": {
    "newwebhook": "New webhook",
    "namewebhook": "Name of the webhook",
    "urlwebhook": "Configuration URL of the webhook",
    "secret": "Secret",
    "event": "Event",
    "enabled": "Enabled the webhook",
    "new_series": "New serie/s",
    "new_user": "New user",
    "invalidevent": "Please select minimum one event",
    "fieldobligatory": "Field obligatory",
    "urlnotvalid": "This url is not valid",
    "unauthorized": "You don't have the permission to create a webhook"
  },
  "fr": {
    "newwebhook": "Nouveau webhook",
    "namewebhook": "Nom du webhook",
    "urlwebhook": "URL du webhook",
    "secret": "Secret",
    "event": "Evènement",
    "enabled": "Activer le webhook",
    "new_series": "Nouvelle/s serie/s",
    "new_user": "Nouvel utilisateur",
    "invalidevent": "SVP choississez minimum une évènement",
    "fieldobligatory": "Champs obligatoire",
    "urlnotvalid": "Cette url n'est pas valide",
    "unauthorized": "Vous n'avez pas les permissions de créer un webhook"
  }
}
</i18n>

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
        {{ $t('newwebhook') }}
      </h4>
    </div>
    <form @submit.prevent="createWebhook">
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('namewebhook') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="webhook.name"
            v-focus
            type="text"
            :placeholder="$t('namewebhook')"
            class="form-control"
            required
            maxlength="255"
          >

          <b-form-invalid-feedback :state="webhook.name !== ''">
            {{ $t('fieldobligatory') }}
          </b-form-invalid-feedback>
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('urlwebhook') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="webhook.url"
            type="text"
            :placeholder="$t('urlwebhook')"
            class="form-control"
            required
            maxlength="1024"
          >

          <b-form-invalid-feedback :state="webhook.url !== ''">
            {{ $t('fieldobligatory') }}
          </b-form-invalid-feedback>
          <b-form-invalid-feedback
            v-if="webhook.url !== ''"
            :state="checkUrl(webhook.url)"
          >
            {{ $t('urlnotvalid') }}
          </b-form-invalid-feedback>
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('secret') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="webhook.secret"
            type="text"
            :placeholder="$t('secret')"
            class="form-control"
            maxlength="1024"
          >
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('event') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <b-form-checkbox-group
            id="events"
            v-model="webhook.event"
            :options="eventsDefined"
            :state="state"
            stacked
          >
            <b-form-invalid-feedback :state="state">
              {{ $t('invalidevent') }}
            </b-form-invalid-feedback>
          </b-form-checkbox-group>
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3" />
        <div class="col-xs-12 col-sm-12 col-md-9">
          <b-form-checkbox
            v-model="webhook.enabled"
          >
            <b>{{ $t('enabled') }}</b>
          </b-form-checkbox>
        </div>
      </div>
      <create-cancel-button
        :disabled="disabledCreate"
        @cancel="done"
      />
    </form>
  </div>
</template>

<script>
import CreateCancelButton from '@/components/globals/CreateCancelButton';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'NewWebhook',
  components: { CreateCancelButton },
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
        { value: 'new_series', text: this.$t('new_series') },
        { value: 'new_user', text: this.$t('new_user') },
      ],
      webhook: {
        url: '',
        name: '',
        secret: '',
        event: [],
        enabled: false,
      },
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
      || this.webhook.event.length === 0);
    },
  },
  methods: {
    createWebhook() {
      const queries = httpoperations.getFormData(this.webhook);
      const url = `albums/${this.albumId}/webhooks`;
      HTTP.post(url, queries).then(() => {
        this.done();
      }).catch((err) => {
        const status = httpoperations.getStatusError(err);
        if (status === 401 || status === 403) {
          this.$snotify.error(this.$t('unauthorized'));
        } else {
          this.$snotify.error(this.$t('sorryerror'));
        }
      });
    },
    done() {
      this.$emit('done');
    },
    // https://stackoverflow.com/questions/5717093/check-if-a-javascript-string-is-a-url
    checkUrl(str) {
      const pattern = new RegExp('^https?:\\/\\/'+ // protocol
        '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|'+ // domain name
        '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
        '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*'+ // port and path
        '(\\?[;&a-z\\d%_.~+=-]*)?'+ // query string
        '(\\#[-a-z\\d_]*)?$','i'); // fragment locator
      return !!pattern.test(str);
    },
  },
};
</script>
