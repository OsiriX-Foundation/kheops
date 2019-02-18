/* eslint-disable */

<i18n>
{
	"en": {
		"albumName": "Album Name",
		"albumDescription": "Album Description",
		"users": "Users",
		"addUser": "Add User",
		"addSeries": "Add Studies / Series",
		"downloadSeries": "Download Studies / Series",
		"sendSeries": "Get Studies / Series",
		"deleteSeries": "Remove Studies / Series",
		"writeComments": "Write Comments",
		"create": "Create",
		"cancel": "Cancel"
	},
	"fr": {
		"albumName": "Nom de l'album",
		"albumDescription": "Description de l'album",
		"users": "Utilisateurs",
		"addUser": "Ajouter un utilisateur",
		"addSeries": "Ajouter une étude / série",
		"downloadSeries": "Télécharger une étude / série",
		"sendSeries": "Récupérer une étude / série",
		"deleteSeries": "Supprimer une étude / série",
		"writeComments": "Commenter",
		"create": "Créer",
		"cancel": "Annuler"
	}
}
</i18n>

<template>
  <div class="container">
    <h3>{{ displayName }}</h3>
    <form @submit.prevent="createAlbum">
      <fieldset>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('albumName') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <input
                v-model="album.name"
                type="text"
                :placeholder="$t('albumName')"
                class="form-control"
              >
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('albumDescription') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <textarea
                v-model="album.description"
                rows="5"
                class="form-control"
                :placeholder="$t('albumDescription')"
              />
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('users') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <h5 class="user">
                <span
                  v-for="user in album.users"
                  :key="user.user_name"
                  class="badge badge-secondary"
                >
                  {{ user.firstname+" "+user.lastname }} <span
                    class="icon pointer"
                    @click="deleteUser(user)"
                  >
                    <v-icon name="times" />
                  </span>
                </span>
              </h5>
              <h5 class="user">
                <div class="input-group mb-3">
                  <input
                    v-model="newUserName"
                    type="text"
                    class="form-control form-control-sm"
                    placeholder="email"
                    aria-label="Email"
                  >
                  <div class="input-group-append">
                    <button
                      id="button-addon2"
                      class="btn btn-outline-secondary btn-sm"
                      type="button"
                      title="add user"
                      @click="checkUser()"
                    >
                      <v-icon name="plus" />
                    </button>
                  </div>
                </div>
              </h5>
            </dd>
          </div>
        </div>
      </fieldset>

      <fieldset class="user_settings">
        <legend>{{ $t('usersettings') }}</legend>
        <div
          v-for="(value,label) in album.userSettings"
          :key="label"
          class="row form-group"
          :class="(label==='sendSeries')?'offset-1':''"
        >
          <div>
            <toggle-button
              v-model="album.userSettings[label]"
              :labels="{checked: 'Yes', unchecked: 'No'}"
              :disabled="(!album.userSettings.downloadSeries && label=='sendSeries')"
              :sync="true"
            />
          </div>
          <label>{{ $t(label) }}</label>
        </div>
      </fieldset>
      <fieldset>
        <div class="row">
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="!album.name"
          >
            {{ $t('create') }}
          </button>
          <router-link
            to="/albums"
            class="btn btn-secondary"
          >
            {{ $t('cancel') }}
          </router-link>
        </div>
      </fieldset>
    </form>
  </div>
</template>

<script>
import { HTTP } from '@/router/http'
export default {
	name: 'NewAlbum',

	data () {
		return {
			album: {
				album_id: '',
				name: '',
				description: '',
				addUser: false,
				downloadSeries: true,
				sendSeries: true,
				deleteSeries: false,
				addSeries: true,
				writeComments: true,
				users: [{ email: 'robin.liechti@sib.swiss', 'firstname': 'Robin', 'lastname': 'Liechti' }],
				userSettings: {
					addUser: false,
					addSeries: true,
					downloadSeries: true,
					sendSeries: true,
					deleteSeries: false,
					writeComments: true
				}
			},
			newUserName: ''
		}
	},
	computed: {
		displayName () {
			return (!this.album.album_id) ? 'New album' : this.album.name
		}
	},
	watch: {
		'album.userSettings.downloadSeries' () {
			console.log(this.album.userSettings.downloadSeries)
			if (!this.album.userSettings.downloadSeries) {
				console.log('ici')
				this.album.userSettings.sendSeries = false
				console.log(this.album)
			}
		}
	},
	methods: {
		deleteUser (user) {
			console.log('delete: ', user)
		},
		checkUser () {
			let vm = this
			let idx = _.findIndex(vm.album.users, u => { return u.email === vm.newUserName })
			if (vm.newUserName && idx === -1) {
				HTTP.get('users?reference=' + vm.newUserName, { headers: { 'Accept': 'application/json' } }).then(res => {
					console.log(res.data)
				}).catch(() => {
					console.log('Sorry, an error occured')
				})
			}
		},
		createAlbum () {
			let postValues = {
				name: this.album.name,
				description: this.album.description,
				addUser: this.album.userSettings.addUser,
				downloadSeries: this.album.userSettings.downloadSeries,
				sendSeries: this.album.userSettings.sendSeries,
				deleteSeries: this.album.userSettings.deleteSeries,
				addSeries: this.album.userSettings.addSeries,
				writeComments: this.album.userSettings.writeComments
			}
			this.$store.dispatch('createAlbum', postValues).then(() => {
				this.$router.push('/albums')
			})
		}
	}
}

</script>

<style scoped>
h3 {
	margin-bottom: 40px;
}

h5.user{
	float: left;
	margin-right: 10px;
}

.icon{
	margin-left: 10px;
}
.pointer{
	cursor: pointer;
}
label{
	margin-left: 10px;
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
dt{
	text-align: right;
}
</style>
