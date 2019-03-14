<i18n>
{
	"en": {
		"usersettings": "User settings",
		"general": "General",
		"token": "Token",
		"provider": "Provider"
	},
	"fr": {
		"usersettings": "Préférences utilisateur",
		"general": "Général",
		"token": "Token",
		"provider": "Fournisseur"
	}
}
</i18n>

<template>
  <div
    id="user"
    class="container"
  >
    <h3 class="pb-3">
      {{ $t('usersettings') }}
    </h3>
    <div class="row">
      <div class="col-lg-2 col-sm-12 col-xs-12 mb-2">
        <nav class="nav nav-pills nav-justified flex-column text-center text-lg-left">
          <a
            v-for="(cat,idx) in categories"
            :key="idx"
            class="nav-link"
            :class="(view==cat)?'active':''"
            @click="view=cat"
          >
            {{ $t(cat) }}
          </a>
        </nav>
      </div>
      <div class="col-lg-10 col-sm-12 col-xs-12">
        <user-settings-general v-if="view=='general'" />
        <user-settings-token v-if="view=='token'" />
        <user-settings-provider v-if="view=='provider'" />
      </div>
    </div>
  </div>
</template>

<script>

import { mapGetters } from 'vuex'
import userSettingsGeneral from '@/components/user/userSettingsGeneral'
import userSettingsToken from '@/components/user/userSettingsToken'
import userSettingsProvider from '@/components/user/userSettingsProvider'
export default {
	name: 'User',
	components: { userSettingsGeneral, userSettingsToken, userSettingsProvider },
	data () {
		return {
			// categories: ['general', 'token', 'provider'],
			categories: ['general', 'token'],
			view: 'general'
		}
	},
	computed: {
		...mapGetters({
			user: 'currentUser'
		})
	},
	watch: {
		view () {
			let queryParams = { view: this.view }
			this.$router.push({ query: queryParams })
		},
		'$route.query' () {
			this.view = this.$route.query.view
		}
	},
	mounted () {
		this.view = this.$route.query.view || 'general'
	}
}
</script>

<style scoped>
a.nav-link{
	cursor: pointer;
}
</style>
