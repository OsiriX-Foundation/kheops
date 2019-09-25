<i18n scoped>
{
  "en": {
    "studies": "Studies",
    "comments": "Comments",
    "settings": "Settings"
  },
  "fr": {
    "studies": "Etudes",
    "comments": "Commentaires",
    "settings": "Réglages"
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
import AlbumComments from '@/components/albums/AlbumComments';
import AlbumSettings from '@/components/albums/AlbumSettings';
import ComponentImportStudy from '@/components/study/ComponentImportStudy';

export default {
  name: 'Album',
  components: { ComponentImportStudy, AlbumSettings, AlbumComments },
  data() {
    return {
      newUserName: '',
      loading: false,
    };
  },
  computed: {
    ...mapGetters({
      album: 'album',
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
        this.loadAlbum()
      }
    },
    currentSettings() {
      if (this.currentSettings !== undefined) {
        this.loadAlbum()
      }
    }
  },
  created() {
    this.loading = true;
    this.loadAlbum().then(() => {
      this.loading = false;
    });
  },
  beforeDestroy() {
    this.$store.commit('INIT_ALBUM');
    this.$store.commit('INIT_ALBUM_USERS');
  },
  methods: {
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
</style>
