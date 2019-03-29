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
    "writecomment": "Write your comment here"
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
    "writecomment": "Ecrivez votre commentaire ici"
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
            class="card mt-3 ml-5 mr-5"
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
                {{ comment.post_date|formatDate }}
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
            class="card col-sm-10 offset-sm-2 bg-secondary mt-3 ml-5 mr-5"
          >
            <div class="d-flex">
              <!-- IMPORT_STUDY, REMOVE_STUDY : -->
              <div
                v-if="comment.mutation_type === 'IMPORT_STUDY'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('imported') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>
              <div
                v-if="comment.mutation_type === 'REMOVE_STUDY'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>

              <!-- IMPORT_SERIES, REMOVE_SERIES -->
              <div
                v-if="comment.mutation_type === 'IMPORT_SERIES'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('imported') }} {{ $t('theseries') }} {{ comment.series }} {{ $t('in') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>
              <div
                v-if="comment.mutation_type === 'REMOVE_SERIES'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('theseries') }} {{ comment.series }} {{ $t('in') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>

              <!-- ADD_USER, ADD_ADMIN, REMOVE_USER, PROMOTE_ADMIN, DEMOTE_ADMIN -->
              <div
                v-if="comment.mutation_type === 'ADD_USER'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasadd') }} {{ $t('theuser') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type === 'ADD_ADMIN'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasadd') }} {{ $t('theadmin') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type === 'REMOVE_USER'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('theuser') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type === 'PROMOTE_ADMIN'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasgranted') }} {{ $t('adminrights') }} {{ $t('to') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type === 'DEMOTE_ADMIN'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasremoved') }} {{ $t('adminrights') }} {{ $t('to') }} {{ comment.target_name }}
              </div>

              <!-- LEAVE_ALBUM -->
              <div
                v-if="comment.mutation_type === 'LEAVE_ALBUM'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasleft') }}
              </div>

              <!-- CREATE_ALBUM -->
              <div
                v-if="comment.mutation_type === 'CREATE_ALBUM'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hascreated') }} {{ $t('thealbum') }}
              </div>

              <!-- EDIT_ALBUM -->
              <div
                v-if="comment.mutation_type === 'EDIT_ALBUM'"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasedited') }} {{ $t('thealbum') }}
              </div>

              <!-- ADD STUDY IN FAVORITES -->
              <div
                v-if="comment.mutation_type === 'ADD_FAV' && comment.study"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('addalbum') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>

              <!-- ADD STUDY IN FAVORITES -->
              <div
                v-if="comment.mutation_type === 'REMOVE_FAV' && comment.study"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removealbum') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>

              <div class="bd-highlight">
                <small style="white-space: nowrap">
                  {{ comment.post_date|formatDate }}
                </small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row mt-4 justify-content-center">
      <div class="col-sm-6 col-md-4 text-sm-left text-md-right">
        <b-form-checkbox
          class="pt-1"
          inline
          @change="SetEnabledVariables()"
        >
          Send private message to
        </b-form-checkbox>
      </div>
      <div class="col-sm-6 col-md-4">
        <add-user
          :scope="scope"
          :album-id="album.album_id"
          :enable-add="enablePrivate"
          @private-user="setPrivateUser"
        />
      </div>
    </div>
    <div class="row mt-2 justify-content-center">
      <div class="col-sm-12 col-md-10 offset-md-1">
        <form
          v-if="scope === 'studies' || album.is_admin || album.write_comments"
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
              :disabled="disabledText"
              @keydown.enter.prevent="addComment"
            />
            <div class="input-group-append">
              <button
                title="send comment"
                type="submit"
                class="btn btn-primary"
                :disabled="newComment.comment.length < 2 || disabledText"
              >
                <v-icon name="paper-plane" />
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import AddUser from '@/components/user/AddUser'

export default {
	name: 'CommentsAndNotifications',
	components: { AddUser },
	props: {
		scope: {
			type: String,
			required: true
		},
		id: {
			type: String,
			required: true
		}
	},
	data () {
		return {
			newComment: {
				comment: '',
				to_user: ''
			},
			includeNotifications: false,
			privateUser: '',
			messageSend: false,
			enablePrivate: false,
			disabledText: false
		}
	},
	computed: {
		...mapGetters({
			album: 'album',
			studies: 'studies',
			albumComments: 'albumComments',
			users: 'users'
		}),
		comments () {
			if (this.scope === 'album') return this.albumComments
			let studyIdx = _.findIndex(this.studies, s => { return s.StudyInstanceUID[0] === this.id })
			if (studyIdx > -1) {
				return this.studies[studyIdx].comments
			}
			return []
		},
		container_id () {
			return (this.scope === 'album') ? 'album_comment_container' : 'study_' + this.id.replace(/\./g, '_') + '_comment_container'
		}
	},
	watch: {
		disabledText: {
			handler: function (disabledText) {
				if (!this.disabledText) {
					let textcomment = this.$refs.textcomment
					setTimeout(function () { textcomment.focus() }, 0)
				}
			}
		}
	},
	created () {
		this.getComments()
		if (this.album.album_id) this.$store.dispatch('getUsers')
	},
	methods: {
		SetEnabledVariables () {
			this.enablePrivate = !this.enablePrivate
			this.disabledText = !this.disabledText
		},
		setPrivateUser (user) {
			if (user !== '') {
				this.disabledText = false
				this.privateUser = user
			} else {
				this.disabledText = this.enablePrivate
			}
		},
		addComment () {
			if (this.newComment.comment.length > 2) {
				if (this.enablePrivate) {
					this.newComment.to_user = this.privateUser
				}

				if (this.scope === 'album') {
					let params = {
						type: (this.includeNotifications) ? '' : 'comments',
						query: this.newComment
					}
					this.$store.dispatch('postAlbumComment', params).then(() => {
						this.$snotify.success(this.$t('commentpostsuccess'))
						this.newComment.comment = ''
						this.newComment.to_user = ''
					}).catch(res => {
						this.$snotify.error(this.$t('sorryerror') + ': ' + res)
						this.newComment.comment = ''
						this.newComment.to_user = ''
					})
				} else if (this.scope === 'studies') {
					this.$store.dispatch('postStudiesComment', { StudyInstanceUID: this.id, comment: this.newComment }).then(() => {
						this.$snotify.success(this.$t('commentpostsuccess'))
						this.newComment.comment = ''
						this.newComment.to_user = ''
					}).catch(res => {
						this.$snotify.error(this.$t('sorryerror') + ': ' + res)
						this.newComment.comment = ''
						this.newComment.to_user = ''
					})
				}
			}
		},
		getComments () {
			let type = (this.includeNotifications) ? '' : 'comments'
			if (this.scope === 'album') {
				this.$store.dispatch('getAlbumComments', { type: type }).then(() => {
					this.scrollBottom()
				})
			} else if (this.scope === 'studies') {
				this.$store.dispatch('getStudiesComments', { StudyInstanceUID: this.id, type: type }).then(() => {
					this.scrollBottom()
				})
			}
		},
		splitComment (comment) {
			return comment.split('\n')
		},
		scrollBottom () {
			let container = this.$el.querySelector(`#${this.container_id}`)
			container.scrollTop = container.scrollHeight
		}
	}
}

</script>

<style scoped>

</style>

