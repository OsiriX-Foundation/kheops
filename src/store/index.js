import Vue from 'vue';
import Vuex from 'vuex';
import { vuexOidcCreateStoreModule } from 'vuex-oidc';
import { oidcSettings } from '@/config/oidc';
import users from './modules/users';
import sending from './modules/sending';
import providers from './modules/providers';
import studies from './modules/studies';
import series from './modules/series';
import comments from './modules/comments';
import albums from './modules/albums';
import album from './modules/album';
import tokens from './modules/tokens';
import source from './modules/source';
import webhooks from './modules/webhooks';

Vue.use(Vuex);
export default new Vuex.Store({
  modules: {
    users,
    sending,
    providers,
    studies,
    series,
    comments,
    albums,
    album,
    tokens,
    source,
    webhooks,
    oidcStore: vuexOidcCreateStoreModule(
      oidcSettings,
      {
        namespaced: true,
      },
      // Optional OIDC event listeners
      {
        userLoaded: (user) => console.log('OIDC user is loaded:', user),
        userUnloaded: () => console.log('OIDC user is unloaded'),
        accessTokenExpiring: () => console.log('Access token will expire'),
        accessTokenExpired: () => console.log('Access token did expire'),
        silentRenewError: () => console.log('OIDC user is unloaded'),
        userSignedOut: () => console.log('OIDC user is signed out'),
        oidcError: (payload) => console.log('OIDC error', payload)
      }
    ),
  },
});
