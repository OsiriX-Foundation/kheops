<!--
Components : AlbumButtons
Props :
	Users				Array
	album				Object
	showQuit		Boolean
	showDelete	Boolean
-->
<i18n>
{
	"en":{
		"albumdeletesuccess": "Album deleted successfully",
		"albumquitsuccess": "Album quit successfully",
		"delete": "Delete album",
		"quit": "Leave album",
		"confirm": "Confirm",
		"cancel": "Cancel",
		"lastuser": "You are the last user in the album, if you quit, the album is delete.",
		"delalbum": "Are you sure you want to delete the album?",
		"quitalbum": "Are you sure you want to leave the album?",
		"lastadmin": "You are the last admin, you can choose to define a new admin or let the album without admin."
	},
	"fr": {
		"albumdeletesuccess": "Album supprimé avec succès",
		"albumquitsuccess": "Album quitté avec succès",
		"delete": "Effacer l'album",
		"quit": "Quitter l'album",
		"confirm": "Confirmer",
		"cancel": "Annuler",
		"lastuser": "Vous êtes le dernier utilisateur, si vous quittez l'album, il sera supprimé.",
		"delalbum": "Etes-vous sûr de vouloir supprimer l'album ?",
		"quitalbum": "Etes-vous sûr de vouloir quitter l'album ?",
		"lastadmin": "Vous êtes le dernier administrateur, vous pouvez définir un nouveau administrateur ou laisser l'album sans."
	}
}
</i18n>

<template>
  <div>
    <div
      v-if="showQuit"
    >
      <div
        v-if="album.is_admin && lastAdmin && confirmQuit"
      >
        <p
          style="color:red;"
        >
          {{ $t('lastadmin') }}
        </p>
        <album-users
          :album="album"
          :users="users"
          :show-delete-user="false"
          :show-change-role="true"
        />
      </div>
      <div
        align="right"
        class="btnalbum"
      >
        <p v-if="confirmQuit && !lastAdmin && !lastUser">
          {{ $t("quitalbum") }}
        </p>
        <p v-else-if="confirmQuit && lastUser">
          {{ $t('lastuser') }}
        </p>
        <button
          type="button"
          class="btn btn-danger"
          @click="quitAlbum"
        >
          {{ confirmQuit?$t('confirm'):$t('quit') }}
        </button>
        <button
          v-if="confirmQuit"
          type="button"
          class="btn btn-secondary"
          @click="confirmQuit=!confirmQuit"
        >
          {{ $t('cancel') }}
        </button>
      </div>
    </div>
    <div
      v-if="showDelete"
    >
      <div
        class="btnalbum"
        align="right"
      >
        <p v-if="confirmDeletion">
          {{ $t("delalbum") }}
        </p>
        <button
          type="button"
          class="btn btn-danger"
          @click="deleteAlbum"
        >
          {{ confirmDeletion?$t('confirm'):$t('delete') }}
        </button>
        <button
          v-if="confirmDeletion"
          type="button"
          class="btn btn-secondary"
          @click="confirmDeletion=!confirmDeletion"
        >
          {{ $t('cancel') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import AlbumUsers from '@/components/albums/AlbumUsers'

export default {
	name: 'AlbumButtons',
	components: { AlbumUsers },
	props: {
		album: {
			type: Object,
			required: true,
			default: () => ({})
		},
		users: {
			type: Array,
			required: true,
			default: () => ([])
		},
		showQuit: {
			type: Boolean,
			required: true,
			default: true
		},
		showDelete: {
			type: Boolean,
			required: true,
			default: true
		}
	},
	data () {
		return {
			confirmDeletion: false,
			confirmQuit: false,
			lastAdmin: false,
			lastUser: false
		}
	},
	computed: {
		...mapGetters({
			user: 'currentUser'
		})
	},
	methods: {
		deleteAlbum () {
			if (!this.confirmDeletion) this.confirmDeletion = true
			else {
				this.$store.dispatch('deleteAlbum').then(() => {
					this.$snotify.success(this.$t('albumdeletesuccess'))
					this.$router.push('/albums')
				}).catch(() => {
					this.$snotify.error(this.$t('sorryerror'))
				})
			}
		},
		quitAlbum () {
			if (!this.confirmQuit) {
				if (this.album.is_admin) {
					let last = this.users.filter(user => user.is_admin && user.user_name !== this.user.email)
					this.lastAdmin = !(last.length > 0)
				} else {
					this.lastUser = !(this.users.length > 0)
				}
				this.confirmQuit = true
			} else {
				this.$store.dispatch('quitAlbum', this.user.sub).then(() => {
					this.$snotify.success(this.$t('albumquitsuccess'))
					this.$router.push('/albums')
				}).catch(() => {
					this.$snotify.error(this.$t('sorryerror'))
				})
			}
		}
	}
}
</script>

<style scoped>
.btnalbum{
	padding: 10px;
}

</style>
