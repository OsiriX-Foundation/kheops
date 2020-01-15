// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
// require('../node_modules/vue-snotify/styles/material.css')

import Vue from 'vue';
import BootstrapVue from 'bootstrap-vue';
import '@/css/bootstrap_dark.css';
import '@/css/bootstrap_kheops.css';
import '@/css/main.css';
import Snotify, { SnotifyPosition } from 'vue-snotify';
import 'vue-snotify/styles/material.css';
import 'vue-awesome/icons';
import lodash from 'lodash';
import Icon from 'vue-awesome/components/Icon';
import ToggleButton from 'vue-js-toggle-button';
import VeeValidate from 'vee-validate';
import VueKeyCloak from '@dsb-norge/vue-keycloak-js';
import '@/filters/filters.js';
import VueI18n from 'vue-i18n';
import VueScrollTo from 'vue-scrollto';
import store from './store';
import Access from '@/directives/access';
import router from './router';
import App from './App';
import messages from '@/lang/messages';

Vue.config.productionTip = false;
Vue.config.performance = true;

// globally (in your main .js file)
const snotifyOptions = {
  toast: {
    position: SnotifyPosition.rightTop,
  },
};

Vue.use(Snotify, snotifyOptions);
Vue.use(BootstrapVue);
Vue.use(VeeValidate, { fieldsBagName: 'formFields' });
Vue.use(VueI18n);
Vue.use(lodash);
Vue.use(ToggleButton);
Vue.use(VueScrollTo);
// Vue.use(Vuex)
Vue.component('v-icon', Icon);
Vue.directive('access', Access);
const keycloakconfig = {
  realm: process.env.VUE_APP_REALM_KEYCLOAK,
  url: `${process.env.VUE_APP_URL_KEYCLOAK}/auth`,
  clientId: process.env.VUE_APP_CLIENTID,
  // logoutRedirectUri: 'http://logout'
};

function tokenInterceptor() {
  const user = {
    permissions: ['active'],
  };
  store.dispatch('login', user);
}

// Create VueI18n instance with options
const i18n = new VueI18n({
  locale: 'en',
  messages,
});
/* eslint-disable no-new */
Vue.use(VueKeyCloak, {
  config: keycloakconfig,
  init: {
    onLoad: 'check-sso',
    checkLoginIframe: true,
  },
  onReady: () => {
    tokenInterceptor();
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      router,
      store,
      i18n,
      components: { App },
      template: '<App/>',
    });
  },
});

// https://fr.vuejs.org/v2/guide/custom-directive.html
Vue.directive('focus', {
  inserted(el) {
    el.focus();
  },
});

// if we don't need authentication...

// new Vue({
//   el: '#app',
//   router,
//   store,
//   components: { App },
//   template: '<App/>'
// })

Icon.register({
  refresh: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    d: 'M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z',
  },
  unfold_more: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    d: 'M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83zm0 12.34L8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z',
  },
  unfold_up: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    d: 'M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83z',
  },
  unfold_down: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    d: 'M8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z',
  },
  build: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    d: 'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z',
  },
  help: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    d: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm2.07-7.75l-.9.92C13.45 12.9 13 13.5 13 15h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 .88-.36 1.68-.93 2.25z',
  },
  twitter: {
    width: 400,
    height: 400,
    viewBox: '0 0 400 400',
    d: 'M153.6,301.6c94.3,0,145.9-78.2,145.9-145.9c0-2.2,0-4.4-0.1-6.6c10-7.2,18.7-16.3,25.6-26.6 c-9.2,4.1-19.1,6.8-29.5,8.1c10.6-6.3,18.7-16.4,22.6-28.4c-9.9,5.9-20.9,10.1-32.6,12.4c-9.4-10-22.7-16.2-37.4-16.2 c-28.3,0-51.3,23-51.3,51.3c0,4,0.5,7.9,1.3,11.7c-42.6-2.1-80.4-22.6-105.7-53.6c-4.4,7.6-6.9,16.4-6.9,25.8 c0,17.8,9.1,33.5,22.8,42.7c-8.4-0.3-16.3-2.6-23.2-6.4c0,0.2,0,0.4,0,0.7c0,24.8,17.7,45.6,41.1,50.3c-4.3,1.2-8.8,1.8-13.5,1.8 c-3.3,0-6.5-0.3-9.6-0.9c6.5,20.4,25.5,35.2,47.9,35.6c-17.6,13.8-39.7,22-63.7,22c-4.1,0-8.2-0.2-12.2-0.7 C97.7,293.1,124.7,301.6,153.6,301.6',
  },
  warning: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    d: 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z',
  },
  add: {
    width: 24,
    height: 24,
    viewBox: '0 0 24 24',
    paths: [
      {
        d: 'M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z',
      },
      {
        d: 'M0 0h24v24H0z',
        fill: 'none',
      },
    ],
  },
});
