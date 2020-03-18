<template>
  <div class="container">
    <div
      class="row justify-content-center"
    >
      <notifications-toggle
        v-if="scope === 'album'"
        @change="toggleChange"
      />
      <div
        :id="container_id"
        class="card col-sm-12 col-md-10 offset-md-1 pt-3 pb-3 comment-section card-main"
      >
        <loading
          v-if="loading === true"
        />
        <div
          v-for="comment in comments"
          :key="comment.id"
        >
          <user-comments
            v-if="comment.event_type === 'Comment'"
            :scope="scope"
            :comment="comment"
            :currentuser-email="currentuserEmail"
            :write-comments="writeComments"
            @click-private-user="clickPrivateUser"
          />
          <notifications
            v-if="comment.event_type == 'Mutation'"
            :comment="comment"
          />
        </div>
      </div>
    </div>
    <write-comments
      v-if="writeComments"
      :id="id"
      ref="privateuser"
      :scope="scope"
      @add-study-comment="getStudyComments"
      @add-album-comment="getAlbumComments"
    />
  </div>
</template>

<script>
import { CurrentUser } from '@/mixins/currentuser.js';
import UserComments from '@/components/comments/UserComments';
import Notifications from '@/components/comments/Notifications';
import NotificationsToggle from '@/components/comments/NotificationsToggle';
import WriteComments from '@/components/comments/WriteComments';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'CommentsAndNotifications',
  components: {
    Loading, UserComments, Notifications, WriteComments, NotificationsToggle,
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
      loading: true,
    };
  },
  computed: {
    comments() {
      return this.$store.getters.getCommentsByUID(this.id);
    },
    container_id() {
      return (this.scope === 'album') ? 'album_comment_container' : `study_${this.id.replace(/\./g, '_')}_comment_container`;
    },
  },
  created() {
    this.getComments();
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
      this.getComments();
    },
    getStudyComments() {
      this.$store.dispatch('getStudyComments', { StudyInstanceUID: this.id }).then(() => {
        this.loading = false;
        this.scrollBottom();
      });
    },
    getAlbumComments() {
      const types = (this.includeNotifications) ? undefined : { types: 'comments' };
      this.$store.dispatch('getAlbumComments', { album_id: this.id, queries: types }).then(() => {
        this.loading = false;
        this.scrollBottom();
      });
    },
    getComments() {
      if (this.scope === 'album') {
        this.getAlbumComments();
      } else if (this.scope === 'studies') {
        this.getStudyComments();
      }
    },
    scrollBottom() {
      const container = this.$el.querySelector(`#${this.container_id}`);
      if (container !== null) {
        container.scrollTop = container.scrollHeight;
      }
    },
  },
};

</script>
