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
	<div id = 'user' class = 'container'>
		<h3 class = 'pb-3'>{{$t('usersettings')}}</h3>
		<div class = 'row'>
			<div class = 'col-sm-2 col-xs-12' >
				<nav class="nav nav-pills nav-justified flex-column">
					<a class="nav-link" v-for="(cat,idx) in categories" :key="idx" :class="(view==cat)?'active':''" @click="view=cat">{{$t(cat)}}</a>
				</nav>
			</div>
			<div class = 'col-sm-10 col-xs-12' >
				<user-settings-general v-if="view=='general'"></user-settings-general>
				<user-settings-token v-if="view=='token'"></user-settings-token>
				<user-settings-provider v-if="view=='provider'"></user-settings-provider>
			</div>
		</div>
	</div>
</template>

<script>

import { mapGetters } from 'vuex'
import userSettingsGeneral from '@/components/user/userSettingsGeneral'
import userSettingsToken from '@/components/user/userSettingsToken'
import userSettingsProvider from '@/components/user/userSettingsProvider'
export default{
	name: 'user',
	components: { userSettingsGeneral, userSettingsToken, userSettingsProvider},
	data () {
		return {
			categories: ['general','token','provider'],
			view: 'general'			
		}
	},
	computed: {
		...mapGetters({
			user: 'currentUser'
		})
	}
}
</script>

<style scoped>
a.nav-link{
	cursor: pointer;
}
</style>