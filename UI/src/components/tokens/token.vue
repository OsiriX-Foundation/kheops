<template>
  <div class="token">
    <div
      class="my-3 token-position"
    >
      <h4
        class="row word-break mb-3"
      >
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
        {{ token.title }}
      </h4>
    </div>
    <p
      v-if="token.revoked"
      class="py-3 text-danger"
    >
      {{ $t('token.thistokenrevoked') }}
    </p>
    <div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.scope') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ token.scope_type }}
          </dd>
        </div>
      </div>
      <div
        v-if="token.scope_type=='album'"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.album') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-9 word-break">
          <dd>
            <router-link
              :to="`/albums/${token.album.album_id}`"
            >
              {{ token.album.name }}
            </router-link>
          </dd>
        </div>
      </div>
      <div
        v-if="token.scope_type=='album'"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.createdby') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-9 word-break">
          <dd>
            {{ token.created_by|getUsername }}
          </dd>
        </div>
      </div>
      <div
        v-if="token.scope_type=='album'"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.permission') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>{{ permissions }}</dd>
        </div>
      </div>
      <div
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.tokenid') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>{{ token.id }}</dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.expirationdate') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.expiration_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.startdate') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.not_before_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.creationdate') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.issued_at_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div
        v-if="token.last_used"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.lastuse') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.last_used|formatDateTime }}
          </dd>
        </div>
      </div>
      <div
        v-if="token.revoked"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('token.revokeddate') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd class="text-danger">
            {{ token.revoke_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <done-delete-button
        v-if="tokenStatus(token) !== 'revoked' && tokenStatus(token) !== 'expired'"
        class-row="mt-2"
        class-col="offset-md-3 col-sm-12 col-md-4 col-lg-2 d-block"
        class-col-warning-remove="offset-md-3 col-12 col-md-9"
        :text-warning-remove="$t('token.warningrevoke')"
        :text-button-remove="$t('revoke')"
        :show-done="false"
        :loading="onloading"
        @remove="revoke"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import DoneDeleteButton from '@/components/globalbutton/DoneDeleteButton';
import { validator } from '@/mixins/validator';

export default {
  name: 'Token',
  components: { DoneDeleteButton },
  mixins: [validator],
  data() {
    return {
      confirmRevoke: false,
      onloading: false,
    };
  },
  computed: {
    ...mapGetters({
      token: 'token',
    }),
    permissions() {
      const perms = [];

      Object.keys(this.token).forEach((key) => {
        if (key.indexOf('permission') > -1 && this.token[key] === true) {
          perms.push(this.$t(`token.${key.replace('_permission', '')}`));
        }
      });
      return perms.length ? perms.join(', ') : '-';
    },
    tokenId() {
      return this.$route.params.id;
    },
  },
  created() {
    this.$store.dispatch('initToken');
    const capabilityId = this.defineCapabilityId();
    this.$store.dispatch('getToken', { capabilityId }).then((res) => {
      if (res.status !== 200) {
        this.redirect('listtokens');
        this.$snotify.error('Sorry, an error occurred');
      }
    }).catch(() => {
      this.redirect('listtokens');
      this.$snotify.error('Sorry, an error occurred');
    });
  },
  methods: {
    redirect(action) {
      this.$router.push({ name: 'albumsettingsaction', params: { action } });
    },
    defineCapabilityId() {
      const tokenId = this.tokenId === '0' ? this.$route.query.object : this.tokenId;
      return tokenId;
    },
    revoke() {
      this.onloading = true;
      this.$store.dispatch('revokeToken', { token_id: this.tokenId }).then(() => {
        this.cancel();
      }).catch(() => {
        this.onloading = false;
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    cancel() {
      this.$emit('done');
    },
  },
};
</script>
