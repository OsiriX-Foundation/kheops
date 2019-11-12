<i18n>
{
  "en": {
    "editprovider": "Edit provider",
    "nameProvider": "Name of the provider",
    "urlProvider": "Configuration URL of the provider",
    "edit": "Confirm",
    "remove": "Remove",
    "warningremove": "Are you sure to remove this report provider ?",
    "confirm": "Confirm",
    "cancel": "Cancel"
  },
  "fr": {
    "editprovider": "Edition d'un provider",
    "nameProvider": "Nom du provider",
    "urlProvider": "URL de configuration",
    "edit": "Confirmer",
    "remove": "Supprimer",
    "warningremove": "Etes-vous sûr de vouloir supprimer ce provider ?",
    "confirm": "Confirmer",
    "cancel": "Annuler"
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
          @click.stop="cancel"
        >
          <span>
            <v-icon
              name="arrow-left"
              color="white"
            />
          </span>
        </button>
        {{ $t('editprovider') }}
      </h4>
    </div>
    <form @submit.prevent="updateProvider">
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('nameProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <input
            v-model="provider.name"
            type="text"
            class="form-control"
            required
            maxlength="1024"
          >
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('urlProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <div class="input-group mb-3">
            <input
              v-model="provider.url"
              type="text"
              class="form-control"
              required
              maxlength="1024"
            >

            <div
              v-if="show"
              class="input-group-append"
            >
              <state-provider
                :loading="loading"
                :check-u-r-l="checkURL"
                :class-icon="'ml-2 mt-2'"
              />
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="offset-md-3 col-12 col-sm-12 col-md-3">
          <button
            type="submit"
            class="btn btn-primary btn-block"
            :disabled="loading"
          >
            {{ $t('edit') }}
          </button>
          <button
            v-if="!confirmDelete"
            type="button"
            class="btn btn-danger btn-block"
            @click="deleteProvider"
          >
            {{ $t('remove') }}
          </button>
        </div>
      </div>
      <div
        v-if="confirmDelete"
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
        v-if="confirmDelete"
        class="row mb-2"
      >
        <div class="offset-md-3 col-12 col-sm-12 col-md-3">
          <button
            v-if="confirmDelete"
            type="button"
            class="btn btn-danger btn-block"
            @click="deleteProvider"
          >
            {{ $t('confirm') }}
          </button>
          <button
            v-if="confirmDelete"
            type="button"
            class="btn btn-secondary btn-block"
            @click="confirmDelete=false"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>
<script>
import { mapGetters } from 'vuex';
import StateProvider from '@/components/providers/StateProvider';

export default {
  name: 'EditProvider',
  components: { StateProvider },
  props: {
    albumID: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      show: false,
      checkURL: false,
      loading: false,
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
        this.cancel();
      }
    }).catch((err) => {
      if (err.response !== undefined && err.response.status === 404) {
        this.$snotify.error('Report provider not found');
      } else {
        this.$snotify.error('Sorry, an error occured');
      }
      this.cancel();
    });
  },
  methods: {
    updateProvider() {
      this.setStateProvider(false, true, true);
      const paramsURL = {
        albumID: this.albumID,
        clientID: this.clientID,
      };
      const query = {
        name: this.provider.name,
        url: this.provider.url,
      };

      this.$store.dispatch('updateProvider', { paramsURL, query }).then((res) => {
        if (res.status !== 200) {
          this.setStateProvider(false, false, true);
        } else {
          this.$emit('done');
        }
      }).catch((err) => {
        this.setStateProvider(false, false, true);
        console.log(err);
      });
    },
    setStateProvider(checkURL, loading, show) {
      this.checkURL = checkURL;
      this.loading = loading;
      this.show = show;
    },
    cancel() {
      this.$emit('done');
    },
    deleteProvider() {
      if (!this.confirmDelete) {
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
