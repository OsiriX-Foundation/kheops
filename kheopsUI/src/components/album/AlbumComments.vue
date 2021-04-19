<template>
  <div class="container">
    <comments-and-notifications
      :id="id"
      :write-comments="album.is_admin || album.write_comments"
      scope="album"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import commentsAndNotifications from '@/components/comments/commentsAndNotifications';

export default {
  name: 'AlbumComments',
  components: { commentsAndNotifications },
  props: {
    id: {
      type: String,
      required: true,
    },
  },
  computed: {
    ...mapGetters({
      album: 'album',
    }),
  },
};
</script>
