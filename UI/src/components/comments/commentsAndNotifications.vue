<template>
  <div class="container">
    <div
      class="row justify-content-center"
    >
      <notifications-toggle
        v-if="scope === 'album'"
        @change="toggleChange"
      />
      <comments
        :id="id"
        ref="comments"
        :scope="scope"
        :write-comments="writeComments"
        :notification="includeNotifications"
        class-name="col-sm-12 col-md-10 offset-md-1 pt-3 pb-3"
        @click-private-user="clickPrivateUser"
      />
    </div>
    <write-comments
      v-if="writeComments"
      :id="id"
      ref="privateuser"
      :scope="scope"
      @add-study-comment="updateComments"
      @add-album-comment="updateComments"
    />
  </div>
</template>

<script>
import { CurrentUser } from '@/mixins/currentuser.js';
import NotificationsToggle from '@/components/comments/NotificationsToggle';
import WriteComments from '@/components/comments/WriteComments';
import Comments from '@/components/comments/Comments';

export default {
  name: 'CommentsAndNotifications',
  components: {
    WriteComments, NotificationsToggle, Comments,
  },
  mixins: [CurrentUser],
  props: {
    scope: {
      type: String,
      required: true,
    },
    id: {
      type: String,
      required: true,
    },
    writeComments: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {
      includeNotifications: false,
    };
  },
  destroyed() {
    this.$store.dispatch('deleteStoreComment', { StudyInstanceUID: this.id });
  },
  methods: {
    clickPrivateUser(user) {
      this.$refs.privateuser.checkSpecificUser(user);
    },
    toggleChange(value) {
      this.includeNotifications = value;
    },
    updateComments() {
      this.$refs.comments.updateComments();
    },
  },
};

</script>
