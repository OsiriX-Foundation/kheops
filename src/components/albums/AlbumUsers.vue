<!--
Components : AlbumUsers
Props :
  Users           Array
  album           Object
  showDeleteUser  Boolean
  showChangeRole  Boolean
-->
<i18n>
{
  "en": {
    "username": "User name",
    "user": "user",
    "changerole": "change role to",
    "albumuserdeletesuccess": "Access to the album has been successfully removed",
    "usernotsettoadmin": "User no longer has admin rights",
    "usersettoadmin": "User has admin rights",
    "warningtoggleadmin": "Warning! Do you really want to revoke your admin role? ",
    "remove": "Remove user",
    "warningtoggledelete": "Do you realy want to delete this user ?",
    "Admin": "Data steward",
    "admin": "data steward"
  },
  "fr": {
    "username": "Utilisateur",
    "user": "utilisateur",
    "changerole": "changer le rôle pour",
    "albumuserdeletesuccess": "L'accès à l'album a été supprimé avec succès",
    "usernotsettoadmin": "L'utilisateur n'a plus de droits admin",
    "usersettoadmin": "L'utilisateur a des droits admin",
    "warningtoggleadmin": "Attention ! Voulez-vous vraiment renoncer à vos droits admin ?  ",
    "remove": "Retirer l'utilisateur",
    "warningtoggledelete": "Voulez-vous vraiment supprimer cet utilisateur ?",
    "Admin": "Gardien des données",
    "admin": "gardien des données"
  }
}
</i18n>
<template>
  <div class="user-table-container">
    <table class="table">
      <thead>
        <tr>
          <th>{{ $t('username') }}</th>
          <th v-if="album.is_admin" />
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="user in users"
          :key="user.email"
        >
          <td>
            {{ user|getUsername }}
            <span
              v-if="user.is_admin"
            >
              -
              <span
                class="font-neutral"
              >
                {{ $t("Admin") }}
              </span>
            </span>
            <!-- on mobile -->
            <div
              v-if="album.is_admin"
              class="d-sm-none"
            >
              <div
                v-if="confirmDelete !== user.email && confirmResetAdmin !== user.email"
                class="user_actions"
              >
                <a
                  v-if="showChangeRole"
                  @click.stop="toggleAdmin(user)"
                >
                  {{ $t('changerole') }} {{ (user.is_admin)?$t('user'):$t("admin") }}
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
                  {{ $t('remove') }}
                  <v-icon name="trash" />
                </a>
              </div>
              <div v-if="confirmDelete === user.email">
                <span class="text-danger mr-2">
                  {{ $t("warningtoggledelete") }}
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
              <div v-if="confirmResetAdmin === user.email">
                <span class="text-danger mr-2">
                  {{ $t("warningtoggleadmin") }}
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
            <!-- end on mobile -->
          </td>
          <td
            v-if="album.is_admin"
            class="text-right d-none d-sm-table-cell"
            :class="mobiledetect ? '' : 'showOnTrHover'"
          >
            <div
              v-if="confirmDelete !== user.email && confirmResetAdmin !== user.email"
              class="user_actions"
            >
              <a
                v-if="showChangeRole"
                class="font-white"
                @click.stop="toggleAdmin(user)"
              >
                {{ $t('changerole') }} {{ (user.is_admin)?$t('user'):$t("admin") }}
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
                {{ $t('remove') }}
                <v-icon name="trash" />
              </a>
            </div>
            <div v-if="confirmDelete === user.email">
              <span class="text-danger mr-2">
                {{ $t("warningtoggledelete") }}
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
            <div v-if="confirmResetAdmin === user.email">
              <span class="text-danger mr-2">
                {{ $t("warningtoggleadmin") }}
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
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
import mobiledetect from '@/mixins/mobiledetect.js';
import { CurrentUser } from '@/mixins/currentuser.js';

export default {
  name: 'AlbumUsers',
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
    };
  },
  computed: {
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
    },
  },
  created() {
  },
  methods: {
    toggleAdmin(user) {
      if (this.currentuserSub === user.sub && !this.confirmResetAdmin) {
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
      }).catch(() => {
        this.confirmResetAdmin = '';
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    deleteUser(user) {
      if (this.confirmDelete !== user.email) this.confirmDelete = user.email;
      else {
        const params = {
          album_id: this.album.album_id,
          user: user.email,
        };
        this.$store.dispatch('removeAlbumUser', params).then((res) => {
          if (res.status !== 204) {
            this.$snotify.error(this.$t('sorryerror'));
          }
          this.confirmDelete = '';
        }).catch(() => {
          this.confirmDelete = '';
          this.$snotify.error(this.$t('sorryerror'));
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
