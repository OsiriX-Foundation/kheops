/* eslint-disable */
import Vue from 'vue';
import store from '@/store';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

export const CurrentUser = {
  computed: {
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
    currentuserAccessToken() {
      return store.state.oidcStore.access_token;
    },
    currentuserSub() {
      return store.state.oidcStore.user !== null ? store.state.oidcStore.user.sub : null;
    },
    currentuserEmail() {
      return store.state.oidcStore.user !== null ? store.state.oidcStore.user.email : null;
    },
    currentuserFullname() {
      return store.state.oidcStore.user !== null ? store.state.oidcStore.user.name : null;
    },
  },
  methods: {
    getCurrentuserAccessToken(authenticated) {
      if (window.location.pathname.includes('view')) {
        const [, , token] = window.location.pathname.split('/');
        return token;
      } if (authenticated) {
        return this.currentuserAccessToken;
      }
      return '';
    },
    postAccessToken(access_token) {
      const url = '/register';
      const formData = httpoperations.getFormData({ access_token });
      return HTTP.post(url, formData);
    },
  },
};
