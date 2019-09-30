import axios from 'axios';
import Vue from 'vue';
import { serverURL } from '@/app_config';

export const HTTP = axios.create({ baseURL: serverURL });

HTTP.interceptors.request.use((config) => {
  const tmpconfig = config;
  if (window.location.pathname.includes('/view/')) {
    const [, , token] = window.location.pathname.split('/');
    tmpconfig.headers.Authorization = `Bearer ${token}`;
  } else {
    tmpconfig.headers.Authorization = `Bearer ${Vue.prototype.$keycloak.token}`;
  }
  return tmpconfig;
}, (error) => Promise.reject(error));

HTTP.interceptors.response.use((response) => response, (error) => Promise.reject(error));
