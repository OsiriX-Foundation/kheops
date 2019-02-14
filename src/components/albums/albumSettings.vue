/* eslint-disable */

<i18n>
{
	"en": {
		"general": "General",
		"user": "User",
		"token": "Token"
	},
	"fr": {
		"general": "Général",
		"user": "Utilisateur",
		"token": "Token"

	}
}
</i18n>

<template>
	<div class = 'container' id="albumSettings">
		<div class = 'row'>
			<div class = 'col-2' >
				<nav class="nav nav-pills nav-justified flex-column">
					<a class="nav-link" v-for="(cat,idx) in categories" :key="idx" :class="(view==cat)?'active':''" @click="view=cat">{{$t(cat)}}</a>
				</nav>
			</div>
			<div class = 'col-10' >
				<album-settings-general v-if="view=='general'"></album-settings-general>
				<album-settings-user v-if="view=='user'"></album-settings-user>
				<album-settings-token v-if="view=='token'"></album-settings-token>
			</div>

		</div>
	</div>
</template>

<script>
import { mapGetters } from 'vuex'
import albumSettingsGeneral from '@/components/albums/albumSettingsGeneral'
import albumSettingsUser from '@/components/albums/albumSettingsUser'
import albumSettingsToken from '@/components/albums/albumSettingsToken'

export default {
	name: 'album_settings',
	components: { albumSettingsGeneral, albumSettingsUser, albumSettingsToken },
	data () {
		return {
			view: 'general',
			categories: ['general', 'user', 'token']
		}
	},
	computed: {
		...mapGetters({
			album: 'album'
		})
	},
	created () {
		if (this.categories.indexOf(this.$route.query.cat) > -1) {
			this.view = this.$route.query.cat
		}
	},
	watch: {
		view () {
			this.$router.push({ query: { view: 'settings', cat: this.view } })
		}
	}
}
</script>

<style scoped>

</style>
