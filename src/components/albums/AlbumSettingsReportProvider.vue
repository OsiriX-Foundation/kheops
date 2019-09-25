<template>
  <div
    class="container"
  >
    <providers
      :album-i-d="album.album_id"
      :write-permission="album.is_admin"
    />
  </div>
</template>

<script>
import Providers from '@/components/providers/Providers';

export default {
  name: 'AlbumSettingsReportProvider',
  components: { Providers },
  props: {
    album: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
};
</script>
