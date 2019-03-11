<i18n>
{
	"en": {
		"language": "language",
    "accountmanagement": "Account management",
    "manage": "Manage"
	},
	"fr": {
		"language": "langue",
    "accountmanagement": "Gérer son profil utilisateur",
    "manage": "Gérer"
	}
}
</i18n>

<template>
  <div id="userSettingsGeneral">
    <form>
      <dl class="row">
        <dt class="col-md-3 col-xs-12 text-right">
          {{ $t('language') }}
        </dt>
        <dd class="col-md-9 col-xs-12 text-left">
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
        </dd>

        <dt class="col-md-3 col-xs-12 text-right">
          {{ $t('accountmanagement') }}
        </dt>
        <dd class="col-md-9 col-xs-12 text-left">
          <button
            type="button"
            class="btn btn-primary"
            @click="gomanagement()"
          >
            {{ $t('manage') }}
          </button>
        </dd>
      </dl>
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
