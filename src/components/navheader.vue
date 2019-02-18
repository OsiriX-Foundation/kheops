<i18n>
{
	"en": {
		"welcome": "Welcome"
	},
	"fr": {
		"welcome": "Bienvenue"
	}
}
</i18n>

<template>
  <!-- Navbar -->
  <b-navbar
    toggleable="md"
    type="light"
    variant="light"
    fixed="top"
  >
    <b-navbar-toggle target="nav_collapse" />

    <b-navbar-brand href="#">
      <img
        src="../assets/sib_logo_small.gif"
        style="margin-right:5px"
      ><router-link
        to="/"
        style="font-size: 1.3rem;"
      >
        Kheops
      </router-link>
    </b-navbar-brand>

    <b-collapse
      id="nav_collapse"
      is-nav
    >
      <!-- Right aligned nav items -->
      <b-navbar-nav class="ml-auto">
        <b-navbar-nav right>
          <b-nav-item v-access="'admin'">
            <router-link to="/admin">
              Admin
            </router-link>
          </b-nav-item>
          <b-nav-item v-access="'active'">
            {{ $t('welcome') }} <router-link to="/user">
              {{ user.fullname }}
            </router-link>
          </b-nav-item>
          <b-nav-item v-access="'active'">
            <a
              class="pointer"
              @click="logout()"
            >
              <v-icon name="sign-out-alt" />
            </a>
          </b-nav-item>
          <b-nav-item-dropdown
            :text="'Lang: '+lang"
            right
          >
            <b-dropdown-item @click="changeLang('en')">
              EN
            </b-dropdown-item>
            <b-dropdown-item @click="changeLang('fr')">
              FR
            </b-dropdown-item>
          </b-nav-item-dropdown>
        </b-navbar-nav>
      </b-navbar-nav>
    </b-collapse>
  </b-navbar>
</template>

<script>
import { mapGetters } from 'vuex'
import store from '@/store'
import Vue from 'vue'

export default {
	name: 'NavHeader',
	data () {
		return {
		}
	},
	computed: {
		...mapGetters({
			user: 'currentUser'
		}),
		lang () {
			return this.$i18n.locale
		}
	},
	methods: {
		logout () {
			store.dispatch('logout').then(() => {
				Vue.prototype.$keycloak.logoutFn()
			})
		},
		changeLang (value) {
			this.$root.$i18n.locale = value
		}
	}
}

</script>
