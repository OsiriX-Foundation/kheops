import Vue from 'vue';

export const CurrentUser = {
  computed: {
    currentuserAccessToken() {
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
};
