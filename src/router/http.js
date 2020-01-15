import axios from 'axios';
import Vue from 'vue';
import { serverURL } from '@/app_config';

// eslint-disable-next-line
export const HTTP = axios.create({ baseURL: serverURL });

HTTP.interceptors.request.use((config) => {
  const tmpconfig = config;
  if (tmpconfig.headers.Authorization === undefined) {
    if (window.location.pathname.includes('/view/') === false) {
      tmpconfig.headers.Authorization = `Bearer ${Vue.prototype.$keycloak.token}`;
      return tmpconfig;
    }
    if (window.location.pathname.includes('/view/') === true) {
      const [, , token] = window.location.pathname.split('/');
      tmpconfig.headers.Authorization = `Bearer ${token}`;
      return tmpconfig;
    }
  }
  return tmpconfig;
}, (error) => Promise.reject(error));

HTTP.interceptors.response.use((response) => response, (error) => Promise.reject(error));
