import Vue from 'vue';

export const CurrentUser = {
  computed: {
    currentuserAccessToken() {
      if (window.location.pathname.includes('view')) {
        const [, , token] = window.location.pathname.split('/');
        return token
      } else if (Vue.prototype.$keycloak.authenticated) {
        return Vue.prototype.$keycloak.token;
      } 
      return ''
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
};
