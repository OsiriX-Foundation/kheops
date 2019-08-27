/* eslint-disable */

<i18n>
{
	"en": {
		"general": "General",
		"user": "User",
		"token": "Token",
    "providerSR": "Report providers"
	},
	"fr": {
		"general": "Général",
		"user": "Utilisateur",
    "token": "Token",
    "providerSR": "Report providers"
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
        <album-settings-general
          :album="album"
          v-if="view=='general'"
        />
        <album-settings-user
          v-if="view=='user'"
          :album="album"
        />
        <album-settings-token
          v-if="view=='token'"
          :album="album"  
        />
        <album-settings-report-provider
          v-if="view=='providerSR'"
          :album="album"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import AlbumSettingsGeneral from '@/components/albumsdatamodel/AlbumSettingsGeneral'
import AlbumSettingsUser from '@/components/albumsdatamodel/AlbumSettingsUser'
import AlbumSettingsToken from '@/components/albumsdatamodel/AlbumSettingsToken'
import AlbumSettingsReportProvider from '@/components/albumsdatamodel/AlbumSettingsReportProvider'

export default {
	name: 'AlbumSettings',
	components: { AlbumSettingsGeneral, AlbumSettingsUser, AlbumSettingsToken, AlbumSettingsReportProvider },
	props: {
		album: {
			type: Object,
      required: true,
      default: () => {}
		}
	},
	data () {
		return {
			view: 'general',
			basicCategories: ['general', 'user', 'providerSR']
		}
	},
	computed: {
		categories () {
			return (this.album.is_admin) ? this.basicCategories.concat('token') : this.basicCategories
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
