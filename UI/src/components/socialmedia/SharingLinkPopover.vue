<template>
  <span>
    <div
      v-if="tokens.length === 0"
    >
      <div class="row mt-2 mb-2">
        <div class="col-xs-12 col-sm-12 col-md-8">
          <label>
            {{ $t('sharinglink.write') }}
          </label>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-4">
          <toggle-button
            v-model="token.write_permission"
            :color="{checked: '#5fc04c', unchecked: 'grey'}"
          />
        </div>
      </div>

      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-8">
          <label>
            {{ $t('sharinglink.download') }}
          </label>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-4">
          <toggle-button
            v-if="token.read_permission"
            v-model="token.download_permission"
            :color="{checked: '#5fc04c', unchecked: 'grey'}"
          />
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-8">
          <label>
            {{ $t('sharinglink.addalbum') }}
          </label>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-4">
          <toggle-button
            v-model="token.appropriate_permission"
            :color="{checked: '#5fc04c', unchecked: 'grey'}"
          />
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-6">
          {{ $t('sharinglink.expirationdate') }}
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
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            v-if="loading === false"
            type="submit"
            class="btn btn-primary btn-block"
            @click="create"
          >
            {{ $t('create') }}
          </button>
          <kheops-clip-loader
            v-if="loading === true"
            size="30px"
          />
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            type="reset"
            class="btn btn-secondary btn-block"
            @click="cancel"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </div>
    <div
      v-else
    >
      <span
        v-if="url !== ''"
      >
        <div class="row mt-2">
          <div class="col-xs-12 col-sm-12 col-md-12 mb-2">
            {{ $t('sharinglink.urlsharing') }}
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-12 col-md-12 mb-2">
            <b class="mr-2">{{ url }}</b>
            <button
              v-clipboard:copy="url"
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
        </div>
      </span>
      <div class="row mt-2 mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <label>
            {{ $t('sharinglink.revoke') }}
          </label>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            v-if="loading === false"
            type="submit"
            class="btn btn-danger btn-block"
            @click="revoke"
          >
            {{ $t('revoke') }}
          </button>
          <kheops-clip-loader
            v-if="loading === true"
            size="30px"
          />
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            type="reset"
            class="btn btn-secondary btn-block"
            @click="cancel"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </div>
  </span>
</template>
<script>
import Datepicker from 'vuejs-datepicker';
import moment from 'moment';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'SharingLinkPopover',
  components: { Datepicker, KheopsClipLoader },
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
    tokens: {
      type: Array,
      required: true,
      default: () => [],
    },
    loading: {
      type: Boolean,
      required: true,
      default: false,
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
      toggle_revoke: false,
    };
  },
  methods: {
    cancel() {
      this.$emit('cancel');
    },
    create() {
      this.token.expiration_time = moment(this.token.expiration_time).format();
      this.$emit('create', this.token);
    },
    revoke() {
      this.$emit('revoke', this.tokens);
    },
    onCopy() {
      this.$snotify.success(this.$t('copysuccess'));
    },
    onCopyError() {
      this.$snotify.error(this.$t('sorryerror'));
    },
  },
};
</script>
