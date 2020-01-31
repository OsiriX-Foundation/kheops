<i18n>
{
  "en": {
    "view": "View"
  },
  "fr": {
    "view": "Vue"
  }
}
</i18n>

<template>
  <div class="container">
    <links
      scope="album"
      :albumid="album.album_id"
      :write-permission="album.is_admin"
    />
  </div>
</template>

<script>
import links from '@/components/socialmedia/Links';

export default {
  name: 'AlbumSettingsSocialMedia',
  components: { links },
  props: {
    album: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
};

</script>
