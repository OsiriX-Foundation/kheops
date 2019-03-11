<i18n>
{
	"en": {
		"username": "User name",
		"user": "user",
    "send": "Send"
	},
	"fr": {
		"username": "Utilisateur",
    "user": "Utilisateur",
    "send": "Envoyer"
	}
}
</i18n>

<template>
  <div class="card">
    <div class="card-body">
      <form @submit.prevent="getUser">
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
              {{ $t('send') }}
            </button>
            <button
              class="btn btn-secondary"
              type="reset"
              tabindex="0"
              @keyup.esc="new_user_name=''"
              @click="cancel"
            >
              {{ $t('cancel') }}
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
	name: 'FormGetUser',
	data () {
		return {
			new_user_name: ''
		}
	},
	methods: {
		validEmail (email) {
			var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
			return re.test(email)
		},
		getUser () {
			this.$store.dispatch('checkUser', this.new_user_name).then(sub => {
				if (!sub) this.$snotify.error('Sorry, unknown user')
				else {
					this.$emit('get-user', sub)
					this.new_user_name = ''
				}
			})
		},
		cancel () {
			this.new_user_name = ''
			this.$emit('cancel-user')
		}
	}

}
</script>
