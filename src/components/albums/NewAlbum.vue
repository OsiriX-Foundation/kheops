<i18n>
{
  "en": {
    "albumName": "Album Name",
    "albumDescription": "Album Description",
    "users": "Users",
    "addUser": "Invite a user",
    "addSeries": "Add studies / series",
    "downloadSeries": "Show download button",
    "sendSeries": "Sharing",
    "deleteSeries": "Remove studies / series",
    "writeComments": "Write comments",
    "create": "Create",
    "cancel": "Cancel",
    "newalbum": "New album",
    "usersettings": "Album user settings",
    "authorizationerror": "You don't have the permissions to add the study {study}",
    "studynotfound": "The study {study} is not found"
  },
  "fr": {
    "albumName": "Nom de l'album",
    "albumDescription": "Description de l'album",
    "users": "Utilisateurs",
    "addUser": "Inviter un utilisateur",
    "addSeries": "Ajouter une étude / série",
    "downloadSeries": "Montrer le bouton de téléchargement",
    "sendSeries": "Partager",
    "deleteSeries": "Supprimer une étude / série",
    "writeComments": "Commenter",
    "create": "Créer",
    "cancel": "Annuler",
    "newalbum": "Nouvel album",
    "usersettings": "Réglages des utilisateurs de l'album",
    "authorizationerror": "Vous n'avez pas les droits d'ajouter l'étude {study}",
    "studynotfound": "l'étude {study} n'a pas été trouvée"
  }
}
</i18n>

<template>
  <div class="container">
    <h3
      class="newalbum-title"
    >
      {{ displayName }}
    </h3>
    <form @submit.prevent="createAlbum">
      <fieldset>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt class="d-none d-sm-block edit-title">
              {{ $t('albumName') }}
            </dt>
            <b class="d-block d-sm-none">{{ $t('albumName') }}</b>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <input
                v-model="album.name"
                v-focus
                type="text"
                :placeholder="$t('albumName')"
                class="form-control"
                maxlength="255"
              >
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt class="d-none d-sm-block edit-title">
              {{ $t('albumDescription') }}
            </dt>
            <b class="d-block d-sm-none">{{ $t('albumDescription') }}</b>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <textarea
                v-model="album.description"
                rows="5"
                class="form-control"
                :placeholder="$t('albumDescription')"
                maxlength="2048"
              />
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt class="d-none d-sm-block edit-title">
              {{ $t('users') }}
            </dt>
            <b class="d-block d-sm-none">{{ $t('users') }}</b>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <h5
                v-if="album.users.length > 0"
                class="newalbum-user"
              >
                <span
                  v-for="user in album.users"
                  :key="user.email"
                  class="badge badge-secondary"
                >
                  {{ user.email }}
                  <span
                    class="pointer"
                    @click="deleteUser(user)"
                  >
                    <v-icon name="times" />
                  </span>
                </span>
              </h5>
              <h5
                class="newalbum-user"
              >
                <div class="input-group mb-3">
                  <input
                    v-model="newUserName"
                    type="text"
                    class="form-control form-control-sm"
                    placeholder="email"
                    aria-label="Email"
                    @keydown.enter.prevent="checkUser"
                  >
                  <div class="input-group-append">
                    <button
                      id="button-addon2"
                      class="btn btn-outline-secondary btn-sm"
                      type="button"
                      title="add user"
                      @click="checkUser()"
                    >
                      <v-icon name="plus" />
                    </button>
                  </div>
                </div>
              </h5>
            </dd>
          </div>
        </div>
      </fieldset>

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
              v-for="(valuex, idx) in numberCol"
              :key="idx"
              class="col-md-12 col-lg-6 col-xl-5"
            >
              <span
                v-for="(valuey,idy) in Object.entries(album.userSettings).slice((userSettingsLength/2)*(idx), (userSettingsLength/2)*valuex)"
                :key="idy"
              >
                <div
                  class="mt-2"
                  :class="(valuey[0]=='sendSeries')?'offset-1':''"
                >
                  <toggle-button
                    v-model="album.userSettings[valuey[0]]"
                    :disabled="(!album.userSettings.downloadSeries && valuey[0]=='sendSeries')"
                    :color="{checked: '#5fc04c', unchecked: 'grey'}"
                    :sync="true"
                  />
                  <label
                    class="user-settings ml-2 mt-2 word-break"
                  >
                    {{ $t(valuey[0]) }}
                  </label>
                </div>
              </span>
            </div>
          </div>
        </div>
      </div>

      <fieldset>
        <div class="row">
          <div class="col-md-10 mt-1 d-none d-sm-none d-md-block">
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="!album.name"
            >
              {{ $t('create') }}
            </button>
            <router-link
              to="/albums"
              class="btn btn-secondary"
            >
              {{ $t('cancel') }}
            </router-link>
          </div>
          <div class="col-12 mt-1 d-md-none">
            <button
              type="submit"
              class="btn btn-primary btn-block"
              :disabled="!album.name"
            >
              {{ $t('create') }}
            </button>
            <router-link
              to="/albums"
              class="btn btn-secondary btn-block"
            >
              {{ $t('cancel') }}
            </router-link>
          </div>
        </div>
      </fieldset>
    </form>
  </div>
