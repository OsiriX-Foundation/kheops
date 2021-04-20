<template>
  <div>
    <div class="row mt-4 justify-content-center">
      <div class="col-sm-6 col-md-4 text-sm-left text-md-right">
        <b-form-checkbox
          v-model="statusMsgPrivate"
          class="pt-1"
          inline
          @change="setEnabledVariables()"
        >
          <span
            class="pointer"
          >
            {{ $t("comment.checkprivateuser") }}
          </span>
        </b-form-checkbox>
      </div>
      <div class="col-sm-6 col-md-4">
        <add-user
          :id="id"
          ref="privateuser"
          :scope="scope"
          :enable-add="statusMsgPrivate"
          @private-user="setPrivateUser"
        />
      </div>
    </div>
    <div class="row mt-2 justify-content-center">
      <div class="col-sm-12 col-md-10 offset-md-1">
        <form
          @submit.prevent="addComment"
        >
          <div class="input-group mb-3">
            <textarea
              ref="textcomment"
              v-model="newComment.comment"
              v-focus
              class="form-control form-control-sm"
              :placeholder="$t('comment.writecomment')"
              rows="2"
              maxlength="1024"
              :readonly="disabledText"
              @keydown.enter.prevent="addComment"
              @click="checkUserFromTextarea"
            />
            <div class="input-group-append">
              <button
                v-if="onloading === false"
                title="send comment"
                type="submit"
                class="btn btn-primary button-cursor"
                :disabled="newComment.comment.length < 1 || disabledText"
              >
                <v-icon name="paper-plane" />
              </button>
              <kheops-clip-loader
                v-if="onloading === true"
              />
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
<script>
import AddUser from '@/components/user/AddUser';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'WriteComments',
  components: {
    AddUser, KheopsClipLoader,
  },
  props: {
    id: {
      type: String,
      required: true,
    },
    scope: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      statusMsgPrivate: false,
      disabledText: false,
      privateUser: '',
      newComment: {
        comment: '',
        to_user: '',
      },
      onloading: false,
    };
  },
  computed: {
    accessVar() {
      return this.scope === 'album' ? 'album_access' : 'study_access';
    },
  },
  watch: {
    disabledText: {
      handler() {
        if (!this.disabledText) {
          const { textcomment } = this.$refs;
          setTimeout(() => { textcomment.focus(); }, 0);
        }
      },
    },
  },
  methods: {
    setEnabledVariables() {
      this.disabledText = !this.statusMsgPrivate;
    },
    setPrivateUser(user) {
      if (user !== '') {
        this.disabledText = false;
        this.privateUser = user;
      } else {
        this.disabledText = this.statusMsgPrivate;
      }
    },
    checkSpecificUser(user) {
      this.$refs.privateuser.checkSpecificUser(user).then((res) => {
        if (res.status === 204) {
          this.$snotify.error(this.$t('comment.userunknown', { user }));
        } else if (!res.data[this.accessVar]) {
          this.$snotify.error(this.scope === 'album' ? this.$t('comment.noaccessalbum', { user }) : this.$t('comment.noaccessstudy', { user }));
        } else if (res.status === 200 && res.data[this.accessVar]) {
          this.statusMsgPrivate = true;
          this.$refs.privateuser.setUser(user);
          const { textcomment } = this.$refs;
          setTimeout(() => { textcomment.focus(); }, 0);
        }
      });
    },
    checkUserFromTextarea() {
      if (this.disabledText) {
        this.$refs.privateuser.checkUser();
      }
    },
    addComment() {
      if (this.newComment.comment.length >= 1 && this.onloading === false) {
        const queries = {
          comment: this.newComment.comment,
        };
        if (this.statusMsgPrivate) {
          queries.to_user = this.privateUser;
        }

        if (this.scope === 'studies') {
          this.addStudyComment(queries);
        } else if (this.scope === 'album') {
          this.addAlbumComment(queries);
        }
      }
    },
    addStudyComment(queries) {
      this.onloading = true;
      const params = {
        StudyInstanceUID: this.id,
        queries,
      };

      this.$store.dispatch('postStudyComment', params).then((res) => {
        if (res.status === 204) {
          this.newComment.comment = '';
          this.$emit('add-study-comment');
          this.onloading = false;
        }
      }).catch((res) => {
        this.$snotify.error(`${this.$t('sorryerror')}: ${res}`);
        this.newComment.comment = '';
        this.onloading = false;
      });
    },
    addAlbumComment(queries) {
      this.onloading = true;
      const params = {
        album_id: this.id,
        queries,
      };

      this.$store.dispatch('postAlbumComment', params).then((res) => {
        if (res.status === 204) {
          this.newComment.comment = '';
          this.$emit('add-album-comment');
        }
        this.onloading = false;
      }).catch((res) => {
        this.$snotify.error(`${this.$t('sorryerror')}: ${res}`);
        this.newComment.comment = '';
        this.onloading = false;
      });
    },
  },
};
</script>
