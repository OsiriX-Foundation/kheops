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
		"removealbum": "remove as favorite"
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
		"removealbum": "a enlevé des favories"
	}
}
</i18n>

<template>
	<div class = 'container'>
		<div class = 'row justify-content-center'>
			<p class = 'col-sm-12 col-md-10 offset-md-1 text-right' v-if='scope=="album"'><label>{{$t('includenotifications')}} </label> <toggle-button v-model="includeNotifications" :labels="{checked: 'Yes', unchecked: 'No'}" :sync="true" @change='getComments'/></p>

			<div class="card col-sm-12 col-md-10 offset-md-1 pt-3 pb-3" style = 'max-height: 600px; overflow-y: scroll;' :id = 'container_id'>

				<div v-for="comment in comments" v-bind:key="comment.id">

					<!-- Comments -->

					<div class="card mt-3 ml-5 mr-5" :class="(comment.is_private)?'bg-primary':'bg-secondary'"  v-if='comment.event_type=="Comment"'>
						<div class = 'card-header'><v-icon name = 'user'></v-icon> {{comment.origin_name}}<span class = 'float-right'>{{comment.post_date|formatDate}}</span></div>
						<div class = 'card-body' v-html="$options.filters.nl2br(comment.comment)">
						</div>
					</div>

					<!-- Notifications -->

					<div class = 'card col-sm-10 offset-sm-2 bg-secondary mt-3 ml-5 mr-5' v-if='comment.event_type=="Mutation"'>

						<div class="d-flex">

							<!-- IMPORT_STUDY, REMOVE_STUDY : -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="IMPORT_STUDY"'><i>{{comment.origin_name}}</i> {{$t('imported')}} {{$t('thestudy')}} {{comment.study}}</div>
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="REMOVE_STUDY"'><i>{{comment.origin_name}}</i> {{$t('removed')}} {{$t('thestudy')}} {{comment.study}}</div>

							<!-- IMPORT_SERIES, REMOVE_SERIES -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="IMPORT_SERIES"'><i>{{comment.origin_name}}</i> {{$t('imported')}} {{$t('theseries')}} {{comment.series}} {{$t('in')}} {{$t('thestudy')}} {{comment.study}}</div>
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="REMOVE_SERIES"'><i>{{comment.origin_name}}</i> {{$t('removed')}} {{$t('theseries')}} {{comment.series}} {{$t('in')}} {{$t('thestudy')}} {{comment.study}}</div>

							<!-- ADD_USER, ADD_ADMIN, REMOVE_USER, PROMOTE_ADMIN, DEMOTE_ADMIN -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="ADD_USER"'><i>{{comment.origin_name}}</i> {{$t('hasadd')}} {{$t('theuser')}} {{comment.target_name}}</div>
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="ADD_ADMIN"'><i>{{comment.origin_name}}</i> {{$t('hasadd')}} {{$t('theadmin')}} {{comment.target_name}}</div>
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="REMOVE_USER"'><i>{{comment.origin_name}}</i> {{$t('removed')}} {{$t('theuser')}} {{comment.target_name}}</div>
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="PROMOTE_ADMIN"'><i>{{comment.origin_name}}</i> {{$t('hasgranted')}} {{$t('adminrights')}} {{$t('to')}} {{comment.target_name}}</div>
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="DEMOTE_ADMIN"'><i>{{comment.origin_name}}</i> {{$t('hasremoved')}} {{$t('adminrights')}} {{$t('to')}} {{comment.target_name}}</div>

							<!-- LEAVE_ALBUM -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="LEAVE_ALBUM"'><i>{{comment.origin_name}}</i> {{$t('hasleft')}} </div>

							<!-- CREATE_ALBUM -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="CREATE_ALBUM"'><i>{{comment.origin_name}}</i> {{$t('hascreated')}} {{$t('thealbum')}} </div>

							<!-- EDIT_ALBUM -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="EDIT_ALBUM"'><i>{{comment.origin_name}}</i> {{$t('hasedited')}} {{$t('thealbum')}} </div>

							<!-- ADD STUDY IN FAVORITES -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="ADD_FAV" && comment.study'><i>{{comment.origin_name}}</i> {{$t('addalbum')}} {{$t('thestudy')}} {{comment.study}} </div>

							<!-- ADD STUDY IN FAVORITES -->
							<div class="p-2 flex-grow-1 bd-highlight" v-if = 'comment.mutation_type=="REMOVE_FAV" && comment.study'><i>{{comment.origin_name}}</i> {{$t('removealbum')}} {{$t('thestudy')}} {{comment.study}} </div>

							<div class="bd-highlight"><small style = 'white-space: nowrap'>{{comment.post_date|formatDate}}</small></div>
						</div>

					</div>

				</div>
			</div>
		</div>
		<div class = 'row mt-4 justify-content-center'>
			<div class = 'col-sm-12 col-md-10 offset-md-1'>
				<form @submit.prevent='addComment'>
					<div class = 'row justify-content-center'>
						<div class = 'col-9 mb-2'>
							<textarea class = 'form-control' rows=6 v-model='newComment.comment' placeholder="Use @... for a specific user"></textarea>
						</div>
						<div class = 'col-auto'><button type = 'submit' class = 'btn btn-lg btn-primary' :disabled="newComment.comment.length < 2" ><v-icon name='paper-plane'></v-icon>{{$t('send')}}</button></div>
					</div>
				</form>
			</div>
		</div>
	</div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
	name: 'commentsAndNotifications',
	props: ['scope', 'id'],
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
	},
	created () {
		this.getComments()
		if (this.album.album_id) this.$store.dispatch('getUsers')
	}
}

</script>

<style scoped>

</style>

