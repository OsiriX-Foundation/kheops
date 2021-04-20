<template>
  <b-dropdown
    :disabled="disabled"
    variant="link"
    size="sm"
  >
    <template slot="button-content">
      <span>
        <v-icon
          class="align-middle"
          name="book"
        />
      </span><br>
      <span>{{ $t("album.addalbum") }}</span>
    </template>
    <div
      class="maxheight-scroll"
    >
      <b-dropdown-item
        @click.stop="createAlbum()"
      >
        <span
          class="font-kheops"
        >
          {{ $t('album.addinnewalbum') }}
        </span>
      </b-dropdown-item>
      <b-dropdown-divider />
      <b-dropdown-item
        v-for="album in listAlbums"
        :key="album.id"
        @click.stop="addToAlbum(album.album_id)"
      >
        {{ album.name|maxTextLength(albumNameMaxLength) }}
      </b-dropdown-item>
    </div>
  </b-dropdown>
</template>
<script>
import { mapGetters } from 'vuex';

export default {
  name: 'DropdownImportStudy',
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false,
    },
    albumNameMaxLength: {
      type: Number,
      required: false,
      default: 25,
    },
    albumsQueries: {
      type: Object,
      required: false,
      default: () => ({
        canAddSeries: true,
        sort: '-last_event_time',
      }),
    },
    albumsKey: {
      type: String,
      required: false,
      default: 'canAddSeries',
    },
  },
  computed: {
    ...mapGetters('oidcStore', [
      'oidcIsAuthenticated',
      'oidcAccessToken',
    ]),
    albums() {
      return this.$store.getters.getAlbumsByKey(this.albumsKey);
    },
    listAlbums() {
      const albumID = this.$route.params.album_id;
      return this.albums.filter((album) => album.album_id !== albumID);
    },
  },
  created() {
    this.$store.dispatch('initAlbums', { key: this.albumsKey });
    this.setAlbumsList();
  },
  destroyed() {
    this.$store.dispatch('initAlbums', { key: this.albumsKey });
  },
  methods: {
    setAlbumsList() {
      if (this.oidcIsAuthenticated) {
        const headers = {
          Authorization: `Bearer ${this.oidcAccessToken}`,
        };
        this.$store.dispatch('getAlbums', { queries: this.albumsQueries, key: this.albumsKey, headers });
      }
    },
    createAlbum() {
      this.$emit('create-album');
    },
    addToAlbum(albumID) {
      this.$emit('add-to-album', albumID);
    },
  },
};
</script>
