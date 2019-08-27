<!--
Components : AlbumUsers
Props :
	Users						Array
	album						Object
	showDeleteUser	Boolean
	showChangeRole	Boolean
-->
<i18n>
{
	"en": {
		"username": "User name",
		"user": "user",
		"changerole": "change role to",
		"albumuserdeletesuccess": "Access to the album has been successfully removed",
		"usernotsettoadmin": "User no longer has admin rights",
		"usersettoadmin": "User has admin rights",
		"warningtoggleadmin": "Warning! Do you really want to revoke your admin role? ",
		"remove": "Remove user",
    "warningtoggledelete": "Do you realy want to delete this user ?",
    "Admin": "Data steward",
    "admin": "data steward"
	},
	"fr": {
		"username": "Utilisateur",
		"user": "utilisateur",
		"changerole": "changer le rôle pour",
		"albumuserdeletesuccess": "L'accès à l'album a été supprimé avec succès",
		"usernotsettoadmin": "L'utilisateur n'a plus de droits admin",
		"usersettoadmin": "L'utilisateur a des droits admin",
		"warningtoggleadmin": "Attention ! Voulez-vous vraiment renoncer à vos droits admin ?  ",
		"remove": "Retirer l'utilisateur",
    "warningtoggledelete": "Voulez-vous vraiment supprimer cet utilisateur ?",
    "Admin": "Gardien des données",
    "admin": "gardien des données"
	}
}
</i18n>
<template>
  <div class="user-table-container">
    <table class="table">
      <thead>
        <tr>
          <th>{{ $t('username') }}</th>
          <th v-if="album.is_admin" />
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="user in users"
          :key="user.user_name"
        >
          <td>
            {{ user.user_name }}
            <span
              v-if="user.is_admin"
              style="color:#13B98B"
            >
              {{ $t("Admin") }}
            </span>
            <!-- on mobile -->
            <div
              v-if="album.is_admin"
              class="d-sm-none"
            >
              <div
                v-if="confirmDelete !== user.user_name && confirmResetAdmin !== user.user_name"
                class="user_actions"
              >
                <a
                  v-if="showChangeRole"
                  @click.stop="toggleAdmin(user)"
                >
                  {{ $t('changerole') }} {{ (user.is_admin)?$t('user'):$t("admin") }}
                  <v-icon	name="user" />
                </a>
                <br>
                <a
                  v-if="album.is_admin && showDeleteUser"
                  class="text-danger"
                  @click.stop="deleteUser(user)"
                >
                  {{ $t('remove') }}
                  <v-icon name="trash" />
                </a>
              </div>
              <div v-if="confirmDelete === user.user_name">
                <span class="text-danger mr-2">
                  {{ $t("warningtoggledelete") }}
                </span>
                <br>
                <div class="btn-group">
                  <button
                    type="button"
                    class="btn btn-sm btn-danger"
                    @click.stop="deleteUser(user)"
                  >
                    {{ $t('confirm') }}
                  </button><button
                    type="button"
                    class="btn btn-sm btn-secondary"
                    @click.stop="confirmDelete=''"
                  >
                    {{ $t('cancel') }}
                  </button>
                </div>
              </div>
              <div v-if="confirmResetAdmin === user.user_name">
                <span class="text-danger mr-2">
                  {{ $t("warningtoggleadmin") }}
                </span>
                <br>
                <div class="btn-group">
                  <button
                    type="button"
                    class="btn btn-sm btn-danger"
                    @click.stop="toggleAdmin(user)"
                  >
                    {{ $t('confirm') }}
                  </button><button
                    type="button"
                    class="btn btn-sm btn-secondary"
                    @click.stop="confirmResetAdmin=''"
                  >
                    {{ $t('cancel') }}
                  </button>
                </div>
              </div>
            </div>
            <!-- end on mobile -->
          </td>
          <td
            v-if="album.is_admin"
            class="text-right d-none d-sm-table-cell"
            :class="mobiledetect ? '' : 'showOnTrHover'"
          >
            <div
              v-if="confirmDelete !== user.user_name && confirmResetAdmin !== user.user_name"
              class="user_actions"
            >
              <a
                v-if="showChangeRole"
                @click.stop="toggleAdmin(user)"
              >
                {{ $t('changerole') }} {{ (user.is_admin)?$t('user'):$t("admin") }}
                <v-icon	name="user" />
              </a>
              <br>
              <a
                v-if="album.is_admin && showDeleteUser"
                class="text-danger"
                @click.stop="deleteUser(user)"
              >
                {{ $t('remove') }}
                <v-icon name="trash" />
              </a>
            </div>
            <div v-if="confirmDelete === user.user_name">
              <span class="text-danger mr-2">
                {{ $t("warningtoggledelete") }}
              </span>
              <br>
              <div class="btn-group">
                <button
                  type="button"
                  class="btn btn-sm btn-danger"
                  @click.stop="deleteUser(user)"
                >
                  {{ $t('confirm') }}
                </button><button
                  type="button"
                  class="btn btn-sm btn-secondary"
                  @click.stop="confirmDelete=''"
                >
                  {{ $t('cancel') }}
                </button>
              </div>
            </div>
            <div v-if="confirmResetAdmin === user.user_name">
              <span class="text-danger mr-2">
                {{ $t("warningtoggleadmin") }}
              </span>
              <br>
              <div class="btn-group">
                <button
                  type="button"
                  class="btn btn-sm btn-danger"
                  @click.stop="toggleAdmin(user)"
                >
                  {{ $t('confirm') }}
                </button><button
                  type="button"
                  class="btn btn-sm btn-secondary"
                  @click.stop="confirmResetAdmin=''"
                >
                  {{ $t('cancel') }}
                </button>
              </div>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
