<template>
  <div class="container">
    <h3
      class="d-sm-inline-flex full-width"
    >
      <div
        class="mr-auto"
      >
        {{ $t('albumusersettings.userlist') }}
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
        />{{ $t('albumusersettings.add_user') }}
      </button>
      <form
        v-if="form_add_user && onloading === false"
        @submit.prevent="addUser() && onloading !== true"
      >
        <div class="input-group">
          <div>
            <input-auto-complet
              :placeholder="$t('user.emailuser')"
              @input-value="setUsername"
            />
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
      <div
        v-if="onloading === true"
      >
        <kheops-clip-loader
          size="30px"
        />
      </div>
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
              {{ $t('albumusersettings.usersettings') }}
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
                  {{ $t(`albumusersettings.${dictSettings[label]}`) }}
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
import AlbumUsers from '@/components/albumsettings/AlbumUsers';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';
import InputAutoComplet from '@/components/globals/InputAutoComplet';

export default {
  name: 'AlbumSettingsUser',
  components: { AlbumUsers, KheopsClipLoader, InputAutoComplet },
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
      onloading: false,
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
      if (this.validEmail(this.new_user_name)) {
        this.onloading = true;
        const params = {
          album_id: this.album.album_id,
          user: this.new_user_name,
        };
        this.$store.dispatch('addAlbumUser', params).then(() => {
          this.new_user_name = '';
          this.form_add_user = false;
          this.confirm_delete = '';
          this.onloading = false;
        }).catch((err) => {
          if (err.response !== undefined && err.response.data !== undefined && err.response.data.message === 'User Not Found') {
            this.$snotify.error(this.$t('user.usernotfound'));
          } else {
            this.$snotify.error(this.$t('sorryerror'));
          }
          this.onloading = false;
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
    setUsername(username) {
      this.new_user_name = username;
    },
  },
};

</script>
