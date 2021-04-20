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
        {{ $t('token.newtoken') }}
      </h4>
    </div>
    <form @submit.prevent="createToken">
      <fieldset>
        <div class="row">
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('token.description') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-10 mb-3">
            <input
              v-model="token.title"
              v-focus
              type="text"
              :placeholder="$t('token.description')"
              class="form-control"
              required
              maxlength="255"
            >
            <field-obligatory
              :state="token.title !== ''"
            />
          </div>
        </div>
        <div
          v-if="scope!=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('token.scope') }}</b>
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
                {{ $t(`token.${option_scope}`) }}
              </option>
            </select>
            <field-obligatory
              :state="token.scope_type !== ''"
            />
          </div>
        </div>
        <div
          v-if="token.scope_type==='album' && scope!=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('token.album') }}</b>
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
            <field-obligatory
              :state="token.album !== ''"
            />
          </div>
        </div>
        <div
          v-if="token.scope_type=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('token.permission') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-10">
            <toggle-button
              v-model="permissions.write_permission"
              :color="{checked: '#5fc04c', unchecked: 'grey'}"
            />
            <label
              class="token-props"
            >
              {{ $t('token.write') }}
            </label><br>
            <toggle-button
              v-model="permissions.read_permission"
              :color="{checked: '#5fc04c', unchecked: 'grey'}"
            />

            <label
              class="token-props"
            >
              {{ $t('token.read') }}
            </label>
            <field-obligatory
              class="mb-3"
              :state="warningPermissions"
              :text="$t('token.warningpermissions')"
            />
            <span
              v-if="permissions.read_permission"
            >
              <br>
              <toggle-button
                v-if="permissions.read_permission"
                v-model="permissions.download_permission"
                :color="{checked: '#5fc04c', unchecked: 'grey'}"
                class="ml-3"
              />
              <label
                v-if="permissions.read_permission"
                class="token-props"
              >
                {{ $t('token.download') }}
              </label> <br>
              <toggle-button
                v-if="permissions.read_permission"
                v-model="permissions.appropriate_permission"
                :color="{checked: '#5fc04c', unchecked: 'grey'}"
                class="ml-3"
              />
              <label
                v-if="permissions.read_permission"
                class="token-props"
              >
                {{ $t('token.appropriate') }}
              </label>
            </span>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>{{ $t('token.expirationdate') }}</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-10 mb-3">
            <datepicker
              v-model="token.expiration_time"
              :bootstrap-styling="false"
              input-class="form-control form-control-sm  search-calendar"
              :calendar-button="false"
              calendar-button-icon=""
              wrapper-class="calendar-wrapper"
              :placeholder="$t('token.expirationdate')"
              :clear-button="true"
              clear-button-icon="fa fa-times"
            />
            <field-obligatory
              :state="token.expiration_time !== ''"
            />
          </div>
        </div>
        <!--
        <div
          v-if="permissionsSummary.length > 0"
          class="row"
        >
          <div class="col-xs-12 col-sm-12 col-md-2 mb-1">
            <b>Summary</b>
          </div>
          <div class="col-xs-12 col-sm-12 col-md-10 mb-1">
            <b>{{ $t('user_permission') }}</b> <br>
            <ul>
              <li
                v-for="(value, id) in permissionsSummary"
                :key="id"
              >
                {{ value }}
              </li>
            </ul>
          </div>
        </div>
        -->
        <create-cancel-button
          :disabled="disabledCreateToken"
          :loading="oncreate"
          class-col="offset-md-2 col-md-10"
          @cancel="cancel"
        />
      </fieldset>
    </form>

    <b-modal
      id="tokenModal"
      ref="tokenModal"
      header-class="bg-primary"
      title-class="word-break"
      body-class="bg-secondary"
      centered
      no-fade
      hide-footer
      no-close-on-backdrop
      size="lg"
      @hidden="cancel"
    >
      <template #modal-title>
        {{ token.title }}
      </template>
      <dl class="my-2 row">
        <dt class="col-12 text-warning font-large">
          {{ $t('token.warning') }}
        </dt>
      </dl>
      <dl
        v-if="token.scope_type === 'album'"
        class="my-2 row"
      >
        <dt class="col-xs-12 col-sm-3 token-title">
          {{ $t('token.urlvalue') }}
        </dt>
        <dd class="col-xs-10 col-sm-8">
          <input
            v-model="sharingurl"
            type="text"
            readonly
            class="form-control form-control-sm"
          >
        </dd>
        <div class="col-xs-2 col-sm-1 pointer">
          <button
            v-clipboard:copy="sharingurl"
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
      <dl class="my-2 row">
        <dt class="col-xs-12 col-sm-3 token-title">
          {{ $t('token.tokenvalue') }}
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
import CreateCancelButton from '@/components/globalbutton/CreateCancelButton';
import FieldObligatory from '@/components/globals/FieldObligatory';

