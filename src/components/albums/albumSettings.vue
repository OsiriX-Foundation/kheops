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
  <div
    id="albumSettings"
    class="container"
  >
    <div class="row">
      <div class="col-2">
        <nav class="nav nav-pills nav-justified flex-column">
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
      <div class="col-10">
        <album-settings-general v-if="view=='general'" />
        <album-settings-user v-if="view=='user'" />
        <album-settings-token v-if="view=='token'" />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { AlbumRedirect } from '../../mixins/redirect.js'
import albumSettingsGeneral from '@/components/albums/albumSettingsGeneral'
import albumSettingsUser from '@/components/albums/albumSettingsUser'
import albumSettingsToken from '@/components/albums/albumSettingsToken'

export default {
	name: 'AlbumSettings',
	components: { albumSettingsGeneral, albumSettingsUser, albumSettingsToken },
	mixins: [ AlbumRedirect ],
	data () {
		return {
			view: 'general',
			basicCategories: ['general', 'user']
		}
	},
	computed: {
		...mapGetters({
			album: 'album'
		}),
		categories () {
			return (this.album.is_admin) ? this.basicCategories.concat('token') : this.basicCategories
		}
	},
	watch: {
		view () {
			this.$router.push({ query: { view: 'settings', cat: this.view } })
		},
		'$route.query' () {
			this.view = this.$route.query.cat
		}
	},
	created () {
		if (this.categories.indexOf(this.$route.query.cat) > -1) {
			this.view = this.$route.query.cat
		}
	}
}
</script>

<style scoped>
a.nav-link{
	cursor: pointer;
}
</style>
