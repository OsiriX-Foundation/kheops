<i18n>
{
  "en": {
    "newtoken": "New token",
    "description": "Description",
    "scope": "Scope",
    "album": "album",
    "permission": "Permission",
    "write": "write",
    "read": "read",
    "download": "show download button",
    "appropriate": "add to album / inbox",
    "expirationdate": "Expiration date",
    "tokencopysuccess": "Token successfully copied"

  },
  "fr": {
    "newtoken": "Nouveau token",
    "description": "Description",
    "scope": "Applicable à",
    "album": "album",
    "permission": "Permission",
    "write": "écriture",
    "read": "lecture",
    "download": "montrer le bouton de téléchargement",
    "appropriate": "ajouter à un album / inbox",
    "expirationdate": "Date d'expiration",
    "tokencopysuccess": "Token copié avec succès"
  }
}
</i18n>

<template>
  <div id="newToken">
    <div
      class="my-3 selection-button-container token-position"
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
        {{ $t('newtoken') }}
      </h4>
    </div>
    <form @submit.prevent="createToken">
      <fieldset>
        <div class="row">
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('description') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-10 mb-3">
            <input
              v-model="token.title"
              type="text"
              :placeholder="$t('description')"
              class="form-control"
              required
              maxlength="255"
            >
          </div>
        </div>
        <div
          v-if="scope!=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('scope') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-4 mb-3">
            <select
              v-model="token.scope_type"
              class="form-control"
            >
              <option
                v-for="(option_scope,idx) in scopes"
                :key="idx"
                :value="option_scope"
              >
                {{ $t(option_scope) }}
              </option>
            </select>
          </div>
        </div>
        <div
          v-if="token.scope_type==='album' && scope!=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('album') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-4 mb-3">
            <select
              v-model="token.album"
              class="form-control"
            >
              <option
                v-for="album in albums"
                :key="album.album_id"
                :value="album.album_id"
              >
                {{ album.name }}
              </option>
            </select>
          </div>
        </div>
        <div
          v-if="token.scope_type=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('permission') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-10">
            <toggle-button
              v-model="token.write_permission"
              :labels="{checked: 'Yes', unchecked: 'No'}"
            />
            <label
              class="token-props"
            >
              {{ $t('write') }}
            </label><br>
            <toggle-button
              v-model="token.read_permission"
              :labels="{checked: 'Yes', unchecked: 'No'}"
            />
            
            <label
              class="token-props"
            >
              {{ $t('read') }}
            </label><br>
            <toggle-button
              v-if="token.read_permission"
              v-model="token.download_permission"
              :labels="{checked: 'Yes', unchecked: 'No'}"
              class="ml-3"
            /> <label v-if="token.read_permission">
              {{ $t('download') }}
            </label><br>
            <toggle-button
              v-if="token.read_permission"
              v-model="token.appropriate_permission"
              :labels="{checked: 'Yes', unchecked: 'No'}"
              class="ml-3"
            /> <label v-if="token.read_permission">
              {{ $t('appropriate') }}
            </label>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('expirationdate') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-10 mb-3">
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
        <div class="row">
          <div class="offset-md-2 col-md-10 mb-1 d-none d-sm-none d-md-block">
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="disabledCreateToken"
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
          <div class="col-12 d-md-none">
            <button
              type="submit"
              class="btn btn-primary btn-block"
              :disabled="disabledCreateToken"
            >
              {{ $t('create') }}
            </button>
          </div>
        </div>
      </fieldset>
    </form>

    <b-modal
      id="tokenModal"
      ref="tokenModal"
      centered
      no-fade
      hide-footer
      no-close-on-backdrop
      size="lg"
      @hidden="cancel"
    >
      <dl class="my-2 row">
        <dt class="col-xs-12 col-sm-3 token-title">
          {{ token.title }}
        </dt>
        <dd class="col-xs-10 col-sm-8">
          <input
            v-model="token.access_token"
            type="text"
            readonly
            class="form-control form-control-sm"
          >
        </dd>
        <div class="col-xs-2 col-sm-1 pointer">
          <button
            v-clipboard:copy="token.access_token"
            v-clipboard:success="onCopy"
            v-clipboard:error="onCopyError"
            type="button"
            class="btn btn-secondary btn-sm"
          >
            <v-icon
              name="paste"
              scale="1"
            />
          </button>
        </div>
      </dl>
    </b-modal>
  </div>
</template>

<script>
import moment from 'moment';
import Datepicker from 'vuejs-datepicker';
import { mapGetters } from 'vuex';

export default {
  name: 'NewToken',
  components: { Datepicker },
  props: {
    scope: {
      type: String,
      required: true,
    },
    albumid: {
      type: String,
      required: false,
      default: null,
    },
  },
  data() {
    return {
      token: {
        title: '',
        scope_type: this.scope,
        album: this.albumid,
        access_token: '',
        read_permission: false,
        write_permission: false,
        appropriate_permission: false,
        download_permission: false,
        not_before_time: moment().toDate(),
        expiration_time: moment().add(1, 'months').toDate(),
      },
      scopes: ['user', 'album'],
    };
  },
  computed: {
    ...mapGetters({
      albums: 'albums',
    }),
    disabledCreateToken() {
      return !this.token.title || (this.token.scope_type === 'album' && !this.token.album) || (this.token.scope_type === 'album' && !this.token.read_permission && !this.token.write_permission);
    },
  },
  created() {
    this.$store.dispatch('getAlbums', { queries: { canCreateCapabilityToken: 'true' } });
  },
  destroyed() {
    if (this.albums.length > 0) {
      this.$store.dispatch('initAlbums', {});
    }
  },
  methods: {
    createToken() {
      if (this.token.scope_type !== 'album') {
        this.token.read_permission = false;
        this.token.write_permission = false;
      }
      if (!this.token.read_permission) {
        this.token.download_permission = false;
        this.token.appropriate_permission = false;
      }
      const { token } = this;
      token.expiration_time = moment(this.token.expiration_time).format();
      token.not_before_time = moment(this.token.not_before_time).format();
      this.$store.dispatch('createToken', { token }).then((res) => {
        this.token.access_token = res.data.access_token;
        this.$snotify.success('token created successfully');
        this.$refs.tokenModal.show();
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    onCopy() {
      this.$snotify.success(this.$t('tokencopysuccess'));
    },
    onCopyError() {
      this.$snotify.error(this.$t('sorryerror'));
    },
    cancel() {
      this.$refs.tokenModal.hide();
      this.$emit('done');
    },
  },
};
</script>
