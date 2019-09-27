<i18n>
{
  "en": {
    "welcome": "Welcome",
    "lang": "lang"
  },
  "fr": {
    "welcome": "Bienvenue",
    "lang": "lang"
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
    <b-navbar-brand href="#">
      <!--
      <img
        src="../assets/sib_logo_small.gif"
        style="margin-right:5px"
      >
      -->
      <router-link
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
          <b-nav-item v-access="'active'">
            {{ $t('welcome') }} <router-link to="/user">
              {{ currentuserFullname }}
            </router-link>
          </b-nav-item>
          <b-nav-item v-access="'active'">
            <a
              class="pointer"
              @click="redirect('https://docs.kheops.online')"
            >
              <v-icon name="help" />
            </a>
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
                style="text-transform: uppercase;"
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

export default {
  name: 'NavHeader',
  mixins: [CurrentUser],
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
    changeLang(value) {
      if (this.availableLanguage.includes(value)) {
        localStorage.setItem('language', value);
        this.$root.$i18n.locale = value;
      }
    },
    redirect(href) {
      window.location.href = href;
    },
  },
};

</script>
