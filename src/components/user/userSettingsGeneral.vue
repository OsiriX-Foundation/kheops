<i18n>
{
	"en": {
		"language": "Language",
    "accountmanagement": "Account management",
    "manage": "Manage"
	},
	"fr": {
		"language": "Langue",
    "accountmanagement": "Gérer son profil utilisateur",
    "manage": "Gérer"
	}
}
</i18n>

<template>
  <div id="userSettingsGeneral">
    <form>
      <div class="row">
        <div class="col-xs-3 col-sm-3 col-md-3 text-left text-sm-right">
          {{ $t('language') }}
        </div>
        <div class="col-xs-9 col-sm-9 col-md-9 text-left mb-2">
          <select
            v-model="lang"
            class="form-control"
          >
            <option value="en">
              English
            </option>
            <option value="fr">
              Fançais
            </option>
          </select>
        </div>
        <div class="col-xs-3 col-sm-3 col-md-3 col-xs-12 text-left text-sm-right">
          {{ $t('accountmanagement') }}
        </div>
        <div class="col-xs-9 col-sm-9 col-md-9 col-xs-12 text-left mb-2">
          <button
            type="button"
            class="btn btn-primary"
            @click="gomanagement()"
          >
            {{ $t('manage') }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
export default {
	name: 'UserSettingsGeneral',
	data () {
		return {
		}
	},
	computed: {
		lang: {
			get () {
				return this.$i18n.locale
			},
			set (value) {
				this.$root.$i18n.locale = value
			}
		}
	},
	methods: {
		gomanagement () {
			window.open(`${process.env.VUE_APP_URL_KEYCLOAK}/auth/realms/${process.env.VUE_APP_REALM_KEYCLOAK}/account`)
		}
	}
}
</script>

<style scoped>
dt {
	text-transform: capitalize;
}
</style>
