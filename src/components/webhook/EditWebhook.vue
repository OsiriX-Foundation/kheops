<i18n>
{
  "en": {
    "confirm": "Confirm",
    "warningremove": "Are you sure to remove this webhook ?",
    "editwebhook": "Edit webhook",
    "namewebhook": "Name of the webhook",
    "urlwebhook": "Configuration URL of the webhook",
    "secret": "Secret",
    "event": "Event",
    "enabled": "Enabled the webhook",
    "new_series": "New serie/s",
    "new_user": "New user",
    "invalidevent": "Please select minimum one event",
    "fieldobligatory": "Field obligatory",
    "urlnotvalid": "This url is not valid"
  },
  "fr": {
    "confirm": "Confirmer",
    "warningremove": "Etes-vous sûre de vouloir supprimer ce webhook ?",
    "editwebhook": "Edition d'un webhook",
    "namewebhook": "Nom du webhook",
    "urlwebhook": "URL de webhook",
    "secret": "Secret",
    "event": "Evènement",
    "enabled": "Activer le webhook",
    "new_series": "Nouvelle/s serie/s",
    "new_user": "Nouvel utilisateur",
    "invalidevent": "SVP choississez minimum une évènement",
    "fieldobligatory": "Champs obligatoire",
    "urlnotvalid": "Cette url n'est pas valide"
  }
}
</i18n>

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
        {{ $t('editwebhook') }}
      </h4>
    </div>
    <form @submit.prevent="editWebhook">
      <div class="row mb-3">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('namewebhook') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="modelWebhook.name"
            v-focus
            type="text"
            :placeholder="$t('namewebhook')"
            class="form-control"
            required
            maxlength="255"
          >

          <b-form-invalid-feedback :state="modelWebhook.name !== ''">
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
            v-model="modelWebhook.url"
            type="text"
            :placeholder="$t('urlwebhook')"
            class="form-control"
            required
            maxlength="1024"
          >

          <b-form-invalid-feedback :state="modelWebhook.url !== ''">
            {{ $t('fieldobligatory') }}
          </b-form-invalid-feedback>
          <b-form-invalid-feedback
            v-if="modelWebhook.url !== ''"
            :state="checkUrl(modelWebhook.url)"
          >
            {{ $t('urlnotvalid') }}
          </b-form-invalid-feedback>
        </div>
      </div>
      <div
        v-if="webhook.use_secret === false"
        class="row mb-3"
      >
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('secret') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9">
          <input
            v-model="modelWebhook.secret"
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
            v-model="modelWebhook.event"
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
        <div class="offset-xs-12 offset-sm-12 offset-md-3 col-xs-12 col-sm-12 col-md-9">
          <b-form-checkbox
            v-model="modelWebhook.enabled"
          >
            <b>{{ $t('enabled') }}</b>
          </b-form-checkbox>
        </div>
      </div>
      <done-delete-button
        class-row="mb-2"
        class-col="offset-md-4 offset-lg-3 col-xs-12 col-sm-12 col-md-4 col-lg-3"
        :text-warning-remove="$t('warningremove')"
        :text-button-done="$t('confirm')"
      />
    </form>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import DoneDeleteButton from '@/components/globals/DoneDeleteButton';

export default {
  name: 'Webhook',
  components: { DoneDeleteButton },
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
      modelWebhook: {
        name: '',
        url: '',
        event: [],
        enabled: false,
      },
    };
  },
  computed: {
    ...mapGetters({
      webhook: 'webhook',
    }),
    state() {
      if (this.modelWebhook.event !== undefined && this.modelWebhook.event !== null) {
        return this.modelWebhook.event.length >= 1;
      }
      return false;
    },
    webhookId() {
      return this.$route.params.id;
    },
  },
  created() {
    const params = {
      albumId: this.albumId,
      webhookId: this.webhookId,
    };
    this.$store.dispatch('getWebhook', params).then(() => {
      this.setModelWebhook(this.webhook);
    });
  },
  beforeDestroy() {
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
    editWebhook() {
      console.log('edit');
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
