<i18n>
{
  "en": {
  },
  "fr": {
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
        {{ $t('newlink') }}
      </span>
    </h4>
    <div class="d-flex flex-row">
      <div class="mt-2">
        <h4>
          {{ $t('links') }}
        </h4>
      </div>
    </div>
    <b-table
      v-if="loadingData === false"
      stacked="sm"
      striped
      hover
      :items="links"
      :fields="fields"
      :sort-desc="true"
      :sort-by.sync="sortBy"
      tbody-tr-class="link"
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
        slot="link"
        slot-scope="data"
      >
        {{ data }}
      </template>
      <template
        slot="status"
        slot-scope="data"
      >
        <div
          v-if="linkStatus(data.item)=='active'"
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
          v-if="linkStatus(data.item)=='revoked'"
          class="text-danger"
        >
          <v-icon
            name="ban"
            class="mr-2"
          />{{ $t("revoked") }}<br>{{ data.item.revoke_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.revoke_time|formatTime }}</small>
        </div>
        <div
          v-if="linkStatus(data.item)=='expired'"
          class="text-danger"
        >
          <v-icon
            name="ban"
            class="mr-2"
          />{{ $t("expired") }}<br>{{ data.item.expiration_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.expiration_time|formatTime }}</small>
        </div>
        <div v-if="linkStatus(data.item)=='wait'">
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
    albumid: {
      type: String,
      required: true,
      default: null,
    },
  },
  data() {
    return {
      loadingData: false,
      showInvalid: false,
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
          key: 'link',
          label: this.$t('description'),
          sortable: true,
        },
        {
          key: 'expiration_time',
          label: this.$t('expiration date'),
          sortable: true,
          class: 'd-none d-sm-table-cell',
        },
        {
          key: 'permission',
          label: this.$t('permission'),
          sortable: true,
          class: 'd-none d-sm-table-cell',
        },
      ],
    };
  },
  computed: {
    ...mapGetters({
      albumTokens: 'albumTokens',
    }),
    links() {
      return this.albumTokens;
    },
  },
  created() {
    this.loadingData = true;
    this.getTokens().then(() => {
      this.loadingData = false;
    });
  },
  methods: {
    getTokens() {
      const queries = {
        valid: !this.showInvalid,
        album: this.albumid,
      };
      return this.$store.dispatch('getAlbumTokens', { queries });
    },
    loadToken(item) {
      const action = 'token';
      const { id } = item;
      this.$router.push({ name: 'albumsettingsactionid', params: { action, id } });
    },
    clickNew() {
      const action = 'newtoken';
      this.$router.push({ name: 'albumsettingsaction', params: { action } });
    },
    linkStatus(itemToken) {
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
