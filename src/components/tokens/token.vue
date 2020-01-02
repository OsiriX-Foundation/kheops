<i18n>
{
  "en": {
    "token": "token",
    "description": "description",
    "scope": "scope",
    "album": "album",
    "permission": "permission",
    "write": "write",
    "read": "read",
    "download": "show download button",
    "appropriate": "send to user / album",
    "expirationdate": "expiration date",
    "startdate": "start date",
    "creationdate": "creation date",
    "revokeddate": "revoke date",
    "revoke": "Revoke",
    "thistokenrevoked": "this token is revoked",
    "lastuse": "last use date",
    "back": "back",
    "warningrevoke": "Are you sure you want to revoke this token ?",
    "cancel": "Cancel",
    "confirm": "Confirm",
    "createdby": "Created by"
  },
  "fr": {
    "token": "token",
    "description": "description",
    "scope": "applicable à",
    "album": "album",
    "permission": "permission",
    "write": "écriture",
    "read": "lecture",
    "download": "bouton téléchargement",
    "appropriate": "envoyer à un utlisateur ou album",
    "expirationdate": "date d'expiration",
    "startdate": "date de début",
    "creationdate": "date de création",
    "revokeddate": "date de révoquation",
    "revoke": "Révoquer",
    "thistokenrevoked": "ce token a été revoqué",
    "lastuse": "dernière utilisation",
    "back": "retour",
    "warningrevoke": "Etes-vous sûr de vouloir revoquer ce token ?",
    "cancel": "Cancel",
    "confirm": "Confirm",
    "createdby": "Créé par"
  }
}
</i18n>

<template>
  <div class="token">
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
        {{ token.title }}
      </h4>
    </div>
    <p
      v-if="token.revoked"
      class="py-3 text-danger"
    >
      {{ $t('thistokenrevoked') }}
    </p>
    <div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('scope') }}
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
            {{ $t('album') }}
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
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('createdby') }}
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
            {{ $t('permission') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>{{ permissions }}</dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt class="token-title">
            {{ $t('expirationdate') }}
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
            {{ $t('startdate') }}
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
            {{ $t('creationdate') }}
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
            {{ $t('lastuse') }}
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
            {{ $t('revokeddate') }}
          </dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd class="text-danger">
            {{ token.revoke_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div class="row mt-3">
        <div class="offset-sm-3 col-sm-9 d-none d-sm-none d-md-block">
          <button
            v-if="!token.revoked && !confirmRevoke"
            type="button"
            class="btn btn-danger"
            @click="revoke"
          >
            {{ $t('revoke') }}
          </button>
          <p
            v-if="!token.revoked && confirmRevoke"
          >
            {{ $t('warningrevoke') }}
          </p>
          <button
            v-if="!token.revoked && confirmRevoke"
            type="button"
            class="btn btn-danger"
            @click="revoke"
          >
            {{ $t('confirm') }}
          </button>
          <button
            v-if="!token.revoked && confirmRevoke"
            type="button"
            class="btn btn-secondary"
            @click="confirmRevoke=false"
          >
            {{ $t('cancel') }}
          </button>
        </div>

        <div class="col-12 d-md-none">
          <button
            v-if="!token.revoked && !confirmRevoke"
            type="button"
            class="btn btn-danger btn-block"
            @click="revoke"
          >
            {{ $t('revoke') }}
          </button>
          <p
            v-if="!token.revoked && confirmRevoke"
          >
            {{ $t('warningrevoke') }}
          </p>
          <button
            v-if="!token.revoked && confirmRevoke"
            type="button"
            class="btn btn-danger btn-block"
            @click="revoke"
          >
            {{ $t('confirm') }}
          </button>
          <button
            v-if="!token.revoked && confirmRevoke"
            type="button"
            class="btn btn-secondary btn-block"
            @click="confirmRevoke=false"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  name: 'Token',
  props: {
  },
  data() {
    return {
      confirmRevoke: false,
    };
  },
  computed: {
    ...mapGetters({
      token: 'token',
    }),
    permissions() {
      const perms = [];
      _.forEach(this.token, (value, key) => {
        if (key.indexOf('permission') > -1 && value) {
          perms.push(key.replace('_permission', ''));
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
        this.$snotify.error('Sorry, an error occur');
      }
    }).catch(() => {
      this.redirect('listtokens');
      this.$snotify.error('Sorry, an error occur');
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
      if (this.confirmRevoke === false) {
        this.confirmRevoke = true;
      } else {
        this.$store.dispatch('revokeToken', { token_id: this.tokenId }).then(() => {
          this.cancel();
        }).catch(() => {
          this.$snotify.error(this.$t('sorryerror'));
        });
      }
    },
    cancel() {
      this.$emit('done');
    },
  },
};
</script>
