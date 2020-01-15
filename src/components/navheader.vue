<i18n>
{
  "en": {
    "welcome": "Welcome",
    "lang": "lang",
    "tooltipHelp": "Help",
    "tooltipLogout": "Logout",
    "tooltipLogin": "Login"
  },
  "fr": {
    "welcome": "Bienvenue",
    "lang": "lang",
    "tooltipHelp": "Aide",
    "tooltipLogout": "Déconnexion",
    "tooltipLogin": "Se connecter"
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
      <div class="d-flex">
        <div style="padding-right: 5px">
          <kheops-pyramid
            width="12.2696mm"
            height="7.41195mm"
          />
        </div>
        <div class="align-self-end">
          <kheops-font
            width="16.5968mm"
            height="3.70944mm"
          />
        </div>
      </div>
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
            class="font-kheops active"
          >
            <router-link
              to="/user"
            >
              {{ currentuserFullname !== undefined ? currentuserFullname : currentuserEmail }}
            </router-link>
          </b-nav-item>
          <b-nav-item
            v-else-if="logged === false"
            class="active pointer"
            :title="$t('tooltipLogin')"
            @click="login()"
          >
            <span
              class="font-white"
            >
              {{ $t('tooltipLogin') }}
            </span>
          </b-nav-item>
          <b-nav-item
            class="active pointer"
            :title="$t('tooltipHelp')"
            target="_blank"
            @click="redirect('https://docs.kheops.online')"
          >
            <span
              class="font-white"
            >
              {{ $t('tooltipHelp') }}
              <v-icon name="help" />
            </span>
          </b-nav-item>
          <b-nav-item
            v-if="logged"
            :title="$t('tooltipLogout')"
            class="active pointer"
            @click="logout()"
          >
            <span
              class="font-white"
            >
              {{ $t('tooltipLogout') }}
              <v-icon name="sign-out-alt" />
            </span>
          </b-nav-item>
          <b-nav-item-dropdown
            :text="`${$t('lang')}: ${lang}`"
            toggle-class="font-white"
            right
            class="active"
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
import KheopsPyramid from '@/components/kheopsSVG/KheopsPyramid.vue';
import KheopsFont from '@/components/kheopsSVG/KheopsFont.vue';

export default {
  name: 'NavHeader',
  components: { KheopsPyramid, KheopsFont },
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
      const storageLanguage = localStorage.getItem('language');
      const navigatorLanguage = (navigator.language || navigator.userLanguage).split('-')[0];
      if (storageLanguage !== null) {
        this.changeLang(storageLanguage);
      } else {
        this.changeLang(navigatorLanguage);
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
