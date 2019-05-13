/* eslint-disable */

<i18n>
{
	"en": {
		"general": "General",
		"user": "User",
		"token": "Token",
    "providerSR": "Providers"
	},
	"fr": {
		"general": "Général",
		"user": "Utilisateur",
    "token": "Token",
    "providerSR": "Providers"
	}
}
</i18n>

<template>
  <div
    id="albumSettings"
    class="container"
  >
    <div class="row">
      <div class="d-none d-md-block col-md-2">
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
      <div class="d-block d-sm-block d-md-none col-12 ">
        <b-dropdown
          id="dropdown-right"
          :text="$t(view)"
          variant="primary"
          class="m-2 p-2 d-flex justify-content-center"
          toggle-class="col-12"
          menu-class="col-10"
        >
          <b-dropdown-item
            v-for="(cat,idx) in categories"
            :key="idx"
            href="#"
            :active="view===cat"
          >
            <a
              class="nav-link"
              :class="(view==cat)?'active':''"
              @click="view=cat"
            >
              {{ $t(cat) }}
            </a>
          </b-dropdown-item>
        </b-dropdown>
      </div>
      <div class="col-sm-12 col-md-10">
        <album-settings-general v-if="view=='general'" />
        <album-settings-user v-if="view=='user'" />
        <album-settings-token v-if="view=='token'" />
        <album-settings-report-provider v-if="view=='providerSR'" />
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
import albumSettingsReportProvider from '@/components/albums/albumSettingsReportProvider'

export default {
	name: 'AlbumSettings',
	components: { albumSettingsGeneral, albumSettingsUser, albumSettingsToken, albumSettingsReportProvider },
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
			return (this.album.is_admin) ? this.basicCategories.concat('token', 'providerSR') : this.basicCategories
		}
	},
	watch: {
		view () {
			this.$router.push({ query: { view: 'settings', cat: this.view } })
		},
		'$route.query' () {
			this.view = this.$route.query.cat !== undefined ? this.$route.query.cat : 'general'
		}
	},
	created () {
		if (this.categories.indexOf(this.$route.query.cat) > -1) {
			this.view = this.$route.query.cat
		}
	},
	beforeDestroy () {
		let query = Object.assign({}, this.$route.query)
		delete query.cat
		this.$router.replace({ query })
	}
}
</script>

<style scoped>
a.nav-link{
	cursor: pointer;
}
</style>
