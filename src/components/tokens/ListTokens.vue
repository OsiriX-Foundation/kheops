<i18n>
{
  "en": {
    "newtoken": "New token",
    "showrevokedtoken": "Show revoked tokens",
    "showinvalidtoken": "Show invalid tokens",
    "revoke": "revoke",
    "revoked": "revoked",
    "active": "active",
    "expired": "expired",
    "revokedsuccess": "revoked successfully",
    "expiration date": "expiration date",
    "status": "status",
    "description": "description",
    "scope": "scope",
    "create date": "create date",
    "last used": "last used",
    "permission": "permission"
  },
  "fr": {
    "newtoken": "Nouveau token",
    "showrevokedtoken": "Afficher les tokens révoqués",
    "showinvalidtoken": "Afficher les tokens invalides",
    "revoke": "révoquer",
    "revoked": "révoqué",
    "active": "actif",
    "expired": "expiré",
    "revokedsuccess": "révoqué avec succès",
    "expiration date": "date d'expiration",
    "status": "statut",
    "description": "description",
    "scope": "application",
    "create date": "créé le",
    "last used": "dern. utilisation",
    "permission": "permission"
  }
}
</i18n>
<template>
  <div>
    <h4>
      <span
        class="link"
        @click="clickNew()"
      >
        <v-icon
          name="plus"
          scale="1"
          class="mr-3"
        />
        {{ $t('newtoken') }}
      </span>
    </h4>
    <div class="d-flex flex-row">
      <div class="mt-2">
        <h4>
          Tokens
        </h4>
      </div>
      <div class="mt-2 ml-auto">
        <toggle-button
          v-model="showInvalid"
          :labels="{checked: 'Yes', unchecked: 'No'}"
          @change="toggleValid"
        />
        <span class="ml-2 toggle-label">
          {{ $t('showinvalidtoken') }}
        </span>
      </div>
    </div>

    <b-table
      v-if="loadingData === false"
      stacked="sm"
      striped
      hover
      :items="tokens"
      :fields="fields"
      :sort-desc="true"
      :sort-by.sync="sortBy"
      tbody-tr-class="link"
      @row-clicked="loadToken"
    >
      <template
        slot="scope_type"
        slot-scope="data"
      >
        <div v-if="data.value=='album'">
          <router-link
            :to="`/albums/${data.item.album.album_id}`"
            @click.stop
          >
            <v-icon
              name="book"
              class="mr-2"
            />
            {{ data.item.album.name }}
          </router-link>
        </div>
        <div v-if="data.value=='user'">
          <v-icon
            name="user"
            class="mr-2"
          />
          {{ $t('user') }}
        </div>
      </template>
      <template
        slot="status"
        slot-scope="data"
      >
        <div
          v-if="tokenStatus(data.item)=='active'"
          class="text-success"
        >
          <span class="nowrap">
            <v-icon
              name="check-circle"
              class="mr-2"
            />{{ $t("active") }}
          </span>
        </div>
        <div
          v-if="tokenStatus(data.item)=='revoked'"
          class="text-danger"
        >
          <v-icon
            name="ban"
            class="mr-2"
          />{{ $t("revoked") }}<br>{{ data.item.revoke_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.revoke_time|formatTime }}</small>
        </div>
        <div
          v-if="tokenStatus(data.item)=='expired'"
          class="text-danger"
        >
          <v-icon
            name="ban"
            class="mr-2"
          />{{ $t("expired") }}<br>{{ data.item.expiration_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.expiration_time|formatTime }}</small>
        </div>
        <div v-if="tokenStatus(data.item)=='wait'">
          <v-icon
            name="clock"
            class="mr-2"
          /><br>{{ data.item.not_before_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.not_before_time|formatTime }}</small>
        </div>
      </template>
      <template
        slot="expiration_time"
        slot-scope="data"
      >
        <span :class="(data.item.revoked)?'text-danger':''">
          {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
        </span>
      </template>
      <template
        slot="issued_at_time"
        slot-scope="data"
      >
        {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
      </template>
      <template
        slot="last_used"
        slot-scope="data"
      >
        {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
      </template>
      <template
        slot="permission"
        slot-scope="data"
      >
        {{ data.item|formatPermissions }}
      </template>
      <template
        slot="actions"
        slot-scope="data"
      >
        <button
          v-if="!data.item.revoked"
          type="button"
          class="btn btn-danger btn-xs"
          @click.stop="revoke(data.item.id)"
        >
          {{ $t('revoke') }}
        </button>
        <span
          v-if="data.item.revoked"
          class="text-danger"
        >
          {{ $t('revoked') }}
        </span>
      </template>
    </b-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import moment from 'moment';

export default {
  name: 'ListTokens',
  components: { },
  props: {
    scope: {
      type: String,
      required: true,
      default: '',
    },
    albumid: {
      type: String,
      required: false,
      default: null,
    },
  },
  data() {
    return {
      showInvalid: false,
      loadingData: false,
      sortBy: 'expiration_date',
      fields: [
        {
          key: 'status',
          label: this.$t('status'),
          sortable: true,
        },
        {
          key: 'title',
          label: this.$t('description'),
          sortable: true,
        },
        {
          key: 'scope_type',
          label: this.$t('scope'),
          sortable: true,
          class: this.scope === 'album' ? 'd-none' : 'd-none d-sm-table-cell',
        },
        {
          key: 'expiration_time',
          label: this.$t('expiration date'),
          sortable: true,
          class: 'd-none d-sm-table-cell',
        },
        {
          key: 'issued_at_time',
          label: this.$t('create date'),
          sortable: true,
          class: 'd-none d-md-table-cell',
        },
        {
          key: 'last_used',
          label: this.$t('last used'),
          sortable: true,
          class: 'd-none d-md-table-cell',
        },
        {
          key: 'permission',
          label: this.$t('permission'),
          sortable: true,
          class: 'd-none d-sm-table-cell',
        },
        {
          key: 'actions',
          label: '',
          sortable: false,
        },
      ],
      routername: {
        actionid: '',
        action: '',
      },
    };
  },
  computed: {
    ...mapGetters({
      user: 'currentUser',
      albumTokens: 'albumTokens',
    }),
    tokens() {
      let tokens = [];
      if (this.scope === 'user') {
        tokens = this.user.tokens;
      } else if (this.scope === 'album') {
        tokens = this.albumTokens;
      }
      return tokens;
    },
  },
  created() {
    this.loadingData = true;
    this.initRouterName();
    this.getTokens().then(() => {
      this.loadingData = false;
    });
  },
  beforeDestroy() {
    this.$store.dispatch('initValidParamToken');
  },
  methods: {
    initRouterName() {
      this.routername.actionid = this.scope === 'album' ? 'albumsettingsactionid' : 'useractionid';
      this.routername.action = this.scope === 'album' ? 'albumsettingsaction' : 'useraction';
    },
    toggleValid() {
      this.getTokens();
      this.$store.commit('setValidParamToken', !this.showInvalid);
    },
    getTokens() {
      if (this.scope === 'album' && this.albumid) {
        const queries = {
          valid: !this.showInvalid,
          album: this.albumid,
        };
        return this.$store.dispatch('getAlbumTokens', { queries });
      }
      return this.$store.dispatch('getUserTokens', { showInvalid: this.showInvalid, album_id: this.albumid });
    },
    loadToken(item) {
      const action = 'token';
      const { id } = item;
      this.$router.push({ name: this.routername.actionid, params: { action, id } });
    },
    clickNew() {
      const action = 'newtoken';
      this.$router.push({ name: this.routername.action, params: { action } });
    },
    tokenStatus(itemToken) {
      if (itemToken.revoked) {
        return 'revoked';
      } if (moment(itemToken.not_before_time) > moment()) {
        return 'wait';
      } if (moment(itemToken.expiration_time) < moment()) {
        return 'expired';
      }
      return 'active';
    },
    revoke(tokenId) {
      this.$store.dispatch('revokeToken', { token_id: tokenId }).then((res) => {
        this.$snotify.success(`token ${res.data.title} ${this.$t('revokedsuccess')}`);
        this.getTokens();
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
  },
};
</script>
