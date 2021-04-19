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
      <router-link
        to="/inbox"
      >
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
      </router-link>
    </b-navbar-nav>
    <b-collapse
      id="nav_collapse"
      is-nav
    >
      <!-- Right aligned nav items -->
      <b-navbar-nav class="ml-auto">
        <b-navbar-nav right>
          <b-nav-item
            v-if="logged === true"
            class="font-kheops active"
          >
            <router-link
              to="/user"
            >
              {{ oidcUser.name }}
            </router-link>
          </b-nav-item>
          <b-nav-item
            v-else-if="logged === false"
            class="active pointer"
            :title="$t('navbar.tooltipLogin')"
            @click="login()"
          >
            <span
              class="font-white"
            >
              {{ $t('navbar.tooltipLogin') }}
            </span>
          </b-nav-item>
          <b-nav-item
            class="active pointer"
            :title="$t('navbar.tooltipHelp')"
            target="_blank"
            @click="redirectOn('https://docs.kheops.online')"
          >
            <span
              class="font-white"
            >
              {{ $t('navbar.tooltipHelp') }}
              <v-icon name="help" />
            </span>
          </b-nav-item>
          <b-nav-item
            v-if="logged"
            :title="$t('navbar.tooltipLogout')"
            class="active pointer"
          >
            <router-link
              :to="{ path: '/oidc-logout', name: 'oidcLogout', params: {redirect: redirect} }"
              class="font-white"
            >
              {{ $t('navbar.tooltipLogout') }}
              <v-icon name="sign-out-alt" />
            </router-link>
          </b-nav-item>
          <b-nav-item-dropdown
            :text="`${$t('navbar.lang')}: ${lang}`"
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
import { mapActions, mapGetters } from 'vuex';
import { loadLanguageAsync } from '@/setup/i18n-setup';
import KheopsPyramid from '@/components/kheopsSVG/KheopsPyramid.vue';
import KheopsFont from '@/components/kheopsSVG/KheopsFont.vue';
import availableLanguage from '@/lang/availablelanguage';

export default {
  name: 'NavHeader',
  components: { KheopsPyramid, KheopsFont },
  props: {
    logged: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  computed: {
    ...mapGetters('oidcStore', [
      'oidcUser',
    ]),
    availableLanguage() {
      return availableLanguage;
    },
    lang() {
      return this.$i18n.locale;
    },
    redirect() {
      return `${process.env.VUE_APP_URL_ROOT}${this.$route.path}`;
    },
  },
  created() {
    this.setFromLocalStorage();
  },
  methods: {
    ...mapActions('oidcStore', ['authenticateOidcSilent', 'signOutOidc', 'authenticateOidc']),
    setFromLocalStorage() {
      const storageLanguage = localStorage.getItem('language');
      const navigatorLanguage = (navigator.language || navigator.userLanguage).split('-')[0];
      if (storageLanguage !== null) {
        this.changeLang(storageLanguage);
      } else {
        this.changeLang(navigatorLanguage);
      }
    },
    changeLang(value) {
      if (this.availableLanguage.includes(value)) {
        localStorage.setItem('language', value);
        loadLanguageAsync(value);
      }
    },
    redirectOn(href) {
      window.open(href, '_blank');
    },
    login() {
      const payload = {
        redirectPath: this.$route.path,
      };
      this.authenticateOidc(payload);
    },
  },
};

</script>
