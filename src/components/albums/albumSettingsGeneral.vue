/* eslint-disable */
<i18n>
{
	"en":{
		"albumname": "Album name",
		"albumdescription": "Album description",
		"notification": "Notifications",
		"albumdeletesuccess": "Album deleted successfully"
	},
	"fr": {
		"albumname": "Nom de l'album",
		"albumdescription": "Description de l'album",
		"notification": "Notifications",
		"albumdeletesuccess": "Album supprimé avec succès"
	}
}
</i18n>

<template>
	<div class = 'container'>
		<dl>
			<dt>{{$t('albumname')}}</dt>
			<dd>
				<div v-if='edit.name=="-1"'>
					{{album.name}} <span class = 'icon-edit' @click="edit.name=album.name" v-if='album.is_admin && edit.name=="-1"'><v-icon name='pencil-alt'></v-icon></span>
				</div>
				<div v-if='edit.name!="-1"'>
					<form @submit.prevent='updateAlbum'>
						<div class="input-group mb-2">
							<div>
								<input type="text" class = 'form-control' v-model='edit.name'>
							</div>
							<div class="input-group-append">
                <button class="btn btn-primary" type="submit">{{$t('update')}}</button>
                <button class="btn btn-secondary" type="reset"  @keyup.esc='edit.name="-1"' @click='edit.name="-1"' tabindex="0">{{$t('cancel')}}</button>
							</div>
						</div>
					</form>
				</div>

			</dd>
			<dt>{{$t('albumdescription')}}<span class = 'icon-edit float-right' @click="edit.description=album.description" v-if='album.is_admin && edit.description=="-1"'><v-icon name='pencil-alt'></v-icon></span></dt>
			<dd class = 'album_description'>
				<div v-if='edit.description=="-1"' v-html='$options.filters.nl2br(album.description)'></div>
				<div v-if='edit.description!="-1"'>
					<form @submit.prevent='updateAlbum'>
						<div class="">
							<div>
								<textarea v-model='edit.description' rows='6' class = 'form-control'></textarea>
							</div>
							<div>
								<button class="btn btn-primary" type="submit">{{$t('update')}}</button>
                <button class="btn btn-secondary" type="reset"  @keyup.esc='edit.description="-1"' @click='edit.description="-1"' tabindex="0">{{$t('cancel')}}</button>
							</div>
						</div>
					</form>
				</div>
			</dd>
		</dl>
		<!--
		<dl>
			<dt>{{$t('notification')}}</dt>
			<dd style = 'margin-top: 10px'>
				<div class = 'row'>
					<div class = 'col'>
						<toggle-button v-model="album.notification_new_series" :labels="{checked: 'Yes', unchecked: 'No'}" :sync="true" @change='updateAlbum'/> <label>New Study</label>
					</div>
					<div class = 'col'>
						<toggle-button v-model="album.notification_new_comment" :labels="{checked: 'Yes', unchecked: 'No'}" :sync="true"  @change='updateAlbum'/> <label>New comment</label>
					</div>
				</div>
			</dd>
		</dl>
		-->
		<p class = 'float-right' v-if='album.is_admin'><button type = 'button' class = 'btn btn-danger' @click='deleteAlbum'>{{confirmDeletion?$t('confirmdeletion'):$t('delete')}}</button> <button type = 'button' class = 'btn btn-secondary' @click='confirmDeletion=!confirmDeletion' v-if='confirmDeletion'>{{$t('cancel')}}</button></p>
	</div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
	name: 'album_settings_general',
	data () {
		return {
			edit: {
				name: '-1',
				description: '-1'
			},
			confirmDeletion: false
		}
	},
	computed: {
		...mapGetters({
			album: 'album'
		})
	},
	methods: {
		updateAlbum () {
			if (!this.album.is_admin) {
				this.$snotify.error(this.$t('permissiondenied'))
				return
			}
			let params = {}
			_.forEach(this.edit, (v, k) => {
				if (v === -1) return
				if (this.album[k] !== v) {
					params[k] = v
				}
			})
			params.notificationNewComment = this.album.notification_new_comment
			params.notificationNewSeries = this.album.notification_new_series

			this.$store.dispatch('patchAlbum', params).then( () => {
				this.$snotify.success(this.$t('albumupdatesuccess'))
				this.edit.name = '-1'
				this.edit.description = '-1'
			}).catch( () => {
				this.$snotify.error(this.$t('sorryerror'))
			})
		},
		deleteAlbum () {
			if (!this.confirmDeletion) this.confirmDeletion = true
			else {
				this.$store.dispatch('deleteAlbum').then( () => {
					this.$snotify.success(this.$t('albumdeletesuccess'))
					this.$router.push('/albums')
				}).catch( () => {
					this.$snotify.error(this.$t('sorryerror'))
				})
			}
		}
	}
}

</script>

<style scoped>
dd span.icon-edit, dt span.icon-edit {
	margin: 0 10px;
	cursor: pointer;
}

dl {
	font-size: 125%;
}
dl label {
	font-size: 100%;
	margin-left: 20px;
}

dd.album_description{
	border: 1px solid #333;
	height: 10em;
	padding: 10px;
}

</style>

