<!--
		scope: Define the scope (Study or Album)
			- type: String
			- required: true
			- default: ''
		albumId: Define the album ID
			- type: String
			- required: false
			- default: ''
		enableAdd: Enable input if this prop is true.
			- type: Boolean
			- required: true
			- default: true

		this component send emit "private-user" to the parent when :
			- when user is delete
			- when user is add
			- when Enable Add is set to false in parent component
-->
<template>
  <div>
    <h5
      v-if="user"
      class="user"
    >
      <span
        class="badge badge-secondary"
      >
        {{ user }}
        <span
          class="icon pointer"
          @click="deleteUser()"
        >
          <v-icon name="times" />
        </span>
      </span>
    </h5>
    <h5
      v-else
      class="user"
    >
      <div class="input-group mb-3">
        <input
          ref="textcomment"
          v-model="newUserName"
          type="text"
          class="form-control form-control-sm"
          placeholder="email"
          aria-label="Email"
          :disabled="!enableAdd"
          @keydown.enter.prevent="checkUser"
        >
        <div class="input-group-append">
          <button
            id="button-addon2"
            class="btn btn-outline-secondary btn-sm"
            type="button"
            title="add user"
            :disabled="!enableAdd"
            @click="checkUser()"
          >
            <v-icon name="plus" />
          </button>
        </div>
      </div>
    </h5>
  </div>
</template>

<script>
import { HTTP } from '@/router/http'

export default {
	name: 'AddUser',
	props: {
		scope: {
			type: String,
			required: true,
			default: ''
		},
		albumId: {
			type: String,
			required: false,
			default: ''
		},
		enableAdd: {
			type: Boolean,
			required: true,
			default: true
		}
	},
	data () {
		return {
			user: '',
			newUserName: ''
		}
	},
	watch: {
		enableAdd: {
			handler: function (enableAdd) {
				if (!this.enableAdd) {
					this.deleteUser()
				} else {
					let textcomment = this.$refs.textcomment
					setTimeout(function () { textcomment.focus() }, 0)
				}
			}
		}
	},
	methods: {
		deleteUser () {
			this.user = ''
			this.$emit('private-user', this.user)
		},
		checkUser () {
			if (this.scope === 'album') {
				this.checkUserAlbum()
			} else {
				this.checkUserStudies()
			}
		},
		checkUserStudies () {
			HTTP.get('users?reference=' + this.newUserName, { headers: { 'Accept': 'application/json' } }).then(res => {
				if (res.status === 204) this.$snotify.error('User unknown')
				else if (res.status === 200) {
					this.setUser(res.data.email)
				}
			}).catch(() => {
				console.log('Sorry, an error occured')
			})
		},
		checkUserAlbum () {
			HTTP.get('/albums/' + this.albumId + '/users', '', { headers: { 'Accept': 'application/json' } }).then(res => {
				if (res.data.filter(user => { return user.user_name === this.newUserName }).length > 0) {
					this.setUser(this.newUserName)
				} else {
					this.$snotify.error('User unknown in this album')
				}
			}).catch(() => {
				console.log('Sorry, an error occured')
			})
		},
		setUser (user) {
			this.user = user
			this.newUserName = ''
			this.$emit('private-user', this.user)
		}
	}
}
</script>
