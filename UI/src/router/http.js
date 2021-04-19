import axios from 'axios';
import store from '@/store';
// eslint-disable-next-line
export const HTTP = axios.create({ baseURL: process.env.VUE_APP_URL_API });

HTTP.interceptors.request.use((config) => {
  const tmpconfig = config;
  if (tmpconfig.headers.Authorization === undefined) {
    if (window.location.pathname.includes('/view/') === false) {
      const accessToken = store.state.oidcStore.access_token;
      tmpconfig.headers.Authorization = `Bearer ${accessToken}`;
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