import mobiledetect from '@/mixins/mobiledetect.js'
import { CurrentUser } from '@/mixins/currentuser.js'

export default {
	name: 'AlbumUsers',
	mixins: [ CurrentUser ],
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
		showDeleteUser: {
			type: Boolean,
			required: true,
			default: true
		},
		showChangeRole: {
			type: Boolean,
			required: true,
			default: true
		}
	},
	data () {
		return {
			confirmDelete: '',
			confirmResetAdmin: ''
		}
	},
	computed: {
		mobiledetect () {
			return mobiledetect.mobileAndTabletcheck()
		}
	},
	created () {
	},
	methods: {
		toggleAdmin (user) {
			if (this.currentuserSub === user.user_id && !this.confirmResetAdmin) {
				this.confirmResetAdmin = user.user_name
				return
      }

      let params = {
        album_id: this.album.album_id,
        user_name: user.user_name,
        user_is_admin: !user.is_admin
      }
      this.$store.dispatch('manageAlbumUserAdmin', params).then(res => {
        if (res.status === 204) {
          let message = (user.is_admin) ? this.$t('usersettoadmin') : this.$t('usernotsettoadmin')
          this.$snotify.success(message)
        } else {
				  this.$snotify.error(this.$t('sorryerror'))
        }
			}).catch(() => {
				this.$snotify.error(this.$t('sorryerror'))
      })
		},
		deleteUser (user) {
			if (this.confirmDelete !== user.user_name) this.confirmDelete = user.user_name
			else {
        let params = {
          album_id: this.album.album_id,
          user: user.user_name
        }
				this.$store.dispatch('removeAlbumUser', params).then(res => {
          if (res.status === 204) {
            this.$snotify.success(this.$t('albumuserdeletesuccess'))
            this.confirmDelete = ''
          } else {
					  this.$snotify.error(this.$t('sorryerror'))
          }
				}).catch(() => {
					this.$snotify.error(this.$t('sorryerror'))
				})
			}
		}
	}
}
</script>

<style scoped>
div.user-table-container{
	min-height: 200px;
	padding: 25px 0;
}
a {
	cursor: pointer;
}
/*
td.showOnTrHover div.user_actions{
	visibility: hidden;
}

tr:hover  td.showOnTrHover div.user_actions {
	visibility: visible;
}
*/
</style>
