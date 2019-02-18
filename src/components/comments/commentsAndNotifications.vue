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
		"includenotifications": "include notifications"
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
		"includenotifications": "inclure les notifications"
	}
}
</i18n>

<template>
  <div class="container">
    <div class="row justify-content-center">
      <p
        v-if="scope==&quot;album&quot;"
        class="col-sm-12 col-md-10 offset-md-1 text-right"
      >
        <label>{{ $t('includenotifications') }} </label> <toggle-button
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
            v-if="comment.event_type==&quot;Comment&quot;"
            class="card mt-3 ml-5 mr-5"
            :class="(comment.is_private)?'bg-primary':'bg-secondary'"
          >
            <div class="card-header">
              <v-icon name="user" /> {{ comment.origin_name }}<span class="float-right">
                {{ comment.post_date|formatDate }}
              </span>
            </div>
            <div
              class="card-body"
              v-html="$options.filters.nl2br(comment.comment)"
            />
          </div>

          <!-- Notifications -->

          <div
            v-if="comment.event_type==&quot;Mutation&quot;"
            class="card col-sm-10 offset-sm-2 bg-secondary mt-3 ml-5 mr-5"
          >
            <div class="d-flex">
              <!-- IMPORT_STUDY, REMOVE_STUDY : -->
              <div
                v-if="comment.mutation_type==&quot;IMPORT_STUDY&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('imported') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>
              <div
                v-if="comment.mutation_type==&quot;REMOVE_STUDY&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>

              <!-- IMPORT_SERIES, REMOVE_SERIES -->
              <div
                v-if="comment.mutation_type==&quot;IMPORT_SERIES&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('imported') }} {{ $t('theseries') }} {{ comment.series }} {{ $t('in') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>
              <div
                v-if="comment.mutation_type==&quot;REMOVE_SERIES&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('theseries') }} {{ comment.series }} {{ $t('in') }} {{ $t('thestudy') }} {{ comment.study }}
              </div>

              <!-- ADD_USER, ADD_ADMIN, REMOVE_USER, PROMOTE_ADMIN, DEMOTE_ADMIN -->
              <div
                v-if="comment.mutation_type==&quot;ADD_USER&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasadd') }} {{ $t('theuser') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type==&quot;ADD_ADMIN&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasadd') }} {{ $t('theadmin') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type==&quot;REMOVE_USER&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('removed') }} {{ $t('theuser') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type==&quot;PROMOTE_ADMIN&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasgranted') }} {{ $t('adminrights') }} {{ $t('to') }} {{ comment.target_name }}
              </div>
              <div
                v-if="comment.mutation_type==&quot;DEMOTE_ADMIN&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasremoved') }} {{ $t('adminrights') }} {{ $t('to') }} {{ comment.target_name }}
              </div>

              <!-- LEAVE_ALBUM -->
              <div
                v-if="comment.mutation_type==&quot;LEAVE_ALBUM&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasleft') }}
              </div>

              <!-- CREATE_ALBUM -->
              <div
                v-if="comment.mutation_type==&quot;CREATE_ALBUM&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hascreated') }} {{ $t('thealbum') }}
              </div>

              <!-- EDIT_ALBUM -->
              <div
                v-if="comment.mutation_type==&quot;EDIT_ALBUM&quot;"
                class="p-2 flex-grow-1 bd-highlight"
              >
                <i>{{ comment.origin_name }}</i> {{ $t('hasedited') }} {{ $t('thealbum') }}
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
      <div class="col-sm-12 col-md-10 offset-md-1">
        <form @submit.prevent="addComment">
          <div class="row justify-content-center">
            <div class="col-9 mb-2">
              <textarea
                v-model="newComment.comment"
                class="form-control"
                rows="6"
                placeholder="Use @... for a specific user"
              />
            </div>
            <div class="col-auto">
              <button
                type="submit"
                class="btn btn-lg btn-primary"
                :disabled="newComment.comment.length < 2"
              >
                <v-icon name="paper-plane" />{{ $t('send') }}
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

export default {
	name: 'CommentsAndNotifications',
	props: {
		scope: {
			type: String,
			required: true
		},
		id: {
			type: String,
			required: true
		} },
	data () {
		return {
			newComment: {
				comment: '',
				to_user: ''
			},
			includeNotifications: false
		}
	},
	computed: {
		...mapGetters({
			album: 'album',
			studies: 'studies',
			album_comments: 'album_comments',
			users: 'users'
		}),
		comments () {
			if (this.scope === 'album') return this.album_comments

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
	created () {
		this.getComments()
		if (this.album.album_id) this.$store.dispatch('getUsers')
	},
	methods: {
		addComment () {
			if (this.newComment.comment.length > 2) {
				if (this.newComment.comment.indexOf('@') > -1) {
					if (this.users.length) { // album
						_.forEach(this.users, user => {
							if (this.newComment.comment.indexOf(user.user_name) > -1) {
								this.newComment.to_user = user.user_name
								this.newComment.comment = this.newComment.comment.replace('@' + user.user_name, '').trim()
							}
						})
					} else {
						let atIdx = this.newComment.comment.indexOf('@')
						let end = this.newComment.comment.substr(atIdx).length
						let match = this.newComment.comment.substr(atIdx).match(/\s/)
						if (match) end = match.index
						this.newComment.to_user = this.newComment.comment.substr(atIdx + 1, end - 1)
						this.$store.dispatch('checkUser', this.newComment.to_user).then(res => {
							if (res) {
								this.newComment.comment = this.newComment.comment.replace('@' + this.newComment.to_user, '').trim()
								this.newComment.to_user = res
							} else {
								this.newComment.to_user = ''
							}
						})
					}
				}
				if (this.scope === 'album') {
					this.$store.dispatch('postAlbumComment', this.newComment).then(() => {
						this.$snotify.success('commentpostsuccess')
						this.newComment.comment = ''
						this.newComment.to_user = ''
					}).catch(res => {
						this.$snotify.error(this.$t('sorryerror') + ': ' + res)
						this.newComment.comment = ''
						this.newComment.to_user = ''
					})
				} else if (this.scope === 'studies') {
					this.$store.dispatch('postStudiesComment', { StudyInstanceUID: this.id, comment: this.newComment }).then(() => {
						this.$snotify.success('commentpostsuccess')
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
					let container = this.$el.querySelector('#album_comment_container')
					container.scrollTop = container.scrollHeight
				})
			} else if (this.scope === 'studies') {
				this.$store.dispatch('getStudiesComments', { StudyInstanceUID: this.id, type: type }).then(() => {
					let container = this.$el.querySelector('#study_' + this.id.replace(/\./g, '_') + '_comment_container')
					container.scrollTop = container.scrollHeight
				})
			}
		}
	}
}

</script>

<style scoped>

</style>

