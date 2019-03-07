/* eslint-disable */

<i18n>
{
	"en": {
		"userlist": "List of users",
		"add_user": "Invite a user",
		"add_series": "Add Studies / Series",
		"download_series": "Download Studies / Series",
		"send_series": "Add to album / inbox",
		"delete_series": "Remove Studies / Series",
		"write_comments": "Write Comments",
		"albumuseraddsuccess": "User successfully added to the album",
		"albumuserdeletesuccess": "Access to the album has been successfully removed",
		"usersettoadmin": "User has admin rights",
		"usernotsettoadmin": "User no longer has admin rights",
		"Unknown user": "Unknown user",
    "usersettings": "Album user settings"
	},
	"fr": {
		"userlist": "Liste d'utilisateurs",
		"add_user": "Inviter un utilisateur",
		"add_series": "Ajouter une étude / série",
		"download_series": "Télécharger une étude / série",
		"send_series": "Ajouter à un album / inbox",
		"delete_series": "Supprimer une étude / série",
		"write_comments": "Commenter",
		"albumuseraddsuccess": "L'utilisateur a été ajouté avec succès à l'album",
		"albumuserdeletesuccess": "L'accès à l'album a été supprimé avec succès",
		"usersettoadmin": "L'utilisateur a des droits admin",
		"usernotsettoadmin": "L'utilisateur n'a plus de droits admin",
		"Unknown user": "Utilisateur inconnu",
    "usersettings": "Réglages des utilisateurs de l'album"
	}
}
</i18n>

<template>
  <div class="container">
    <h3
      v-if="!form_add_user"
      class="pointer"
      style="width: 100%"
    >
      {{ $t('userlist') }}<button
        v-if="album.add_user||album.is_admin"
        class="btn btn-secondary float-right"
        @click="addUser()"
      >
        <v-icon
          name="user-plus"
          scale="1"
          class="mr-2"
        />{{ $t('add_user') }}
      </button>
    </h3>
    <div
      v-if="form_add_user"
      class="card"
    >
      <div class="card-body">
        <form @submit.prevent="addUser">
          <div class="input-group mb-2">
            <div>
              <input
                v-model="new_user_name"
                type="email"
                class="form-control"
                autofocus
                :placeholder="'email '+$t('user')"
              >
            </div>
            <div class="input-group-append">
              <button
                class="btn btn-primary"
                type="submit"
                :disabled="!validEmail(new_user_name)"
              >
                {{ $t('add') }}
              </button>
              <button
                class="btn btn-secondary"
                type="reset"
                tabindex="0"
                @keyup.esc="new_user_name=''"
                @click="new_user_name='';form_add_user=!form_add_user"
              >
                {{ $t('cancel') }}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <album-users
      :album="album"
      :users="users"
      :show-delete-user="true"
      :show-change-role="true"
    />

    <fieldset class="user_settings">
      <legend>{{ $t('usersettings') }}</legend>
      <div
        v-for="(label,idx) in userSettings"
        :key="idx"
        class="row form-group"
        :class="(label=='send_series')?'offset-1':''"
      >
        <div>
          <toggle-button
            v-if="album.is_admin"
            v-model="album[label]"
            :labels="{checked: 'Yes', unchecked: 'No'}"
            :disabled="(!album.download_series && !album.send_series && label=='send_series')"
            :sync="true"
            @change="patchAlbum(label)"
          />
          <v-icon
            v-if="!album.is_admin && !album[label]"
            name="ban"
            class="text-danger"
          />
          <v-icon
            v-if="!album.is_admin && album[label]"
            name="check-circle"
            class="text-success"
          />
        </div>
        <label class="ml-2">
          {{ $t(label) }}
        </label>
      </div>
    </fieldset>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import AlbumUsers from '@/components/albums/AlbumUsers'

export default {
	name: 'AlbumSettingsUser',
	components: { AlbumUsers },
	data () {
		return {
			form_add_user: false,
			new_user_name: '',
			userSettings: [
				'add_user',
				'add_series',
				'delete_series',
				'download_series',
				'send_series',
				'write_comments'
			]
		}
	},
	computed: {
		...mapGetters({
			album: 'album',
			users: 'users'
		})
	},
	created () {
		this.$store.dispatch('getAlbum', { album_id: this.$route.params.album_id })
		this.$store.dispatch('getUsers', { album_id: this.$route.params.album_id })
	},
	methods: {
		addUser () {
			if (!this.form_add_user) this.form_add_user = true
			else {
				if (this.new_user_name && this.validEmail(this.new_user_name)) {
					this.$store.dispatch('add_user_to_album', { user_name: this.new_user_name }).then(() => {
						this.$snotify.success(this.$t('albumuseraddsuccess'))
						this.new_user_name = ''
						this.form_add_user = false
						this.confirm_delete = ''
					}).catch(res => {
						this.$snotify.error(this.$t(res))
					})
				}
			}
		},
		validEmail (email) {
			var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
			return re.test(email)
		},
		patchAlbum (field) {
			let params = {}
			params[field] = this.album[field]
			this.$store.dispatch('patchAlbum', params).catch(err => {
				console.error(err)
				this.$snotify.error(this.$t('sorryerror'))
			})
		}
	}
}

</script>

<style scoped>
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
