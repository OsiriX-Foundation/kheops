<i18n scoped>
{
  "en": {
    "studies": "Studies",
    "comments": "Comments",
    "settings": "Settings",
    "twitterEnable": "Twitter link enable",
    "twitterDisable": "No twitter link",
    "sharingEnable": "Sharing link enable",
    "sharingDisable": "No sharing link"
  },
  "fr": {
    "studies": "Etudes",
    "comments": "Commentaires",
    "settings": "Réglages",
    "twitterEnable": "Lien twitter déjà actif",
    "twitterDisable": "Pas de lien twitter",
    "sharingEnable": "Lien de partage déjà actif",
    "sharingDisable": "Pas de lien de partage"
  }
}
</i18n>

<template>
  <div
    v-if="!loading"
  >
    <div
      class="container"
    >
      <div class="row">
        <div class="col-12 col-sm-12 col-md-12 col-lg-6">
          <h3>
            <v-icon
              name="book"
              scale="2"
            />
            <span class="p-2">
              {{ album.name }}
            </span>
            <span
              @click.stop="toggleFavorite(album.album_id, album.is_favorite)"
            >
              <v-icon
                name="star"
                scale="2"
                :color="(!album.is_favorite) ? 'grey' : ''"
              />
            </span>
            <span
              v-if="album.is_admin === true"
              id="twitter-link"
              :text="twitterToken.length > 0 ? $t('twitterEnable') : $t('twitterDisable')"
              style="cursor: pointer;"
              @click.stop="toggleTwitter(album.album_id)"
            >
              <v-icon
                name="twitter"
                :color="(twitterToken.length > 0) ? '#1da1f2' : '#657786'"
                scale="3"
              />
            </span>
            <span
              v-if="album.is_admin === true"
              id="sharing-link"
              :text="sharingToken.length > 0 ? $t('sharingEnable') : $t('sharingDisable')"
              class="btn btn-link p-0"
              @click.stop="sharingTokenParams.show = !sharingTokenParams.show"
            >
              <v-icon
                name="link"
                :color="(sharingToken.length > 0) ? 'white' : 'grey'"
                scale="2"
              />
            </span>
          </h3>
        </div>
        <div class="col-12 col-sm-12 col-md-12 col-lg-6 mb-3">
          <nav class="nav nav-pills nav-fill flex-column flex-lg-row text-center justify-content-lg-end">
            <a
              class="nav-link"
              :class="(currentView === 'studies' || currentView === undefined)?'active':''"
              @click.stop="loadView('studies')"
            >
              {{ $t('studies') }}
            </a>
            <a
              class="nav-link"
              :class="(currentView === 'comments')?'active':''"
              @click.stop="loadView('comments')"
            >
              {{ $t('comments') }}
            </a>
            <a
              class="nav-link"
              :class="(currentView === 'settings')?'active':''"
              @click.stop="loadView('settings')"
            >
              {{ $t('settings') }}
            </a>
          </nav>
        </div>
        <!-- <div class = 'col-md'></div> -->
      </div>
    </div>
    <!--
      https://fr.vuejs.org/v2/guide/components-dynamic-async.html
    -->
    <span v-if="currentView === 'studies' || currentView === undefined && loading === false">
      <div class="container">
        <div
          v-if="album.description !== undefined && album.description.length > 0"
          class="card"
        >
          <div
            class="card-body"
            style="max-height: 135px; overflow-y: auto"
          >
            <p
              v-for="line in album.description.split('\n')"
              :key="line.id"
              class="pl-3 py-0 my-0"
            >
              {{ line }}
            </p>
          </div>
        </div>
      </div>
      <component-import-study
        :album="album"
        :source="source"
        :permissions="permissions"
      />
    </span>
    <album-comments
      v-if="currentView=='comments' && loading === false"
      :id="album.album_id"
    />
    <album-settings
      v-if="currentView=='settings' && loading === false"
      :album="album"
    />
    <b-popover
      v-if="showRevokeTwitter"
      target="twitter-link"
      :show="showRevokeTwitter"
      placement="auto"
    >
      <twitter-link
        :tokens="twitterToken"
        @cancel="showRevokeTwitter = false"
        @revoke="revokeTwitterTokens"
      />
    </b-popover>
    <b-popover
      v-if="sharingTokenParams.show"
      target="sharing-link"
      :show.sync="sharingTokenParams.show"
      placement="auto"
    >
      <sharing-link
        :album-id="albumID"
        :url="urlSharing"
        :tokens="sharingToken"
        @cancel="cancelSharingToken"
        @revoke="revokeSharingTokens"
        @create="createSharingToken"
      />
    </b-popover>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import moment from 'moment';
import AlbumComments from '@/components/albums/AlbumComments';
import AlbumSettings from '@/components/albums/AlbumSettings';
import ComponentImportStudy from '@/components/study/ComponentImportStudy';
import SharingLink from '@/components/socialmedia/SharingLink';
import TwitterLink from '@/components/socialmedia/TwitterLink';

