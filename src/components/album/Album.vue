<template>
  <div
    v-if="!loading"
  >
    <div
      class="container"
    >
      <div class="row">
        <div class="col-12 col-sm-12 col-md-12 col-lg-6">
          <album-title
            :title="album.name"
          >
            <span
              @click.stop="toggleFavorite(album.album_id, album.is_favorite)"
            >
              <v-icon
                name="star"
                scale="2"
                :color="(!album.is_favorite) ? 'grey' : ''"
              />
            </span>
            <list-links
              v-if="album.is_admin"
              :albumid="album.album_id"
            />
          </album-title>
        </div>
        <div class="col-12 col-sm-12 col-md-12 col-lg-6 mb-3">
          <album-menu />
        </div>
      </div>
    </div>
    <span v-if="currentView === 'studies' || currentView === undefined && loading === false">
      <div class="container">
        <div
          v-if="album.description !== undefined && album.description.length > 0"
          class="card"
        >
          <album-description
            :description="album.description"
          />
        </div>
      </div>
      <component-import-study
        :album-i-d="albumID"
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
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import AlbumComments from '@/components/album/AlbumComments';
import AlbumSettings from '@/components/albumsettings/AlbumSettings';
import AlbumMenu from '@/components/album/AlbumMenu';
import AlbumTitle from '@/components/album/AlbumTitle';
import AlbumDescription from '@/components/album/AlbumDescription';
import ComponentImportStudy from '@/components/study/ComponentImportStudy';
import ListLinks from '@/components/socialmedia/ListLinks';

export default {
  name: 'Album',
  components: {
    ComponentImportStudy, AlbumSettings, AlbumComments, AlbumMenu, AlbumTitle, AlbumDescription, ListLinks,
  },
  data() {
    return {
      newUserName: '',
      loading: true,
    };
  },
  computed: {
    ...mapGetters({
      album: 'album',
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
  },
  watch: {
    albumID() {
      this.initAlbum();
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
    this.initAlbum();
  },
  methods: {
    initAlbum() {
      this.$store.commit('INIT_ALBUM');
      this.$store.commit('INIT_ALBUM_USERS');
      this.$store.commit('INIT_TOKENS');
      this.loading = true;
      this.loadAlbum().then((res) => {
        if (res !== undefined && res.status === 200) {
          if (this.album.is_admin === true) {
            this.getTokens();
          }
          this.loading = false;
        }
      });
      const source = {
        key: 'album',
        value: this.albumID,
      };
      this.$store.dispatch('setSource', source);
    },
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
    loadView(view) {
      this.$router.push({ name: 'albumview', params: { view } });
    },
  },
};
</script>
