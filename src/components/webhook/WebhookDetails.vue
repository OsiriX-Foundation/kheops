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
  <span
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
        {{ webhook.name }}
      </h4>
    </div>
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('urlwebhook') }}</b>
      </div>
      <div :class="classColLeft">
        {{ webhook.url }}
      </div>
    </div>
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('secret') }}</b>
      </div>
      <div :class="classColLeft">
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
      <div :class="classColRight">
        <b>{{ $t('event') }}</b>
      </div>
      <div :class="classColLeft">
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
    <div class="row mb-3">
      <div :class="classColRight">
        <b>{{ $t('state') }}</b>
      </div>
      <div :class="classColLeft">
        <b
          v-if="webhook.enabled === true"
        >
          {{ $t('webhookenabled') }}
        </b>
        <b
          v-else-if="webhook.enabled === false"
        >
          {{ $t('webhookdisabled') }}
        </b>
      </div>
    </div>
    <done-delete-button
      class-row="mb-2"
      class-col="offset-md-5 offset-lg-4 col-xs-12 col-sm-12 col-md-5 col-lg-4"
      class-col-warning-remove="offset-md-5 offset-lg-4 col-sm-12 col-md-6 col-lg-7"
      :text-warning-remove="$t('warningremove')"
      :text-button-done="$t('edit')"
      @done="edit"
      @remove="remove"
    />
  </span>
</template>

<script>
import DoneDeleteButton from '@/components/globals/DoneDeleteButton';

export default {
  name: 'WebhookDetails',
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
    };
  },
  computed: {
    webhookId() {
      return this.$route.params.id;
    },
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
    done() {
      this.$emit('done');
    },
    edit() {
      this.$emit('edit', this.webhookId);
    },
    remove() {
      this.$emit('remove', this.webhookId);
    },
  },
};
</script>