export default {
  name: 'Album',
  components: {
    ComponentImportStudy, AlbumSettings, AlbumComments, SharingLink, TwitterLink,
  },
  data() {
    return {
      newUserName: '',
      loading: true,
      twitterTokenParams: {
        title: 'twitter_link',
        scope_type: 'album',
        album: '',
        read_permission: true,
        write_permission: false,
        download_permission: true,
        appropriate_permission: false,
        expiration_time: '',
      },
      showRevokeTwitter: false,
      sharingTokenParams: {
        show: false,
      },
      urlSharing: '',
    };
  },
  computed: {
    ...mapGetters({
      album: 'album',
      albumTokens: 'albumTokens',
      validParamsToken: 'validParamsToken',
    }),
    currentSettings() {
      return this.$route.params.category;
    },
    currentView() {
      return this.$route.params.view;
    },
    albumID() {
      return this.$route.params.album_id;
    },
    source() {
      return {
        key: 'album',
        value: this.albumID,
      };
    },
    permissions() {
      return {
        add_series: this.album.add_series || this.album.is_admin,
        delete_series: this.album.delete_series || this.album.is_admin,
        download_series: this.album.download_series || this.album.is_admin,
        send_series: this.album.send_series || this.album.is_admin,
        write_comments: this.album.write_comments || this.album.is_admin,
        add_inbox: this.album.send_series || this.album.is_admin,
      };
    },
    twitterToken() {
      return this.albumTokens.filter((token) => token.title.includes('twitter_link') && moment(token.expiration_time) > moment() && !token.revoked);
    },
    sharingToken() {
      return this.albumTokens.filter((token) => token.title.includes('sharing_link') && moment(token.expiration_time) > moment() && !token.revoked);
    },
  },
  watch: {
    albumID() {
      this.loading = true;
      this.loadAlbum().then(() => {
        this.loading = false;
      });
    },
    currentView() {
      if (this.currentView !== undefined) {
        this.loadAlbum();
      }
    },
    currentSettings() {
      if (this.currentSettings !== undefined) {
        this.loadAlbum();
      }
    },
  },
  created() {
    this.loading = true;
    this.loadAlbum().then((res) => {
      if (res !== undefined && res.status === 200) {
        if (this.album.is_admin === true) {
          this.getTokens();
        }
        this.loading = false;
      }
    });
  },
  beforeDestroy() {
    this.$store.commit('INIT_ALBUM');
    this.$store.commit('INIT_ALBUM_USERS');
  },
  methods: {
    getTokens() {
      const queries = {
        valid: this.validParamsToken,
        album: this.albumID,
      };
      return this.$store.dispatch('getAlbumTokens', { queries });
    },
    loadAlbum() {
      return this.$store.dispatch('getAlbum', { album_id: this.albumID }).then((res) => res)
        .catch((err) => {
          this.$router.push('/albums');
          Promise.reject(err);
        });
    },
    toggleFavorite(albumID, isFavorite) {
      const value = !isFavorite;
      this.$store.dispatch('manageFavoriteAlbum', { album_id: albumID, value }).then(() => {
        this.$store.dispatch('setKeyValueAlbum', { key: 'is_favorite', value });
      });
    },
    toggleTwitter(albumID) {
      if (this.twitterToken.length === 0) {
        this.createTwitterToken(albumID);
      } else {
        this.showRevokeTwitter = this.twitterToken.length > 0 && !this.showRevokeTwitter;
      }
    },
    createTwitterToken(albumID) {
      this.twitterTokenParams.album = albumID;
      this.twitterTokenParams.expiration_time = moment().add(100, 'Y').format();
      const twitterWindow = window.open('', 'twitter');
      this.createToken(this.twitterTokenParams).then((res) => {
        const urlTwitter = 'https://twitter.com/intent/tweet';
        const urlSharing = `I want to show you my study album ! Click on this link ${process.env.VUE_APP_URL_ROOT}/view/${res.data.access_token} #Kheops`;
        const queries = `?text=${encodeURIComponent(urlSharing)}`;
        twitterWindow.location.href = urlTwitter + queries;
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    revokeTwitterTokens(tokens) {
      this.showRevokeTwitter = false;
      this.revokeTokens(tokens);
    },
    revokeSharingTokens(tokens) {
      this.cancelSharingToken();
      this.urlSharing = '';
      this.revokeTokens(tokens);
    },
    cancelSharingToken() {
      this.sharingTokenParams.show = false;
    },
    createSharingToken(token) {
      this.createToken(token).then((res) => {
        const urlSharing = `${process.env.VUE_APP_URL_ROOT}/view/${res.data.access_token}`;
        this.urlSharing = urlSharing;
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
    },
    createToken(token) {
      return this.$store.dispatch('createToken', { token });
    },
    revokeTokens(tokens) {
      tokens.forEach((token) => {
        this.$store.dispatch('revokeToken', { token_id: token.id }).then((res) => {
          if (res.status === 200) {
            this.getTokens();
          }
        }).catch(() => {
          this.$snotify.error(this.$t('sorryerror'));
        });
      });
    },
    loadView(view) {
      this.$router.push({ name: 'albumview', params: { view } });
    },
  },
};
</script>

<style scoped>
h3 {
  margin-bottom: 40px;
  float: left;
}

h5.user{
  float: left;
  margin-right: 10px;
}

.icon{
  margin-left: 10px;
}
.pointer{
  cursor: pointer;
}
label{
  margin-left: 10px;
}
a.nav-link{
  cursor: pointer;
}
.nav a:hover:not(.active) {
  opacity: 0.5;
}
</style>
