<i18n>
{
  "en": {
    "webhook": "Webhook",
    "urlwebhook": "URL of the webhook",
    "secret": "Secret",
    "event": "Event",
    "nosecret": "No secret defined",
    "secretdefined": "A secret has been defined",
    "enabled": "Enabled",
    "new_series": "New series",
    "new_user": "New user",
    "noevent": "No event defined",
    "numbertriggers": "Number of triggers",
    "warningremove": "Are you sure to remove this webhook ?",
    "edit": "Edit",
    "webhookdisabled": "This webhook is disabled",
    "webhookenabled": "This webhook is enabled",
    "state": "State"
  },
  "fr": {
    "webhook": "Webhook",
    "urlwebhook": "URL du webhook",
    "secret": "Secret",
    "event": "Evènement",
    "nosecret": "Pas de secret défini",
    "secretdefined": "Un secret a été défini",
    "enabled": "Activé",
    "new_series": "Nouvelles séries",
    "new_user": "Nouvel utilisateur",
    "noevent": "Pas d'évèment défini",
    "numbertriggers": "Nombre déclenchements",
    "warningremove": "Etes-vous sûre de vouloir supprimer ce webhook ?",
    "edit": "Editer",
    "webhookdisabled": "Ce webhook est désactivé",
    "webhookenabled": "Ce webhook est activé",
    "state": "Etat"
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
        {{ $t('webhook') }} - {{ webhook.name }}
      </h4>
    </div>
    <div class="row">
      <div
        class="col-lg-5"
      >
        <div class="row mb-3">
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-5">
            <b>{{ $t('state') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-7">
            <b
              v-if="webhook.enabled === true"
            >
              {{ $t('webhookenabled') }}
            </b>
            <b
              v-else-if="webhook.enabled === true"
            >
              {{ $t('webhookdisabled') }}
            </b>
          </div>
        </div>
        <div class="row mb-3">
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-5">
            <b>{{ $t('urlwebhook') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-7">
            {{ webhook.url }}
          </div>
        </div>
        <div class="row mb-3">
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-5">
            <b>{{ $t('secret') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-7">
            <span
              v-if="webhook.use_secret === true"
            >
              {{ $t('secretdefined') }}
            </span>
            <span
              v-else-if="webhook.use_secret === false"
            >
              {{ $t('nosecret') }}
            </span>
          </div>
        </div>
        <div class="row mb-3">
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-5">
            <b>{{ $t('event') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-6 col-lg-7">
            <span
              v-if="webhook.events.length > 0"
            >
              <li
                v-for="event in webhook.events"
                :key="event.id"
              >
                {{ $t(event) }}
              </li>
            </span>
            <span
              v-else-if="webhook.events.length === 0"
            >
              {{ $t('noevent') }}
            </span>
          </div>
        </div>
        <done-delete-button
          class-row="mb-2"
          class-col="offset-md-6 offset-lg-5 col-xs-12 col-sm-12 col-md-6 col-lg-5"
          class-col-warning-remove="offset-md-6 offset-lg-5 col-sm-12 col-md-6 col-lg-7"
          :text-warning-remove="$t('warningremove')"
          :text-button-done="$t('edit')"
          @done="edit"
          @remove="removeWebhook"
        />
      </div>
      <div
        v-if="webhook.number_of_triggers > 0"
        class="col-lg-7"
      >
        <triggers
          :triggers="webhook.triggers"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import DoneDeleteButton from '@/components/globals/DoneDeleteButton';
import Triggers from '@/components/webhook/Triggers';

export default {
  name: 'Webhook',
  components: { DoneDeleteButton, Triggers },
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
    };
  },
  computed: {
    ...mapGetters({
      webhook: 'webhook',
    }),
    webhookId() {
      return this.$route.params.id;
    },
  },
  created() {
    const params = {
      albumId: this.albumId,
      webhookId: this.webhookId,
    };
    this.$store.dispatch('getWebhook', params);
  },
  beforeDestroy() {
    this.$store.dispatch('initWebhook');
  },
  methods: {
    done() {
      this.$emit('done');
    },
    edit() {
      this.$emit('edit', this.webhookId);
    },
    removeWebhook() {
      const params = {
        albumId: this.albumId,
        webhookId: this.webhookId,
      };
      this.$store.dispatch('removeWebhook', params).then(() => {
        this.done();
      });
    },
  },
};
</script>
