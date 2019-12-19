/* eslint-disable */
import Vue from 'vue';

export const CurrentUser = {
  computed: {
    authenticated() {
      return Vue.prototype.$keycloak.authenticated;
    },
    currentuserOnView() {
      return window.location.pathname.includes('view');
    },
    currentuserCapabilitiesToken() {
      if (window.location.pathname.includes('view')) {
        const [, , token] = window.location.pathname.split('/');
        return token;
      }
      return undefined;
    },
    currentuserKeycloakToken() {
      return Vue.prototype.$keycloak.token;
    },
    currentuserSub() {
      return Vue.prototype.$keycloak.idTokenParsed.sub;
    },
    currentuserEmail() {
      return Vue.prototype.$keycloak.idTokenParsed.email;
    },
    currentuserFullname() {
      return Vue.prototype.$keycloak.idTokenParsed.name;
    },
  },
  methods: {
    currentuserAccessToken() {
      if (window.location.pathname.includes('view')) {
        const [, , token] = window.location.pathname.split('/');
        return token;
      } if (Vue.prototype.$keycloak.authenticated) {
        return Vue.prototype.$keycloak.token;
      }
      return '';
    },
  },
};
