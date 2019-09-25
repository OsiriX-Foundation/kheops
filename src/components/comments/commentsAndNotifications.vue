/* eslint-disable */

<i18n>
{
  "en": {
    "commentpostsuccess": "comment posted successfully",
    "imported": "imported",
    "removed": "removed",
    "thestudy": "the study",
    "theseries": "the series",
    "hasadd": "has add",
    "hasgranted": "has granted",
    "hasremoved": "has removed",
    "adminrights": "admin rights",
    "hasleft": "has left",
    "hascreated": "has created",
    "hasedited": "has edited",
    "includenotifications": "include notifications",
    "addalbum": "add as favorite",
    "removealbum": "remove as favorite",
    "to": "to",
    "writecomment": "Write your comment here",
    "checkprivateuser": "Send private message to",
    "notificationof": "Notification of",
    "createreportprovider": "{user} create the report provider {reportname}",
    "editreportprovider": "{user} edit the report provider {reportname}",
    "deletereportprovider": "{user} delete the report provider {reportname}",
    "newreport": "{user} add a new report in the study {study} with the report provider {reportname}"
  },
  "fr" : {
    "commentpostsuccess": "le commentaire a été posté avec succès",
    "imported": "a importé",
    "removed": "a supprimé",
    "thestudy": "l'étude",
    "theseries": "la série",
    "hasadd": "a ajouté",
    "hasgranted": "a attribué",
    "hasremoved": "a retiré",
    "adminrights": "des droits admin",
    "hasleft": "a quitté",
    "hascreated": "a créé",
    "hasedited": "a édité",
    "includenotifications": "inclure les notifications",
    "addalbum": "a mis en favori",
    "removealbum": "a enlevé des favories",
    "to": "à",
    "writecomment": "Ecrivez votre commentaire ici",
    "checkprivateuser": "Envoyer un message privé à",
    "notificationof": "Notification du",
    "createreportprovider": "{user} a créé le report provider {reportname}",
    "editreportprovider": "{user} a édité le report provider {reportname}",
    "deletereportprovider": "{user} a supprimé le report provider {reportname}",
    "newreport": "{user} a ajouté un nouveau rapport dans l'étude {study} avec le report provider {reportname}"
  }
}
</i18n>