export default {
  name: 'NewToken',
  components: { Datepicker, CreateCancelButton, FieldObligatory },
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
      albumsKey: 'canCreateCapabilityToken',
      token: {
        title: '',
        scope_type: this.scope,
        album: this.albumid,
        access_token: '',
        not_before_time: moment().toDate(),
        expiration_time: moment().add(1, 'months').toDate(),
      },
      sharingurl: '',
      permissions: {
        write_permission: false,
        read_permission: false,
        download_permission: false,
        appropriate_permission: false,
      },
      scopes: ['user', 'album'],
      oncreate: false,
    };
  },
  computed: {
    albums() {
      return this.$store.getters.getAlbumsByKey(this.albumsKey);
    },
    disabledCreateToken() {
      return (
        !this.token.title
        || (this.token.scope_type === 'album' && !this.token.album)
        || (this.token.scope_type === 'album' && !this.permissions.read_permission && !this.permissions.write_permission)
        || this.oncreate
      );
    },
    warningPermissions() {
      return (this.token.scope_type === 'album' && (this.permissions.read_permission || this.permissions.write_permission));
    },
    permissionsSummary() {
      const summary = [];
      Object.keys(this.permissions).forEach((key) => {
        if (this.permissions[key] === true) {
          summary.push(this.$t(key));
          if (key === 'write_permission' && this.permissions.read_permission === true) {
            summary.push(this.$t('delete_permission'));
          }
        }
      });
      return summary;
    },
  },
  watch: {
    permissions: {
      handler() {
        if (this.permissions.read_permission === false) {
          this.permissions.download_permission = false;
          this.permissions.appropriate_permission = false;
        }
      },
      deep: true,
    },
  },
  created() {
    const queries = { canCreateCapabilityToken: true };
    this.$store.dispatch('getAlbums', { queries, key: this.albumsKey });
  },
  destroyed() {
    if (this.albums.length > 0) {
      this.$store.dispatch('initAlbums', { key: this.albumsKey });
    }
  },
  methods: {
    createToken() {
      this.oncreate = true;
      if (this.token.scope_type !== 'album') {
        this.permissions.read_permission = false;
        this.permissions.write_permission = false;
      }
      if (!this.permissions.read_permission) {
        this.permissions.download_permission = false;
        this.permissions.appropriate_permission = false;
      }
      const token = { ...this.token, ...this.permissions };
      token.expiration_time = moment(this.token.expiration_time).format();
      token.not_before_time = moment(this.token.not_before_time).format();
      delete token.access_token;
      this.$store.dispatch('createToken', { token }).then((res) => {
        this.token.access_token = res.data.access_token;
        this.sharingurl = `${process.env.VUE_APP_URL_ROOT}/view/${res.data.access_token}`;
        this.$refs.tokenModal.show();
      }).catch(() => {
        this.oncreate = false;
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    onCopy() {
      this.$snotify.success(this.$t('copysuccess'));
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
