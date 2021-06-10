<!--
Components : AlbumUsers
Props :
  Users           Array
  album           Object
  showDeleteUser  Boolean
  showChangeRole  Boolean
-->
<template>
  <div
    class="wrapper-table"
  >
    <div
      class="wrapper-table-title"
    >
      {{ $t('albumuser.username') }}
    </div>
    <div
      v-for="user in users"
      :key="user.email"
      class="wrapper-table-content"
    >
      <div
        class="wrapper-table-content-flex"
      >
        <div
          class="wrapper-table-content-username"
        >
          {{ user|getUsername }} {{ user.name !== undefined ? `- ( ${user.name} )` : '' }}
          <span
            v-if="user.is_admin"
            class="font-neutral"
          >
            - {{ $t("albumuser.Admin") }}
          </span>
        </div>
        <div
          v-if="album.is_admin"
          class="wrapper-table-content-action"
        >
          <div
            v-if="onloading[user.sub] === true"
          >
            <kheops-clip-loader />
          </div>
          <div
            v-else-if="confirmDelete !== user.email && confirmResetAdmin !== user.email"
          >
            <a
              v-if="showChangeRole"
              class="font-white"
              @click.stop="toggleAdmin(user)"
            >
              {{ (user.is_admin)?$t('albumuser.changeroleuser'):$t("albumuser.changeroleadmin") }}
              <v-icon
                name="user"
              />
            </a>
            <br>
            <a
              v-if="album.is_admin && showDeleteUser && user.sub !== currentuserSub"
              class="text-danger"
              @click.stop="deleteUser(user)"
            >
              {{ $t('albumuser.remove') }}
              <v-icon name="trash" />
            </a>
          </div>
          <div v-else-if="confirmDelete === user.email">
            <span class="text-danger mr-2">
              {{ $t("albumuser.warningtoggledelete") }}
            </span>
            <br>
            <div class="btn-group">
              <button
                type="button"
                class="btn btn-sm btn-danger"
                @click.stop="deleteUser(user)"
              >
                {{ $t('confirm') }}
              </button><button
                type="button"
                class="btn btn-sm btn-secondary"
                @click.stop="confirmDelete=''"
              >
                {{ $t('cancel') }}
              </button>
            </div>
          </div>
          <div v-else-if="confirmResetAdmin === user.email">
            <span class="text-danger mr-2">
              {{ $t("albumuser.warningtoggleadmin") }}
            </span>
            <br>
            <div class="btn-group">
              <button
                type="button"
                class="btn btn-sm btn-danger"
                @click.stop="toggleAdmin(user)"
              >
                {{ $t('confirm') }}
              </button><button
                type="button"
                class="btn btn-sm btn-secondary"
                @click.stop="confirmResetAdmin=''"
              >
                {{ $t('cancel') }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <album-admin-token
        v-if="album.is_admin === true && user.is_admin && (confirmResetAdmin === user.email || confirmDelete === user.email)"
        :warning-message="$t('albumuser.revokeadmintoken')"
        class-name="mt-2"
        :albumid="album.album_id"
        :user="user.email"
      />
    </div>
  </div>
</template>
<script>
import Vue from 'vue';
import mobiledetect from '@/mixins/mobiledetect.js';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';
import AlbumAdminToken from '@/components/albumsettings/AlbumAdminToken';
import { CurrentUser } from '@/mixins/currentuser.js';

export default {
  name: 'AlbumUsers',
  components: { KheopsClipLoader, AlbumAdminToken },
  mixins: [CurrentUser],
  props: {
    album: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    users: {
      type: Array,
      required: true,
      default: () => ([]),
    },
    showDeleteUser: {
      type: Boolean,
      required: true,
      default: true,
    },
    showChangeRole: {
      type: Boolean,
      required: true,
      default: true,
    },
  },
  data() {
    return {
      confirmDelete: '',
      confirmResetAdmin: '',
      onloading: {},
    };
  },
  computed: {
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
    },
  },
  watch: {
    users: {
      handler() {
        this.setOnLoading(this.users);
      },
      deep: true,
    },
  },
  created() {
    this.setOnLoading(this.users);
  },
  methods: {
    setOnLoading(users) {
      this.onloading = {};
      users.forEach((user) => {
        Vue.set(this.onloading, user.sub, false);
      });
    },
    toggleAdmin(user) {
      if (this.currentuserSub === user.sub && !this.confirmResetAdmin) {
        this.confirmResetAdmin = user.email;
        return;
      }
      Vue.set(this.onloading, user.sub, true);

      if (user.is_admin && this.confirmResetAdmin !== user.email) {
        Vue.set(this.onloading, user.sub, false);
        this.confirmResetAdmin = user.email;
        return;
      }

      const params = {
        album_id: this.album.album_id,
        user_name: user.email,
        user_is_admin: !user.is_admin,
      };
      this.$store.dispatch('manageAlbumUserAdmin', params).then((res) => {
        if (res.status === 204) {
          if (this.confirmResetAdmin === user.email) {
            this.getAlbum();
          }
        } else {
          this.$snotify.error(this.$t('sorryerror'));
        }
        this.confirmResetAdmin = '';
        Vue.set(this.onloading, user.sub, false);
      }).catch(() => {
        this.confirmResetAdmin = '';
        this.$snotify.error(this.$t('sorryerror'));
        Vue.set(this.onloading, user.sub, false);
      });
    },
    deleteUser(user) {
      if (this.confirmDelete !== user.email) this.confirmDelete = user.email;
      else {
        Vue.set(this.onloading, user.sub, true);
        const params = {
          album_id: this.album.album_id,
          user: user.email,
        };
        this.$store.dispatch('removeAlbumUser', params).then((res) => {
          if (res.status !== 204) {
            this.$snotify.error(this.$t('sorryerror'));
          }
          this.confirmDelete = '';
          Vue.set(this.onloading, user.sub, false);
        }).catch(() => {
          this.confirmDelete = '';
          this.$snotify.error(this.$t('sorryerror'));
          Vue.set(this.onloading, user.sub, false);
        });
      }
    },
    getAlbum() {
      this.$store.dispatch('getAlbum', { album_id: this.album.album_id }).catch((err) => {
        this.$router.push('/albums');
        return err;
      });
    },
  },
};
</script>
