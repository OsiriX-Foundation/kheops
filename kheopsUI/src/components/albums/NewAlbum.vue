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
              {{ $t('newalbum.albumName') }}
            </dt>
            <b class="d-block d-sm-none">{{ $t('newalbum.albumName') }}</b>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <input
                v-model="album.name"
                v-focus
                type="text"
                :placeholder="$t('newalbum.albumName')"
                class="form-control"
                maxlength="255"
              >
              <field-obligatory :state="album.name !== ''" />
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt class="d-none d-sm-block edit-title">
              {{ $t('newalbum.albumDescription') }}
            </dt>
            <b class="d-block d-sm-none">{{ $t('newalbum.albumDescription') }}</b>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <textarea
                v-model="album.description"
                rows="5"
                class="form-control"
                :placeholder="$t('newalbum.albumDescription')"
                maxlength="2048"
              />
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt class="d-none d-sm-block edit-title">
              {{ $t('newalbum.users') }}
            </dt>
            <b class="d-block d-sm-none">{{ $t('newalbum.users') }}</b>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <h5
                class="newalbum-user"
              >
                <div class="input-group mb-3">
                  <input-auto-complet
                    :placeholder="$t('user.emailuser')"
                    :reset="resetUser"
                    :focus="false"
                    :submit="false"
                    @input-reset="setResetInput(false)"
                    @input-value="setUsername"
                    @keydown-enter-prevent="checkUser"
                  />
                  <div class="input-group-append">
                    <button
                      v-if="loadingCheckUser === false"
                      id="button-addon2"
                      class="btn btn-outline-secondary btn-sm"
                      type="button"
                      title="add user"
                      @click="checkUser()"
                    >
                      <v-icon name="plus" />
                    </button>
                    <kheops-clip-loader
                      v-if="loadingCheckUser === true"
                      size="25px"
                      class="ml-1"
                    />
                  </div>
                </div>
              </h5>
            </dd>
          </div>
          <div class="offset-md-3 col-md-9">
            <new-album-user
              v-if="album.users.length > 0"
              :users="album.users"
              @toggle-admin="toggleAdmin"
              @delete-user="deleteUser"
            />
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
                {{ $t('albumusersettings.usersettings') }}
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
                    {{ $t(`albumusersettings.${valuey[0]}`) }}
                  </label>
                </div>
              </span>
            </div>
          </div>
        </div>
      </div>

      <fieldset>
        <create-cancel-button
          :disabled="disabledCreate"
          :loading="oncreate"
          class-col="mt-3 col-12"
          @cancel="cancel"
        />
      </fieldset>
    </form>
  </div>
</template>

<script>
import { HTTP } from '@/router/http';
import CreateCancelButton from '@/components/globalbutton/CreateCancelButton';
import FieldObligatory from '@/components/globals/FieldObligatory';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';
import NewAlbumUser from '@/components/albums/NewAlbumUser';
import InputAutoComplet from '@/components/globals/InputAutoComplet';

export default {
  name: 'NewAlbum',
  components: {
    CreateCancelButton, FieldObligatory, KheopsClipLoader, NewAlbumUser, InputAutoComplet,
  },
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
      oncreate: false,
      loadingCheckUser: false,
      resetUser: false,
    };
  },
  computed: {
    displayName() {
      return (!this.album.album_id) ? this.$t('newalbum.newalbum') : this.album.name;
    },
    userSettingsLength() {
      return Object.keys(this.album.userSettings).length;
    },
    downloadSeries() {
      return this.album.userSettings.downloadSeries;
    },
    disabledCreate() {
      return this.album.name === '' || this.oncreate;
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
    toggleAdmin(user) {
      const index = this.album.users.findIndex((i) => i.email === user.email);
      if (index > -1) this.album.users[index].is_admin = !this.album.users[index].is_admin;
    },
    checkUser() {
      this.loadingCheckUser = true;
      const vm = this;
      const idx = _.findIndex(vm.album.users, (u) => u.email === vm.newUserName);
      if (vm.newUserName && idx === -1) {
        HTTP.get(`users?reference=${vm.newUserName}`, { headers: { Accept: 'application/json' } }).then((res) => {
          if (res.status === 204) this.$snotify.error('User unknown');
          else if (res.status === 200) {
            const user = res.data;
            user.is_admin = false;
            this.album.users.unshift(user);
            this.setResetInput(true);
          }
          this.loadingCheckUser = false;
        }).catch(() => {
          this.loadingCheckUser = false;
        });
      } else {
        this.loadingCheckUser = false;
        this.setResetInput(true);
      }
    },
    createAlbum() {
      this.oncreate = true;
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
          this.addAlbumNewUser(albumCreated);
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
        this.oncreate = false;
      });
    },
    addAlbumNewUser(albumCreated) {
      this.album.users.forEach((user) => {
        const paramsUser = {
          album_id: albumCreated.album_id,
          user: user.email,
        };
        if (user.is_admin === true) {
          this.addAlbumUserAdmin(paramsUser);
        } else {
          this.addAlbumUser(paramsUser);
        }
      });
    },
    addAlbumUserAdmin(paramsUser) {
      this.$store.dispatch('addAlbumUserAdmin', paramsUser).then((res) => {
        if (res.status !== 204) {
          this.$snotify.error(this.$t('sorryerror'));
        }
      }).catch((err) => {
        this.$snotify.error(this.$t('sorryerror'));
        return err;
      });
    },
    addAlbumUser(paramsUser) {
      this.$store.dispatch('addAlbumUser', paramsUser).then((res) => {
        if (res.status !== 201) {
          this.$snotify.error(this.$t('sorryerror'));
        }
      }).catch((err) => {
        this.$snotify.error(this.$t('sorryerror'));
        return err;
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
    cancel() {
      this.$router.go(-1);
    },
    setUsername(username) {
      this.newUserName = username;
    },
    setResetInput(value) {
      this.resetUser = value;
    },
  },
};

</script>
