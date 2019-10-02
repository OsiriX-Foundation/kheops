<i18n scoped>
{
  "en": {
    "write": "Add and delete series in the album",
    "download": "Can download the studies",
    "expirationdate": "Expiration date",
    "create": "Create",
    "cancel": "Cancel"
  },
  "fr": {
    "write": "Ajouter et supprimer des séries dans l'album",
    "download": "Peux télécharger les études présentes",
    "expirationdate": "Date d'expiration",
    "create": "Créer",
    "cancel": "Annuler"
  }
}
</i18n>
<template>
  <div
    class="container"
    v-if="url === ''"
  >
    <div class="row mt-2 mb-2">
      <div class="col-xs-12 col-sm-12 col-md-6">
        <label>
          {{ $t('write') }}
        </label>
      </div>
      <div class="col-xs-12 col-sm-12 col-md-6">
        <toggle-button
          v-model="token.write_permission"
          :labels="{checked: 'Yes', unchecked: 'No'}"
        />
      </div>
    </div>

    <div class="row mb-2">
      <div class="col-xs-12 col-sm-12 col-md-6">
        <label>
          {{ $t('download') }}
        </label>
      </div>
      <div class="col-xs-12 col-sm-12 col-md-6">
        <toggle-button
          v-if="token.read_permission"
          v-model="token.download_permission"
          :labels="{checked: 'Yes', unchecked: 'No'}"
        />
      </div>
    </div>
    <div class="row mb-2">
      <div class="col-xs-12 col-sm-12 col-md-6">
        <b>{{ $t('expirationdate') }}</b>
      </div>
      <div class="col-xs-12 col-sm-12 col-md-6">
        <datepicker
          v-model="token.expiration_time"
          :bootstrap-styling="false"
          input-class="form-control form-control-sm  search-calendar"
          :calendar-button="false"
          calendar-button-icon=""
          wrapper-class="calendar-wrapper"
          :placeholder="$t('expirationdate')"
          :clear-button="true"
          clear-button-icon="fa fa-times"
        />
      </div>
    </div>
    <div class="row mb-2">
      <div class="col-xs-12 col-sm-12 col-md-6">
      </div>
      <div class="col-xs-12 col-sm-12 col-md-6">
          <button
            type="submit"
            class="btn btn-primary"
            @click="create"
          >
            {{ $t('create') }}
          </button>
          <button
            type="reset"
            class="btn btn-secondary ml-3"
            @click="cancel"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <div
    class="container"
    v-else
  >
    <div class="row mt-2">
      <div class="col-xs-12 col-sm-12 col-md-12 mb-2">
        Your sharing url :
      </div>
    </div>
    <div class="row">
      <div class="col-xs-12 col-sm-12 col-md-12 mb-2">
        <b-card>
          <b class="mr-2">{{ url }}</b>
          <button
            v-clipboard:copy="url"
            type="button"
            class="btn btn-secondary btn-sm"
          >
            <v-icon
              name="paste"
              scale="1"
            />
          </button>
        </b-card>
      </div>
    </div>
  </div>
</template>
<script>
import Datepicker from 'vuejs-datepicker';
import moment from 'moment';

export default {
  name: 'SharingLink',
  components: { Datepicker },
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
    },
    url: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      token: {
        title: 'sharing_link',
        scope_type: 'album',
        album: this.albumId,
        read_permission: true,
        write_permission: false,
        download_permission: false,
        appropriate_permission: false,
        expiration_time: moment().add(1, 'months').toDate(),
      },
    };
  },
  computed: {
  },
  watch: {
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
    cancel() {
      this.$emit('cancel')
    },
    create() {
      this.token.expiration_time = moment(this.token.expiration_time).format();
      this.$emit('create', this.token)
    }
  },
};
</script>
<style>
div.calendar-wrapper{
  color: #333;
}
</style>