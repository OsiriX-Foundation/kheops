<template>
  <div class="container">
    <webhooks
      scope="album"
      :album-id="album.album_id"
      :write-permission="album.is_admin"
    />
  </div>
</template>

<script>
import Webhooks from '@/components/webhook/Webhooks';

export default {
  name: 'AlbumSettingsWebhook',
  components: { Webhooks },
  props: {
    album: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
};

</script>
