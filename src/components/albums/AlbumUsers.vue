
<i18n>
{
	"en": {
		"username": "User name",
		"user": "user",
		"changerole": "change role to"
	},
	"fr": {
		"username": "Utilisateur",
		"user": "Utilisateur",
		"changerole": "changer le rôle pour"
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
            {{ user.user_name }} <span v-if="user.is_admin">
              (Admin)
            </span>
          </td>
          <td
            v-if="album.is_admin"
            class="showOnTrHover text-right"
          >
            <div
              v-if="confirm_delete!=user.user_name"
              class="user_actions"
            >
              <a @click.stop="toggleAdmin(user)">
                {{ $t('changerole') }} {{ (user.is_admin)?$t('user'):"admin" }}
              </a> <a
                v-if="album.is_admin"
                class="text-danger"
                style="margin-left: 20px"
                @click.stop="deleteUser(user)"
              >
                <v-icon name="trash" />
              </a>
            </div>
            <div v-if="confirm_delete==user.user_name">
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
                  @click.stop="confirm_delete=''"
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
export default {
	name: 'AlbumUsers',
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
		}
	},
	data () {
		return {
			confirm_delete: ''
		}
	},
	watch: {
		users: {
			handler: function () {
				this.$store.dispatch('getAlbum', { album_id: this.$route.params.album_id })
			},
			deep: true
		}
	},
	created () {
	},
	methods: {
		toggleAdmin (user) {
			user.is_admin = !user.is_admin
			this.$store.dispatch('toggleAlbumUserAdmin', user).then(() => {
				let message = (user.is_admin) ? this.$t('usersettoadmin') : this.$t('usernotsettoadmin')
				this.$snotify.success(message)
			}).catch(() => {
				this.$snotify.error(this.$t('sorryerror'))
			})
		},
		deleteUser (user) {
			if (this.confirm_delete !== user.user_name) this.confirm_delete = user.user_name
			else {
				this.$store.dispatch('remove_user_from_album', { user_name: user.user_name }).then(() => {
					this.$snotify.success(this.$t('albumuserdeletesuccess'))
					this.confirm_delete = ''
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
td.showOnTrHover div.user_actions{
	visibility: hidden;
}

tr:hover  td.showOnTrHover div.user_actions{
	visibility: visible;
}
</style>
