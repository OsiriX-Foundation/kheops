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
    "urlnotvalid": "This url is not valid",
    "unauthorized": "You don't have the permissions"
  },
  "fr": {
    "confirm": "Confirmer",
    "warningremove": "Etes-vous sûre de vouloir supprimer ce webhook ?",
    "editwebhook": "Edition d'un webhook",
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
    "unauthorized": "Vous n'avez pas les permissions"
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
        <div :class="classColRight">
          <b>{{ $t('namewebhook') }}</b>
        </div>
        <div :class="classColLeft">
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
        <div :class="classColRight">
          <b>{{ $t('urlwebhook') }}</b>
        </div>
        <div :class="classColLeft">
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
        <div :class="classColRight">
          <b>{{ $t('secret') }}</b>
        </div>
        <div :class="classColLeft">
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
        <div :class="classColRight">
          <b>{{ $t('event') }}</b>
        </div>
        <div :class="classColLeft">
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
        <div :class="classColRight" />
        <div :class="classColLeft">
          <b-form-checkbox
            v-model="modelWebhook.enabled"
          >
            <b class="pointer">{{ $t('enabled') }}</b>
          </b-form-checkbox>
        </div>
      </div>
      <done-delete-button
        class-row="mb-2"
        class-col="offset-md-5 offset-lg-4 col-xs-12 col-sm-12 col-md-5 col-lg-4"
        class-col-warning-remove="offset-md-5 offset-lg-4 col-sm-12 col-md-6 col-lg-7"
        :text-warning-remove="$t('warningremove')"
        :text-button-done="$t('confirm')"
        :disabled-done="disabledCreate"
        @remove="remove"
      />
    </form>
  </div>
</template>

<script>
import DoneDeleteButton from '@/components/globals/DoneDeleteButton';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'Webhook',
  components: { DoneDeleteButton },
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
      || this.modelWebhook.event.length === 0);
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
          this.$snotify.error(this.$t('unauthorized'));
        } else {
          this.$snotify.error(this.$t('sorryerror'));
        }
      });
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
