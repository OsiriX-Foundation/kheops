<template>
  <div
    :id="container_id"
    :class="`${className} card comment-section card-main`"
  >
    <infinite-loading
      ref="infiniteLoading"
      direction="top"
      :identifier="infiniteId"
      @infinite="infiniteHandler"
    >
      <div slot="spinner">
        <loading />
      </div>
      <div slot="no-more" />
      <div slot="no-results" />
      <div slot="error">
        {{ $t('comment.error') }} <br> <br>
        <button
          type="button"
          class=" btn btn-md"
          @click="reloadComments()"
        >
          {{ $t('reload') }}
        </button>
      </div>
    </infinite-loading>
    <div
      v-for="(comment, index) in comments"
      :key="comment.id"
      :index="index"
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
</template>

<script>
import { CurrentUser } from '@/mixins/currentuser.js';
import UserComments from '@/components/comments/UserComments';
import Notifications from '@/components/comments/Notifications';
import Loading from '@/components/globalloading/Loading';
import InfiniteLoading from 'vue-infinite-loading';

export default {
  name: 'Comments',
  components: {
    Loading, UserComments, Notifications, InfiniteLoading,
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
    notification: {
      type: Boolean,
      required: true,
      default: false,
    },
    className: {
      type: String,
      required: false,
      default: '',
    },
  },
  data() {
    return {
      infiniteId: 0,
      offset: 0,
      incrementLimit: 50,
      limit: 50,
    };
  },
  computed: {
    comments() {
      const commentToReverse = this.$store.getters.getCommentsByUID(this.id);
      return commentToReverse.reverse();
    },
    container_id() {
      return (this.scope === 'album') ? 'album_comment_container' : `study_${this.id.replace(/\./g, '_')}_comment_container`;
    },
  },
  watch: {
    notification() {
      this.reloadComments();
    },
  },
  destroyed() {
    this.$store.dispatch('deleteStoreComment', { StudyInstanceUID: this.id });
  },
  methods: {
    // https://peachscript.github.io/vue-infinite-loading/old/#!/getting-started/trigger-manually
    infiniteHandler($state) {
      const firstLoading = this.comments.length === 0;
      this.getComments().then((res) => {
        if (res.data.length === 0) {
          $state.complete();
        } else if (res.data.length === parseInt(res.headers['x-total-count'], 10)) {
          $state.loaded();
          $state.complete();
        } else {
          $state.loaded();
          this.limit += this.incrementLimit;
        }
        if (firstLoading) {
          this.scrollBottom();
        }
      }).catch((err) => {
        $state.error();
        return err;
      });
    },
    getStudyComments() {
      const queries = { limit: this.limit, offset: this.offset };
      return this.$store.dispatch('getStudyComments', { StudyInstanceUID: this.id, queries });
    },
    getAlbumComments() {
      const types = (this.notification) ? undefined : { types: 'comments' };
      const queries = { limit: this.limit, offset: this.offset };
      return this.$store.dispatch('getAlbumComments', { album_id: this.id, queries: { ...types, ...queries } });
    },
    getComments() {
      if (this.scope === 'album') {
        return this.getAlbumComments();
      }
      return this.getStudyComments();
    },
    scrollBottom() {
      const container = this.$el;
      if (container !== null) {
        container.scrollTop = container.scrollHeight;
      }
    },
    clickPrivateUser(email) {
      this.$emit('click-private-user', email);
    },
    reloadComments() {
      this.$store.dispatch('deleteStoreComment', { StudyInstanceUID: this.id });
      this.limit = this.incrementLimit;
      this.infiniteId += 1;
    },
  },
};

</script>
