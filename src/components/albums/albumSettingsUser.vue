/* eslint-disable */

<i18n>
{
	"en": {
		"username": "User name",
		"user": "user",
		"changerole": "change role to",
		"add_user": "Add User",
		"add_series": "Add Studies / Series",
		"download_series": "Download Studies / Series",
		"send_series": "Get Studies / Series",
		"delete_series": "Remove Studies / Series",
		"write_comments": "Write Comments",
		"albumuseraddsuccess": "User added successfully to the album",
		"albumuserdeletesuccess": "Access to the album has been successfully removed",
		"usersettoadmin": "User has admin rights",
		"usernotsettoadmin": "User has no more admin rights",
		"Unknown user": "Unknown user"
	},
	"fr": {
		"username": "Utilisateur",
		"user": "Utilisateur",
		"changerole": "changer le rôle pour",
		"add_user": "Ajouter un utilisateur",
		"add_series": "Ajouter une étude / série",
		"download_series": "Télécharger une étude / série",
		"send_series": "Récupérer une étude / série",
		"delete_series": "Supprimer une étude / série",
		"write_comments": "Commenter",
		"albumuseraddsuccess": "L'utilisateur a été ajouté avec succès à l'album",
		"albumuserdeletesuccess": "L'accès à l'album a été supprimé avec succès",
		"usersettoadmin": "L'utilisateur a des droits admin",
		"usernotsettoadmin": "L'utilisateur n'a plus de droits admin",
		"Unknown user": "Utilisateur inconnu"
	}
}
</i18n>

<template>
	<div class = 'container'>
		<h3 class = 'pointer' v-if = '!form_add_user'><span @click='addUser()'><v-icon name='user-plus' scale='2'></v-icon>{{$t('add_user')}}</span></h3>
		<div class = 'card' v-if='form_add_user'>
			<div class = 'card-body'>
				<form @submit.prevent='addUser'>
					<div class="input-group mb-2">
						<div>
							<input type="email" class = 'form-control' v-model='new_user_name' autofocus :placeholder="'email '+$t('user')">
						</div>
						<div class="input-group-append">
              <button class="btn btn-primary" type="submit" :disabled='!validEmail(new_user_name)'>{{$t('add')}}</button>
              <button class="btn btn-secondary" type="reset"  @keyup.esc='new_user_name=""' @click='new_user_name="";form_add_user=!form_add_user' tabindex="0">{{$t('cancel')}}</button>
						</div>
					</div>
				</form>
			</div>
		</div>
		<div class = 'user-table-container'>
			<table class = 'table'>
				<thead>
					<tr>
						<th>{{$t('username')}}</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<tr v-for='user in users' :key="user.user_name">
						<td>{{user.user_name}} <span v-if='user.is_admin'>(Admin)</span></td>
						<td class = 'showOnTrHover text-right'>
							<div v-if='confirm_delete!=user.user_name' class = 'user_actions'>
								<a @click.stop='toggleAdmin(user)'>{{$t('changerole')}} {{(user.is_admin)?$t('user'):"admin"}}</a> <a @click.stop='deleteUser(user)' v-if='album.is_admin' class='text-danger' style='margin-left: 20px'><v-icon name = 'trash'></v-icon></a>
							</div>
							<div v-if='confirm_delete==user.user_name'>
								<div class = 'btn-group'><button type = 'button' @click.stop='deleteUser(user)' class = 'btn btn-sm btn-danger'>{{$t('confirm')}}</button><button type = 'button' @click.stop='confirm_delete=""' class = 'btn btn-sm btn-secondary'>{{$t('cancel')}}</button></div>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>

		<fieldset v-if='album.is_admin' class = 'user_settings'>
			<legend>{{$t('usersettings')}}</legend>
			<div class = 'row form-group' v-for='(label,idx) in userSettings' :key="idx" :class = '(label=="send_series")?"offset-1":""'>
				<div><toggle-button v-model="album[label]" :labels="{checked: 'Yes', unchecked: 'No'}" :disabled="(!album.download_series && label=='send_series')" :sync="true" @change='patchAlbum(label)'/></div>
				<label>{{$t(label)}}</label>
			</div>
		</fieldset>

	</div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
	name: 'album_settings_user',
	data () {
		return {
			confirm_delete: '',
			form_add_user: false,
			new_user_name: '',
			userSettings: [
				'add_user',
				'add_series',
				'download_series',
				'send_series',
				'delete_series',
				'write_comments'
			]
		}
	},
	methods: {
		addUser () {
			if (!this.form_add_user) this.form_add_user = true
			else {
				if (this.new_user_name && this.validEmail(this.new_user_name)) {
					this.$store.dispatch('add_user_to_album', { user_name: this.new_user_name }).then(res => {
						this.$snotify.success(this.$t('albumuseraddsuccess'))
					}).catch(res => {
						this.$snotify.error(this.$t(res))
					})
				}
			}
		},
		toggleAdmin (user) {
			user.is_admin = !user.is_admin
			this.$store.dispatch('toggleAlbumUserAdmin', user).then(res => {
				let message = (user.is_admin) ? this.$t('usersettoadmin') : this.$t('usernotsettoadmin')
				this.$snotify.success(message)
			}).catch(err => {
				console.error(err)
				this.$snotify.error(this.$t('sorryerror'))
			})
		},
		deleteUser (user) {
			if (this.confirm_delete !== user.user_name) this.confirm_delete = user.user_name
			else {
				this.$store.dispatch('remove_user_from_album', { user_name: user.user_name }).then(res => {
					this.$snotify.success(this.$t('albumuserdeletesuccess'))
				})
			}
		},
		validEmail (email) {
			var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
			return re.test(email)
		},
		patchAlbum (field) {
			let params = { field: this.album[field] }
			this.$store.dispatch('patchAlbum', params).then(res => {
				this.$snotify.success(this.$t('albumupdatesuccess'))
			}).catch(err => {
				console.error(err)
				this.$snotify.error(this.$t('sorryerror'))
			})
		}
	},
	computed: {
		...mapGetters({
			album: 'album',
			users: 'users'
		})
	},
	created () {
		this.$store.dispatch('getUsers')
	},
	watch: {
	}
}

</script>

<style>
div.user-table-container{
	min-height: 200px;
	padding: 50px 0;
}

a {
	cursor: pointer;
}
td.showOnTrHover div.user_actions{
	visibility: hidden;
}

tr:hover  td.showOnTrHover div.user_actions{
	visibility: visible;
}
input::placeholder {
	text-transform: lowercase;
}
fieldset.user_settings {
	border: 1px solid #333;
	padding: 20px;
	background-color: #303030 ;
}

fieldset.user_settings legend{
	padding: 0 20px;
	width: auto;
}
</style>
