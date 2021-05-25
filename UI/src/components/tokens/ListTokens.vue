<template>
  <div>
    <div class="d-flex align-content-around flex-wrap">
      <div>
        <h4>
          Tokens
        </h4>
      </div>
      <div class="mt-2 ml-auto">
        <toggle-button
          v-model="showInvalid"
          :color="{checked: '#5fc04c', unchecked: 'grey'}"
          @change="toggleValid"
        />
        <span class="ml-2 toggle-label">
          {{ $t('token.showinvalidtoken') }}
        </span>
      </div>
    </div>
    <b-table
      striped
      hover
      show-empty
      sort-icon-left
      :items="tokens"
      :fields="fields"
      :sort-desc="true"
      :sort-by.sync="sortBy"
      :busy="loadingData"
      tbody-tr-class="link"
      @row-clicked="loadToken"
    >
      <template
        #cell(scope_type)="data"
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
          {{ $t('token.user') }}
        </div>
      </template>
      <template
        #cell(status)="data"
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
          />{{ $t("token.revoked") }}<br>{{ data.item.revoke_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.revoke_time|formatTime }}</small>
        </div>
        <div
          v-if="tokenStatus(data.item)=='expired'"
          class="text-danger"
        >
          <v-icon
            name="ban"
            class="mr-2"
          />{{ $t("token.expired") }}<br>{{ data.item.expiration_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.expiration_time|formatTime }}</small>
        </div>
        <div v-if="tokenStatus(data.item)=='wait'">
          <v-icon
            name="clock"
            class="mr-2"
          /><br>{{ data.item.not_before_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.not_before_time|formatTime }}</small>
        </div>
      </template>
      <template
        #cell(expiration_time)="data"
      >
        <span :class="(data.item.revoked)?'text-danger':''">
          {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
        </span>
      </template>
      <template
        #cell(issued_at_time)="data"
      >
        {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
      </template>
      <template
        #cell(last_used)="data"
      >
        {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
      </template>
      <template
        #cell(permission)="data"
      >
        {{ data.item|formatPermissions }}
      </template>
      <template
        #cell(actions)="data"
      >
        <span
          v-if="data.item.revoked"
          class="text-danger"
        >
          {{ $t('token.revoked') }}
        </span>
        <span
          v-else-if="tokenStatus(data.item)=='expired'"
          class="text-danger"
        >
          {{ $t('token.expired') }}
        </span>
        <span
          v-else
        >
          <button
            v-if="onloading[data.item.id] === undefined || onloading[data.item.id] === false"
            type="button"
            class="btn btn-danger btn-xs"
            @click.stop="revoke(data.item.id)"
          >
            {{ $t('disable') }}
          </button>
          <kheops-clip-loader
            v-else
            :loading="onloading[data.item.id]"
          />
        </span>
      </template>
      <template #table-busy>
        <loading />
      </template>
      <template #empty>
        <div
          class="text-warning text-center"
        >
          <list-empty
            :status="status"
            :text-empty="$t('token.notokens')"
            @reload="getTokens()"
          />
        </div>
      </template>
      <template #emptyfiltered>
        <div
          class="text-warning text-center"
        >
          {{ $t('token.notokens') }}
        </div>
      </template>
    </b-table>
  </div>
</template>

<script>
import Vue from 'vue';
import { mapGetters } from 'vuex';
import Loading from '@/components/globalloading/Loading';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';
import ListEmpty from '@/components/globallist/ListEmpty';
import httpoperations from '@/mixins/httpoperations';
import { validator } from '@/mixins/validator';

export default {
  name: 'ListTokens',
  components: { Loading, ListEmpty, KheopsClipLoader },
  mixins: [validator],
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
      onloading: {},
      sortBy: 'expiration_date',
      fields: [
        {
          key: 'status',
          label: this.$t('token.status'),
          sortable: true,
        },
        {
          key: 'title',
          label: this.$t('token.description'),
          sortable: true,
          tdClass: 'word-break',
        },
        {
          key: 'scope_type',
          label: this.$t('token.scope'),
          sortable: true,
          class: this.scope === 'album' ? 'd-none' : 'd-none d-sm-table-cell',
          tdClass: 'word-break',
        },
        {
          key: 'expiration_time',
          label: this.$t('token.expirationdate'),
          sortable: true,
          class: 'd-none d-lg-table-cell',
        },
        {
          key: 'issued_at_time',
          label: this.$t('token.creationdate'),
          sortable: true,
          class: 'd-none d-lg-table-cell',
        },
        {
          key: 'last_used',
          label: this.$t('token.lastuse'),
          sortable: true,
          class: 'd-none d-lg-table-cell',
        },
        {
          key: 'permission',
          label: this.$t('token.permission'),
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
      status: -1,
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
    this.getTokens();
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
      this.$store.dispatch('setValidParamToken', !this.showInvalid);
    },
    getTokens() {
      this.loadingData = true;
      if (this.scope === 'album' && this.albumid) {
        const queries = {
          valid: !this.showInvalid,
          album: this.albumid,
        };
        return this.$store.dispatch('getAlbumTokens', { queries }).then((res) => {
          const tokens = res.data;
          this.setOnLoading(tokens);
          this.loadingData = false;
          this.status = -1;
        }).catch((err) => {
          this.loadingData = false;
          this.status = httpoperations.getStatusError(err);
          Promise.reject(err);
        });
      }
      return this.$store.dispatch('getUserTokens', { showInvalid: this.showInvalid, album_id: this.albumid }).then((res) => {
        const tokens = res;
        this.setOnLoading(tokens);
        this.loadingData = false;
        this.status = -1;
      }).catch((err) => {
        this.loadingData = false;
        this.status = httpoperations.getStatusError(err);
        Promise.reject(err);
      });
    },
    setOnLoading(tokens) {
      tokens.forEach((token) => {
        Vue.set(this.onloading, token.id, false);
      });
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
    revoke(tokenId) {
      Vue.set(this.onloading, tokenId, true);
      this.$store.dispatch('revokeToken', { token_id: tokenId }).then(() => {
        this.getTokens().then(() => {
          Vue.set(this.onloading, tokenId, false);
        }).catch(() => {
          Vue.set(this.onloading, tokenId, false);
        });
      }).catch(() => {
        Vue.set(this.onloading, tokenId, false);
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
  },
};
</script>
