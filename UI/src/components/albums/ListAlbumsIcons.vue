<template>
  <span
    class="ml-auto"
    :class="visibility ? 'iconsHover' : 'iconsUnhover'"
    @click.stop="toggleFavorite(album.album_id, album.is_favorite)"
  >
    <v-icon
      name="star"
      class="kheopsicon"
      :class="(!album.is_favorite) ? '' : 'bg-neutral fill-neutral'"
    />
  </span>
</template>
<script>
import mobiledetect from '@/mixins/mobiledetect.js';

export default {
  name: 'ListAlbumIcons',
  props: {
    album: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    albumsKey: {
      type: String,
      required: false,
      default: 'all',
    },
  },
  computed: {
    visibility() {
      return this.album.flag.is_hover || this.mobiledetect || this.album.is_favorite;
    },
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
    },
  },
  methods: {
    toggleFavorite(albumID, isFavorite) {
      const value = !isFavorite;
      this.$store.dispatch('manageFavoriteAlbum', { album_id: albumID, value }).then(() => {
        this.$store.dispatch('setValueAlbum', {
          album_id: albumID,
          flag: 'is_favorite',
          value,
          key: this.albumsKey,
        });
      });
    },
  },
};
</script>
