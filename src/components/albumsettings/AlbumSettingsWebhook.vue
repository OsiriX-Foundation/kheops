<template>
  <div
    v-if="album.is_admin"
    class="container"
  >
    <webhooks
      scope="album"
      :album-id="album.album_id"
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
