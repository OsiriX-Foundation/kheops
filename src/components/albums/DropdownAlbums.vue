<i18n>
{
  "en": {
    "addalbum": "Add to an album",
    "addinnewalbum": "Add to a new album",
    "headerlistalbums": "Albums list"
  },
  "fr": {
    "addalbum": "Ajouter à un album",
    "addinnewalbum": "Ajouter à un nouvel album",
    "headerlistalbums": "Liste d'albums"
  }
}
</i18n>
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
      <span>{{ $t("addalbum") }}</span>
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
          {{ $t('addinnewalbum') }}
        </span>
      </b-dropdown-item>
      <b-dropdown-divider />
      <b-dropdown-header>
        {{ $t('headerlistalbums') }}
      </b-dropdown-header>
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
    queriesAlbums: {
      type: Object,
      required: false,
      default: () => ({
        canAddSeries: true,
      }),
    },
  },
  computed: {
    ...mapGetters({
      albums: 'albums',
    }),
    listAlbums() {
      const albumID = this.$route.params.album_id;
      return this.albums.filter((album) => album.album_id !== albumID);
    },
  },
  created() {
    this.setAlbumsList();
  },
  methods: {
    setAlbumsList() {
      this.$store.dispatch('getAlbums', { queries: this.queriesAlbums });
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
