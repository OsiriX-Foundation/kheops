<i18n>
  {
    "en": {
      "url": "URL provider",
      "stateurl": "Access provider",
      "user": "User",
      "clientid": "Client ID",
      "created_time": "Created time",
      "edit": "Edit",
      "remove": "Remove",
      "back": "Back",
      "client_name": "Client name",
      "client_uri": "Client URL",
      "logo_uri": "Logo URL",
      "tos_uri": "terms of service document",
      "policy_uri": "Privacy policy document",
      "software_version": "Software version",
      "contacts": "Contact",
      "supported_modalities": "Supported modalities",
      "warningremove": "Are you sure to remove this report provider ?",
      "confirm": "Confirm",
      "cancel": "Cancel"
    },
    "fr": {
      "url": "URL de configuration",
      "stateurl": "Etat du provider",
      "user": "Utilisateur",
      "clientid": "Identifiant du provider",
      "created_time": "Date de création",
      "edit": "Editer",
      "remove": "Supprimer",
      "back": "Retour",
      "client_name": "Nom du client",
      "client_uri": "URL du client",
      "logo_uri": "URL du logo",
      "tos_uri": "Document de conditions d'utilisation",
      "policy_uri": "Document de la politique de confidentialité",
      "software_version": "Version du logiciel",
      "contacts": "Contact",
      "supported_modalities": "Modalités supportées",
      "warningremove": "Etes-vous sûr de vouloir supprimer ce provider ?",
      "confirm": "Confirmer",
      "cancel": "Annuler"
    }
  }
</i18n>
<template>
  <div v-if="Object.keys(provider).length > 0">
    <div
      class="my-3 provider-position"
    >
      <h4>
        <button
          type="button"
          class="btn btn-link btn-sm d-md-none"
          @click.stop="back"
        >
          <span>
            <v-icon
              name="arrow-left"
              color="white"
            />
          </span>
        </button>
        <span
          class="breakwork"
        >
          {{ provider.name }}
        </span>
      </h4>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('url') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.url }}
          </dd>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('stateurl') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <div class="d-inline-flex">
            <state-provider
              :loading="provider.stateURL.loading"
              :check-u-r-l="provider.stateURL.checkURL"
            />
          </div>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('clientid') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.client_id }}
          </dd>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('created_time') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.created_time }}
          </dd>
        </div>
      </div>
      <span
        v-if="provider.data"
      >
        <div
          v-if="provider.data.client_name"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('client_name') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              {{ provider.data.client_name }}
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.client_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('client_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.client_uri">
                {{ provider.data.client_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.logo_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('logo_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.logo_uri">
                {{ provider.data.logo_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.contacts"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('contacts') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <li
              v-for="contact in provider.data.contacts"
              :key="contact.id"
            >
              <a :href="`mailto:${contact}`">
                {{ contact }}
              </a>
            </li>
          </div>
        </div>
        <div
          v-if="provider.data.tos_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('tos_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.tos_uri">
                {{ provider.data.tos_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.policy_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('policy_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.policy_uri">
                {{ provider.data.policy_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.software_version"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('software_version') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              {{ provider.data.software_version }}
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.supported_modalities"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('supported_modalities') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <li
                v-for="modality in provider.data.supported_modalities"
                :key="modality.id"
              >
                {{ modality }}
              </li>
            </dd>
          </div>
        </div>
      </span>
    </div>
    <div class="row mb-2">
      <div class="offset-md-3 col-12 col-sm-12 col-md-3">
        <button
          v-if="writePermission"
          class="btn btn-primary btn-block"
          @click.stop="edit()"
        >
          {{ $t('edit') }}
        </button>
        <button
          v-if="writePermission && !confirmDelete"
          type="button"
          class="btn btn-danger btn-block"
          @click="deleteProvider"
        >
          {{ $t('remove') }}
        </button>
      </div>
    </div>
    <div
      v-if="writePermission && confirmDelete"
      class="row mb-2"
    >
      <div class="offset-md-3 col-12 col-md-9">
        <p
          class="mt-2"
        >
          {{ $t('warningremove') }}
        </p>
      </div>
    </div>
    <div
      v-if="writePermission && confirmDelete"
      class="row mb-2"
    >
      <div class="offset-md-3 col-12 col-sm-12 col-md-3">
        <button
          v-if="writePermission && confirmDelete"
          type="button"
          class="btn btn-danger btn-block"
          @click="deleteProvider"
        >
          {{ $t('confirm') }}
        </button>
        <button
          v-if="writePermission && confirmDelete"
          type="button"
          class="btn btn-secondary btn-block"
          @click="confirmDelete=false"
        >
          {{ $t('cancel') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import StateProvider from '@/components/providers/StateProvider';

export default {
  name: 'Provider',
  components: { StateProvider },
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
      confirmDelete: false,
    };
  },
  computed: {
    ...mapGetters({
      provider: 'provider',
    }),
    clientID() {
      return this.$route.params.id;
    },
  },
  created() {
    this.$store.dispatch('getProvider', { albumID: this.albumID, clientID: this.clientID }).then((res) => {
      if (res.status !== 200) {
        this.$snotify.error('Sorry, an error occured');
        this.back();
      }
    }).catch((err) => {
      if (err.response !== undefined && err.response.status === 404) {
        this.$snotify.error('Report provider not found');
      } else {
        this.$snotify.error('Sorry, an error occured');
      }
      this.back();
    });
  },
  methods: {
    back() {
      this.$emit('done');
    },
    edit() {
      this.$emit('providerselectededit', this.clientID);
    },
    deleteProvider() {
      if (this.confirmDelete === false) {
        this.confirmDelete = true;
      } else {
        this.$store.dispatch('deleteProvider', { albumID: this.albumID, clientID: this.clientID }).then((res) => {
          if (res.status !== 204) {
            this.$snotify.error('Sorry, an error occured');
          } else {
            this.$emit('done');
          }
        }).catch((err) => {
          console.log(err);
        });
      }
    },
  },
};
</script>
