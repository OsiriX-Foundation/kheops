<i18n>
{
  "en": {
    "userlist": "List of users",
    "add_user": "Invite a user",
    "add_series": "Add Studies / Series",
    "download_series": "Show Download Button",
    "send_series": "Sharing",
    "delete_series": "Remove Studies / Series",
    "write_comments": "Write Comments",
    "albumuseraddsuccess": "User successfully added to the album",
    "Unknown user": "Unknown user",
    "usersettings": "Album user settings",
    "allreadypresent": "This user is already present in the album",
    "add": "Add",
    "cancel": "Cancel",
    "user": "user"
  },
  "fr": {
    "userlist": "Liste d'utilisateurs",
    "add_user": "Inviter un utilisateur",
    "add_series": "Ajouter une étude / série",
    "download_series": "Montrer le bouton de téléchargement",
    "send_series": "Partager",
    "delete_series": "Supprimer une étude / série",
    "write_comments": "Commenter",
    "albumuseraddsuccess": "L'utilisateur a été ajouté avec succès à l'album",
    "Unknown user": "Utilisateur inconnu",
    "usersettings": "Réglages des utilisateurs de l'album",
    "allreadypresent": "Cet utilisateur est déjà présent dans l'album",
    "add": "Ajouter",
    "cancel": "Annuler",
    "user": "utilisateur"
  }
}
</i18n>

<template>
  <div class="container">
    <h3
      class="d-sm-inline-flex full-width"
    >
      <div
        class="mr-auto"
      >
        {{ $t('userlist') }}
      </div>
      <button
        v-if="(album.add_user||album.is_admin) && form_add_user === false"
        class="btn btn-secondary"
        @click="form_add_user=true"
      >
        <v-icon
          name="user-plus"
          scale="1"
          class="mr-2"
        />{{ $t('add_user') }}
      </button>
      <form
        v-if="form_add_user"
        @submit.prevent="addUser"
      >
        <div class="input-group">
          <div>
            <input
              v-model="new_user_name"
              v-focus
              type="email"
              class="form-control"
              :placeholder="'email '+$t('user')"
            >
          </div>
          <div class="input-group-append">
            <button
              class="btn btn-primary"
              type="submit"
              :disabled="!validEmail(new_user_name)"
            >
              {{ $t('add') }}
            </button>
            <button
              class="btn btn-secondary"
              type="reset"
              tabindex="0"
              @keyup.esc="new_user_name=''"
              @click="new_user_name='';form_add_user=!form_add_user"
            >
              {{ $t('cancel') }}
            </button>
          </div>
        </div>
      </form>
    </h3>

    <album-users
      :album="album"
      :users="users"
      :show-delete-user="true"
      :show-change-role="true"
    />

    <div class="card user-settings">
      <div class="container mb-3">
        <div
          class="bg-primary row"
        >
          <div class="col-xl-1" />
          <div class="col-xl-11">
            <h4
              class="mt-3 mb-3 ml-2"
            >
              {{ $t('usersettings') }}
            </h4>
          </div>
        </div>
        <div
          class="row toggle-padding mt-3"
        >
          <div class="col-xl-1" />
          <div
            v-for="(value, idx) in numberCol"
            :key="idx"
            class="col-md-12 col-lg-6 col-xl-5"
          >
            <span
              v-for="(label,idy) in userSettings.slice((userSettings.length/2)*(idx), (userSettings.length/2)*value)"
              :key="idy"
            >
              <div
                class="mt-2"
                :class="(label=='send_series')?'offset-1':''"
              >
                <toggle-button
                  v-if="album.is_admin"
                  :value="album[label]"
                  :disabled="(!album.download_series && label=='send_series')"
                  :sync="true"
                  :color="{checked: '#5fc04c', unchecked: 'grey'}"
                  @change="patchAlbum(label)"
                />
                <v-icon
                  v-if="!album.is_admin && !album[label]"
                  name="ban"
                  class="text-danger"
                />
                <v-icon
                  v-if="!album.is_admin && album[label]"
                  name="check-circle"
                  class="text-success"
                />
                <label class="ml-2 mt-2 word-break">
                  {{ $t(label) }}
                </label>
              </div>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import AlbumUsers from '@/components/albums/AlbumUsers';

export default {
  name: 'AlbumSettingsUser',
  components: { AlbumUsers },
  props: {
    album: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  data() {
    return {
      form_add_user: false,
      new_user_name: '',
      userSettings: [
        'add_user',
        'add_series',
        'delete_series',
        'download_series',
        'send_series',
        'write_comments',
      ],
      dictSettings: {
        add_user: 'addUser',
        add_series: 'addSeries',
        delete_series: 'deleteSeries',
        download_series: 'downloadSeries',
        send_series: 'sendSeries',
        write_comments: 'writeComments',
      },
      numberCol: 2,
    };
  },
  computed: {
    ...mapGetters({
      users: 'albumUsers',
    }),
  },
  created() {
    this.$store.dispatch('getUsersAlbum', { album_id: this.album.album_id });
  },
  methods: {
    addUser() {
      const sameUserName = this.users.filter((user) => user.email === this.new_user_name);
      if (sameUserName.length > 0) {
        this.$snotify.error(this.$t('allreadypresent'));
      } else if (this.validEmail(this.new_user_name)) {
        const params = {
          album_id: this.album.album_id,
          user: this.new_user_name,
        };
        this.$store.dispatch('addAlbumUser', params).then((res) => {
          if (res.status === 201) {
            this.new_user_name = '';
            this.form_add_user = false;
            this.confirm_delete = '';
          } else {
            this.$snotify.error(this.$t('sorryerror'));
          }
        }).catch((err) => {
          this.$snotify.error(this.$t('sorryerror'));
          return err;
        });
      }
    },
    validEmail(email) {
      const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      return re.test(email);
    },
    patchAlbum(field) {
      const queries = {};
      queries[this.dictSettings[field]] = !this.album[field];
      if (field === 'download_series' && this.album.download_series) {
        queries[this.dictSettings.send_series] = false;
      }
      const params = {
        album_id: this.album.album_id,
        queries,
      };
      this.$store.dispatch('editAlbum', params);
    },
  },
};

</script>