<template>
  <div class="container">
    <div class="row justify-content-center">
      <p
        v-if="scope === 'album'"
        class="col-sm-12 col-md-10 offset-md-1 text-right"
      >
        <label class="mr-2">
          {{ $t('includenotifications') }}
        </label> <toggle-button
          v-model="includeNotifications"
          :labels="{checked: 'Yes', unchecked: 'No'}"
          :sync="true"
          @change="getComments"
        />
      </p>

      <div
        :id="container_id"
        class="card col-sm-12 col-md-10 offset-md-1 pt-3 pb-3"
        style="max-height: 600px; overflow-y: scroll;"
      >
        <div
          v-for="comment in comments"
          :key="comment.id"
        >
          <!-- Comments -->

          <div
            v-if="comment.event_type === 'Comment'"
            class="card mt-3 ml-sm-5 mr-sm-5"
            :class="(comment.is_private)?'bg-primary':'bg-secondary'"
          >
            <div class="card-header">
              <v-icon name="user" /> {{ comment.origin_name }}
              <span
                v-if="comment.target_name"
              >
                {{ $t('to') }} {{ comment.target_name }}
              </span>
              <span class="float-right">
                {{ comment.post_date | formatDate }}
              </span>
            </div>
            <div
              class="card-body"
            >
              <p
                v-for="(p,pidx) in splitComment(comment.comment)"
                :key="pidx"
                class="my-0"
              >
                {{ p }}
              </p>
            </div>
          </div>

          <!-- Notifications -->

          <div
            v-if="comment.event_type == 'Mutation'"
            class="card col-sm-10 offset-sm-2 bg-secondary mt-3 ml-sm-5 mr-sm-5"
          >
            <div class="card-header">
              <div class="bd-highlight">
                <i> {{ $t('notificationof') }} {{ comment.post_date | formatDate }} </i>
              </div>
            </div>
            <!-- IMPORT_STUDY, REMOVE_STUDY : -->
            <div class="card-body">
              <div
                v-if="comment.mutation_type === 'IMPORT_STUDY'"
                class="flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('imported') }} {{ $t('thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
              </div>
              <div
                v-if="comment.mutation_type === 'REMOVE_STUDY'"
                class="flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
              </div>

              <!-- IMPORT_SERIES, REMOVE_SERIES -->
              <div
                v-if="comment.mutation_type === 'IMPORT_SERIES'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('imported') }} {{ $t('theseries') }} <b>{{ comment.series.description ? comment.series.description : comment.series.UID }}</b> {{ $t('in') }} {{ $t('thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
              </div>
              <div
                v-if="comment.mutation_type === 'REMOVE_SERIES'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('theseries') }} <b>{{ comment.series.description ? comment.series.description : comment.series.UID }}</b> {{ $t('in') }} {{ $t('thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
              </div>

              <!-- ADD_USER, ADD_ADMIN, REMOVE_USER, PROMOTE_ADMIN, DEMOTE_ADMIN -->
              <div
                v-if="comment.mutation_type === 'ADD_USER'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasadd') }} {{ $t('theuser') }} <i>{{ comment.target_name }}</i>
              </div>
              <div
                v-if="comment.mutation_type === 'ADD_ADMIN'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasadd') }} {{ $t('theadmin') }} <i>{{ comment.target_name }}</i>
              </div>
              <div
                v-if="comment.mutation_type === 'REMOVE_USER'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('theuser') }} <i>{{ comment.target_name }}</i>
              </div>
              <div
                v-if="comment.mutation_type === 'PROMOTE_ADMIN'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasgranted') }} {{ $t('adminrights') }} {{ $t('to') }} <i>{{ comment.target_name }}</i>
              </div>
              <div
                v-if="comment.mutation_type === 'DEMOTE_ADMIN'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasremoved') }} {{ $t('adminrights') }} {{ $t('to') }} <i>{{ comment.target_name }}</i>
              </div>

              <!-- LEAVE_ALBUM -->
              <div
                v-if="comment.mutation_type === 'LEAVE_ALBUM'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasleft') }}
              </div>

              <!-- CREATE_ALBUM -->
              <div
                v-if="comment.mutation_type === 'CREATE_ALBUM'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hascreated') }} {{ $t('thealbum') }}
              </div>

              <!-- EDIT_ALBUM -->
              <div
                v-if="comment.mutation_type === 'EDIT_ALBUM'"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasedited') }} {{ $t('thealbum') }}
              </div>

              <!-- ADD STUDY IN FAVORITES -->
              <div
                v-if="comment.mutation_type === 'ADD_FAV' && comment.study"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('addalbum') }} {{ $t('thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
              </div>

              <!-- ADD STUDY IN FAVORITES -->
              <div
                v-if="comment.mutation_type === 'REMOVE_FAV' && comment.study"
                class=" flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removealbum') }} {{ $t('thestudy') }} <b>{{ comment.study.description ? comment.study.description : comment.study.UID }}</b>
              </div>

              <div
                v-if="comment.mutation_type === 'CREATE_REPORT_PROVIDER' && comment.report_provider"
                class=" flex-grow-1 bd-highlight"
              >
                {{ $t('createreportprovider', {user: comment.origin_name, reportname: comment.report_provider.name}) }}
              </div>

              <div
                v-if="comment.mutation_type === 'EDIT_REPORT_PROVIDER' && comment.report_provider"
                class=" flex-grow-1 bd-highlight"
              >
                {{ $t('editreportprovider', {user: comment.origin_name, reportname: comment.report_provider.name}) }}
              </div>

              <div
                v-if="comment.mutation_type === 'DELETE_REPORT_PROVIDER' && comment.report_provider"
                class=" flex-grow-1 bd-highlight"
              >
                {{ $t('deletereportprovider', {user: comment.origin_name, reportname: comment.report_provider.name}) }}
              </div>

              <div
                v-if="comment.mutation_type === 'NEW_REPORT' && comment.report_provider"
                class=" flex-grow-1 bd-highlight"
              >
                {{ $t('newreport', {user: comment.origin_name, reportname: comment.report_provider.name, study: comment.study.description ? comment.study.description : comment.study.UID}) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="scope === 'studies' || album.is_admin || album.write_comments"
    >
      <div class="row mt-4 justify-content-center">
        <div class="col-sm-6 col-md-4 text-sm-left text-md-right">
          <b-form-checkbox
            class="pt-1"
            inline
            @change="SetEnabledVariables()"
          >
            {{ $t("checkprivateuser") }}
          </b-form-checkbox>
        </div>
        <div class="col-sm-6 col-md-4">
          <add-user
            :id="id ? id : album.album_id"
            ref="privateuser"
            :scope="scope"
            :enable-add="enablePrivate"
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
                class="form-control form-control-sm"
                :placeholder="$t('writecomment')"
                rows="2"
                maxlength="1024"
                :readonly="disabledText"
                @keydown.enter.prevent="addComment"
                @click="checkUserFromTextarea"
              />
              <div class="input-group-append">
                <button
                  title="send comment"
                  type="submit"
                  class="btn btn-primary"
                  :disabled="newComment.comment.length < 1 || disabledText"
                >
                  <v-icon name="paper-plane" />
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import AddUser from '@/components/user/AddUser';

export default {
  name: 'CommentsAndNotifications',
  components: { AddUser },
  props: {
    scope: {
      type: String,
      required: true,
    },
    id: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      newComment: {
        comment: '',
        to_user: '',
      },
      includeNotifications: false,
      privateUser: '',
      messageSend: false,
      enablePrivate: false,
      disabledText: false,
    };
  },
  computed: {
    ...mapGetters({
      album: 'album',
    }),
    comments() {
      return this.$store.getters.getCommentsByUID(this.id);
    },
    container_id() {
      return (this.scope === 'album') ? 'album_comment_container' : `study_${this.id.replace(/\./g, '_')}_comment_container`;
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
  created() {
    this.getComments();
  },
  methods: {
    checkUserFromTextarea() {
      if (this.disabledText) {
        this.$refs.privateuser.checkUser();
      }
    },
    SetEnabledVariables() {
      this.enablePrivate = !this.enablePrivate;
      this.disabledText = !this.disabledText;
    },
    setPrivateUser(user) {
      if (user !== '') {
        this.disabledText = false;
        this.privateUser = user;
      } else {
        this.disabledText = this.enablePrivate;
      }
    },
    addComment() {
      if (this.newComment.comment.length >= 1) {
        const queries = {
          comment: this.newComment.comment,
        };
        if (this.enablePrivate) {
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
      const params = {
        StudyInstanceUID: this.id,
        queries,
      };

      this.$store.dispatch('postStudyComment', params).then((res) => {
        if (res.status === 204) {
          this.$snotify.success(this.$t('commentpostsuccess'));
          this.newComment.comment = '';
          this.$store.dispatch('getStudyComments', { StudyInstanceUID: this.id }).then(() => {
            this.scrollBottom();
          });
        }
      }).catch((res) => {
        this.$snotify.error(`${this.$t('sorryerror')}: ${res}`);
        this.newComment.comment = '';
      });
    },
    addAlbumComment(queries) {
      const params = {
        album_id: this.id,
        queries,
      };

      this.$store.dispatch('postAlbumComment', params).then((res) => {
        if (res.status === 204) {
          this.$snotify.success(this.$t('commentpostsuccess'));
          this.newComment.comment = '';
          this.getComments();
        }
      }).catch((res) => {
        this.$snotify.error(`${this.$t('sorryerror')}: ${res}`);
        this.newComment.comment = '';
      });
    },
    getComments() {
      const types = (this.includeNotifications) ? undefined : { types: 'comments' };
      if (this.scope === 'album') {
        this.$store.dispatch('getAlbumComments', { album_id: this.id, queries: types }).then(() => {
          this.scrollBottom();
        });
      } else if (this.scope === 'studies') {
        this.$store.dispatch('getStudyComments', { StudyInstanceUID: this.id }).then(() => {
          this.scrollBottom();
        });
      }
    },
    splitComment(comment) {
      return comment.split('\n');
    },
    scrollBottom() {
      const container = this.$el.querySelector(`#${this.container_id}`);
      container.scrollTop = container.scrollHeight;
    },
  },
};

</script>

<style scoped>

</style>
