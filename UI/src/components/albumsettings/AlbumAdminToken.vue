<template>
  <div
    v-if="tokens.length > 0 || error === true"
    class="col-12"
  >
    <p
      class="text-warning"
    >
      {{ $t('albumsettings.lastadmintoken') }}
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
        {{ data.item|formatPermissions }}
      </template>
      <template #table-busy>
        <loading />
      </template>
      <template #empty>
        blabla
      </template>
    </b-table>
  </div>
</template>

<script>
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'AlbumAdminToken',
  components: {
    Loading,
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
  },
  data() {
    return {
      tokens: [],
      error: false,
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
          class: 'd-none d-lg-table-cell',
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
          class: 'd-none d-sm-table-cell',
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
        this.error = true;
        Promise.reject(err);
      });
    },
  },
};
</script>
