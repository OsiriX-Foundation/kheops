<template>
  <div
    v-if="tokens.length > 0 || status > -1"
    :class="`col-12 ${className}`"
  >
    <p
      class="text-warning"
    >
      {{ warningMessage }}
    </p>

    <b-table
      show-empty
      :items="tokens"
      :fields="fields"
      :busy="!isLoading"
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
        #cell(expiration_time)="data"
      >
        {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
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
        {{ formatPermissions(data.item) }}
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
    </b-table>
  </div>
</template>

<script>
import Loading from '@/components/globalloading/Loading';
import ListEmpty from '@/components/globallist/ListEmpty';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'AlbumAdminToken',
  components: {
    Loading, ListEmpty,
  },
  mixins: [],
  props: {
    user: {
      type: String,
      required: true,
      default: '',
    },
    albumid: {
      type: String,
      required: false,
      default: null,
    },
    className: {
      type: String,
      required: false,
      default: '',
    },
    warningMessage: {
      type: String,
      required: false,
      default: '',
    },
  },
  data() {
    return {
      tokens: [],
      status: -1,
      isLoading: false,
      fields: [
        {
          key: 'title',
          label: this.$t('token.description'),
          tdClass: 'word-break',
        },
        {
          key: 'expiration_time',
          label: this.$t('token.expirationdate'),
          class: 'd-none d-sm-table-cell',
        },
        {
          key: 'issued_at_time',
          label: this.$t('token.creationdate'),
          class: 'd-none d-lg-table-cell',
        },
        {
          key: 'last_used',
          label: this.$t('token.lastuse'),
          class: 'd-none d-lg-table-cell',
        },
        {
          key: 'permission',
          label: this.$t('token.permission'),
        },
        {
          key: 'actions',
          label: '',
        },
      ],
    };
  },
  computed: {
  },
  created() {
    this.getTokens();
  },
  methods: {
    getTokens() {
      const queries = {
        valid: true,
        user: this.user,
        album: this.albumid,
      };
      this.isLoading = false;
      this.$store.dispatch('getAlbumTokens', { queries }).then((res) => {
        this.tokens = res.data;
        this.isLoading = true;
      }).catch((err) => {
        this.isLoading = true;
        this.status = httpoperations.getStatusError(err);
        Promise.reject(err);
      });
    },
    formatPermissions(items) {
      const perms = [];
      Object.keys(items).forEach((key) => {
        if (key.indexOf('permission') > -1 && items[key] === true) {
          perms.push(this.$t(`token.${key.replace('_permission', '')}`));
        }
      });
      return perms.length ? perms.join(', ') : '-';
    },
  },
};
</script>
