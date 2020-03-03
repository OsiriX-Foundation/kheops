<!--
Components : AlbumButtons
Props :
  Users       Array
  album       Object
  showQuit    Boolean
  showDelete  Boolean
-->
<i18n>
{
  "en":{
    "albumdeletesuccess": "Album deleted successfully",
    "albumquitsuccess": "Album quit successfully",
    "delete": "Delete album",
    "quit": "Leave album",
    "confirm": "Confirm",
    "cancel": "Cancel",
    "lastuser": "You are the last user in the album, if you quit, the album is delete.",
    "delalbum": "Are you sure you want to delete the album?",
    "quitalbum": "Are you sure you want to leave the album?",
    "lastadmin": "You are the last admin, you can choose to define a new admin or let the album without admin."
  },
  "fr": {
    "albumdeletesuccess": "Album supprimé avec succès",
    "albumquitsuccess": "Album quitté avec succès",
    "delete": "Effacer l'album",
    "quit": "Quitter l'album",
    "confirm": "Confirmer",
    "cancel": "Annuler",
    "lastuser": "Vous êtes le dernier utilisateur, si vous quittez l'album, il sera supprimé.",
    "delalbum": "Etes-vous sûr de vouloir supprimer l'album ?",
    "quitalbum": "Etes-vous sûr de vouloir quitter l'album ?",
    "lastadmin": "Vous êtes le dernier administrateur, vous pouvez définir un nouveau administrateur ou laisser l'album sans."
  }
}
</i18n>

<template>
  <div
    v-if="onloading === true"
    class="btnalbum offset-md-8 offset-lg-9 col-md-4 col-lg-3"
  >
    <kheops-clip-loader
      :size="'40px'"
      color="white"
    />
  </div>
  <div
    v-else
    class="container"
  >
    <div
      v-if="showQuit && confirmQuit"
      class="row"
    >
      <div
        class="btnalbum offset-lg-4 col-lg-8 d-md-block text-md-right"
      >
        <p v-if="confirmQuit && !lastAdmin && !lastUser">
          {{ $t("quitalbum") }}
        </p>
        <p v-else-if="confirmQuit && lastUser">
          {{ $t('lastuser') }}
        </p>
      </div>
    </div>
    <div
      v-if="showQuit"
      class="row"
    >
      <div
        v-if="album.is_admin && lastAdmin && confirmQuit && listUsers.length > 0"
        class="col-12"
      >
        <p
          class="text-warning"
        >
          {{ $t('lastadmin') }}
        </p>
        <album-users
          :album="album"
          :users="listUsers"
          :show-delete-user="false"
          :show-change-role="true"
        />
      </div>

      <div
        class="btnalbum offset-md-8 offset-lg-9 col-md-4 col-lg-3"
      >
        <button
          type="button"
          class="btn btn-danger btn-block"
          @click="quitAlbum"
        >
          {{ confirmQuit?$t('confirm'):$t('quit') }}
        </button>
        <button
          v-if="confirmQuit"
          type="button"
          class="btn btn-secondary btn-block"
          @click="confirmQuit=!confirmQuit"
        >
          {{ $t('cancel') }}
        </button>
      </div>
    </div>
    <div
      v-if="showDelete && confirmDeletion"
      class="row"
    >
      <div
        class="btnalbum offset-md-4 offset-lg-6 col-12 col-md-8 col-lg-6"
      >
        <p
          v-if="confirmDeletion"
          class="text-md-right"
        >
          {{ $t("delalbum") }}
        </p>
      </div>
    </div>
    <div
      v-if="showDelete"
      class="row"
    >
      <div
        class="btnalbum offset-md-8 offset-lg-9 col-md-4 col-lg-3"
      >
        <button
          type="button"
          class="btn btn-danger btn-block"
          @click="deleteAlbum"
        >
          {{ confirmDeletion?$t('confirm'):$t('delete') }}
        </button>
        <button
          v-if="confirmDeletion"
          type="button"
          class="btn btn-secondary btn-block"
          @click="confirmDeletion=!confirmDeletion"
        >
          {{ $t('cancel') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import AlbumUsers from '@/components/albumsettings/AlbumUsers';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';
import { CurrentUser } from '@/mixins/currentuser.js';

export default {
  name: 'AlbumButtons',
  components: { AlbumUsers, KheopsClipLoader },
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
    showQuit: {
      type: Boolean,
      required: true,
      default: true,
    },
    showDelete: {
      type: Boolean,
      required: true,
      default: true,
    },
  },
  data() {
    return {
      confirmDeletion: false,
      confirmQuit: false,
      onloading: false,
    };
  },
  computed: {
    lastAdmin() {
      const last = this.users.filter((user) => user.is_admin && user.email !== this.currentuserEmail);
      const currentUserAdmin = this.users.filter((user) => user.is_admin && user.email === this.currentuserEmail);
      return (!(last.length > 0) && currentUserAdmin.length > 0);
    },
    lastUser() {
      return !(this.users.length > 1);
    },
    listUsers() {
      return this.users.filter((user) => user.email !== this.currentuserEmail);
    },
  },
  methods: {
    deleteAlbum() {
      if (!this.confirmDeletion) {
        this.confirmDeletion = true;
      } else {
        this.onloading = true;
        this.$store.dispatch('deleteAlbum', { album_id: this.album.album_id }).then(() => {
          this.$router.push('/albums');
        }).catch(() => {
          this.onloading = false;
          this.$snotify.error(this.$t('sorryerror'));
        });
      }
    },
    quitAlbum() {
      if (!this.confirmQuit) {
        this.confirmQuit = true;
      } else {
        this.onloading = true;
        this.$store.dispatch('removeAlbumUser', { album_id: this.album.album_id, user: this.currentuserSub }).then(() => {
          this.$router.push('/albums');
        }).catch(() => {
          this.onloading = false;
          this.$snotify.error(this.$t('sorryerror'));
        });
      }
    },
  },
};
</script>
