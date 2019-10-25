<i18n>
{
  "en": {
    "welcome": "Welcome",
    "lang": "lang",
    "tooltipHelp": "Help",
    "tooltipLogout": "Logout"
  },
  "fr": {
    "welcome": "Bienvenue",
    "lang": "lang",
    "tooltipHelp": "Aide",
    "tooltipLogout": "Déconnexion"
  }
}
</i18n>

<template>
  <!--
    Navbar
    https://bootstrap-vue.js.org/docs/components/navbar/
  -->
  <b-navbar
    toggleable="md"
    type="light"
    variant="light"
    fixed="top"
  >
    <b-navbar-toggle target="nav_collapse" />
    <b-navbar-nav>
      <kheops
        :width="24"
        :height="24"
      />
      <span
        class="kheops-title"
      >
        KHEOPS
      </span>
    </b-navbar-nav>

    <b-collapse
      id="nav_collapse"
      is-nav
    >
      <!-- Right aligned nav items -->
      <b-navbar-nav class="ml-auto">
        <b-navbar-nav right>
          <b-nav-item
            v-if="logged"
            v-access="'active'"
            class="kheopsfont"
          >
            {{ $t('welcome') }}
            <router-link
              to="/user"
              class="kheopsfont"
            >
              {{ currentuserFullname }}
            </router-link>
          </b-nav-item>
          <b-nav-item
            v-else-if="logged === false"
            v-access="'active'"
          >
            <a
              :title="$t('tooltipLogout')"
              class="pointer"
              @click="login()"
            >
              Login
            </a>
          </b-nav-item>
          <b-nav-item v-access="'active'">
            <a
              :title="$t('tooltipHelp')"
              class="pointer"
              target="_blank"
              @click="redirect('https://docs.kheops.online')"
            >
              {{ $t('tooltipHelp') }}
              <v-icon name="help" />
            </a>
          </b-nav-item>
          <b-nav-item
            v-if="logged"
            v-access="'active'"
          >
            <a
              :title="$t('tooltipLogout')"
              class="pointer"
              @click="logout()"
            >
              <v-icon name="sign-out-alt" />
            </a>
          </b-nav-item>
          <b-nav-item-dropdown
            :text="`${$t('lang')}: ${lang}`"
            right
          >
            <b-dropdown-item
              v-for="language in availableLanguage"
              :key="language.id"
              :active="lang === language"
              @click="changeLang(language)"
            >
              <span
                class="text-uppercase"
              >
                {{ language }}
              </span>
            </b-dropdown-item>
          </b-nav-item-dropdown>
        </b-navbar-nav>
      </b-navbar-nav>
    </b-collapse>
  </b-navbar>
</template>

<script>
import Vue from 'vue';
import store from '@/store';
import { CurrentUser } from '@/mixins/currentuser.js';
import Kheops from '@/components/kheopsSVG/Kheops.vue';

export default {
  name: 'NavHeader',
  components: { Kheops },
  mixins: [CurrentUser],
  props: {
    logged: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {
      availableLanguage: [
        'en',
        'fr',
      ],
    };
  },
  computed: {
    lang() {
      return this.$i18n.locale;
    },
  },
  created() {
    this.setFromLocalStorage();
  },
  methods: {
    setFromLocalStorage() {
      const language = localStorage.getItem('language');
      if (language !== null) {
        this.changeLang(language);
      }
    },
    logout() {
      store.dispatch('logout').then(() => {
        Vue.prototype.$keycloak.logoutFn();
      });
    },
    login() {
      Vue.prototype.$keycloak.loginFn();
    },
    changeLang(value) {
      if (this.availableLanguage.includes(value)) {
        localStorage.setItem('language', value);
        this.$root.$i18n.locale = value;
      }
    },
    redirect(href) {
      window.open(href, '_blank');
    },
  },
};

</script>