</template>

<script>
import { HTTP } from '@/router/http';

export default {
  name: 'NewAlbum',
  data() {
    return {
      album: {
        album_id: '',
        name: '',
        description: '',
        addUser: false,
        downloadSeries: true,
        sendSeries: true,
        deleteSeries: false,
        addSeries: true,
        writeComments: true,
        users: [],
        userSettings: {
          addUser: false,
          addSeries: true,
          deleteSeries: false,
          downloadSeries: true,
          sendSeries: true,
          writeComments: true,
        },
      },
      newUserName: '',
      numberCol: 2,
    };
  },
  computed: {
    displayName() {
      return (!this.album.album_id) ? this.$t('newalbum') : this.album.name;
    },
    userSettingsLength() {
      return Object.keys(this.album.userSettings).length;
    },
    downloadSeries() {
      return this.album.userSettings.downloadSeries;
    },
  },
  watch: {
    downloadSeries() {
      if (!this.album.userSettings.downloadSeries) {
        this.album.userSettings.sendSeries = false;
      }
    },
  },
  methods: {
    deleteUser(user) {
      const index = this.album.users.findIndex((i) => i.email === user.email);
      if (index > -1) this.album.users.splice(index, 1);
    },
    checkUser() {
      const vm = this;
      const idx = _.findIndex(vm.album.users, (u) => u.email === vm.newUserName);
      if (vm.newUserName && idx === -1) {
        HTTP.get(`users?reference=${vm.newUserName}`, { headers: { Accept: 'application/json' } }).then((res) => {
          if (res.status === 204) this.$snotify.error('User unknown');
          else if (res.status === 200) {
            this.album.users.push({ email: res.data.email });
            vm.newUserName = '';
          }
        }).catch(() => {
          console.log('Sorry, an error occured');
        });
      }
    },
    createAlbum() {
      const formData = {
        name: this.album.name,
        description: this.album.description,
        addUser: this.album.userSettings.addUser,
        downloadSeries: this.album.userSettings.downloadSeries,
        sendSeries: this.album.userSettings.sendSeries,
        deleteSeries: this.album.userSettings.deleteSeries,
        addSeries: this.album.userSettings.addSeries,
        writeComments: this.album.userSettings.writeComments,
      };
      this.$store.dispatch('createAlbum', { formData }).then((res) => {
        if (res.status === 201) {
          const albumCreated = res.data;
          this.addAlbumUser(albumCreated);
          const data = this.dataToUpload(albumCreated.album_id);
          if (data.length > 0) {
            this.putStudiesInAlbum(albumCreated, data).then(() => {
              this.$router.push(`/albums/${albumCreated.album_id}`);
            });
          } else {
            this.$router.push(`/albums/${albumCreated.album_id}`);
          }
        }
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    addAlbumUser(albumCreated) {
      this.album.users.forEach((user) => {
        const paramsUser = {
          album_id: albumCreated.album_id,
          user: user.email,
        };
        this.$store.dispatch('addAlbumUser', paramsUser).then((res) => {
          if (res.status !== 201) {
            this.$snotify.error(this.$t('sorryerror'));
          }
        }).catch((err) => {
          this.$snotify.error(this.$t('sorryerror'));
          return err;
        });
      });
    },
    putStudiesInAlbum(albumCreated, data) {
      let queries = {};
      queries = this.$route.query.source === 'inbox' ? { inbox: true } : { album: this.$route.query.source };
      return this.$store.dispatch('putStudiesInAlbum', { queries, data }).then((results) => {
        results.forEach((result) => {
          const { res } = result;
          if (res.request !== undefined && res.request.status !== 201) {
            const { studyId } = result;
            if (res.request.status === 403) {
              this.$snotify.error(this.$t('authorizationerror', { study: studyId }));
            } else if (res.request.status === 404) {
              this.$snotify.error(this.$t('studynotfound', { study: studyId }));
            } else {
              this.$snotify.error(this.$t('sorryerror'));
            }
          }
        });
      });
    },
    dataToUpload(albumId) {
      const data = [];
      if (this.$route.query && this.$route.query.StudyInstanceUID) {
        const studies = Array.isArray(this.$route.query.StudyInstanceUID) ? this.$route.query.StudyInstanceUID : [this.$route.query.StudyInstanceUID];
        studies.forEach((study) => {
          data.push({
            album_id: albumId,
            study_id: study,
          });
        });
      }
      if (this.$route.query && this.$route.query.SeriesInstanceUID) {
        const series = Array.isArray(this.$route.query.SeriesInstanceUID) ? this.$route.query.SeriesInstanceUID : [this.$route.query.SeriesInstanceUID];
        series.forEach((serie) => {
          const infoSerie = serie.split(',');
          if (infoSerie.length === 2) {
            data.push({
              album_id: albumId,
              study_id: infoSerie[0],
              serie_id: infoSerie[1],
            });
          }
        });
      }
      return data;
    },
  },
};

</script>
