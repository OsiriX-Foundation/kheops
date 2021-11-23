import Vue from 'vue';
import Router from 'vue-router';
import { vuexOidcCreateRouterMiddleware } from 'vuex-oidc';
import ListAlbums from '@/components/albums/ListAlbums';
import NewAlbum from '@/components/albums/NewAlbum';
// import Album from '@/components/albums/Album';
import Album from '@/components/album/Album';
import User from '@/components/user/user';
import store from '@/store';
import Inbox from '@/components/inbox/Inbox';
import ViewWithoutLogin from '@/components/withoutlogin/ViewWithoutLogin';
import OidcCallback from '@/components/oidc/OidcCallback';
import OidcCallbackError from '@/components/oidc/OidcCallbackError';
import OidcInitiateLogin from '@/components/oidc/OidcInitiateLogin';
import OidcLogout from '@/components/oidc/OidcLogout';

Vue.use(Router);

const router = new Router({
  mode: 'history',
  routes: [{
    path: '/',
    redirect: '/inbox',
  },
  {
    path: '/login',
    name: 'oidcInitiateLogin',
    component: OidcInitiateLogin,
    meta: {
      header: false,
    },
  },
  {
    path: '/oidc-logout',
    name: 'oidcLogout',
    component: OidcLogout,
    meta: {
      header: false,
    },
  },
  {
    path: '/oidc-callback',
    name: 'oidcCallback',
    component: OidcCallback,
    meta: {
      header: false,
    },
  },
  {
    path: '/oidc-callback-error',
    name: 'oidcCallbackError',
    component: OidcCallbackError,
    meta: {
      isPublic: true,
    },
  },
  {
    path: '/inbox',
    name: 'studies',
    component: Inbox,
  },
  {
    path: '/albums',
    name: 'albums',
    component: ListAlbums,
    meta: {
      title: 'albums',
    },
  },
  {
    path: '/albums/new',
    name: 'newAlbum',
    component: NewAlbum,
    meta: {
      title: 'newalbum',
    },
  },
  {
    path: '/albums/:album_id',
    name: 'album',
    component: Album,
    meta: {
      title: 'album',
    },
  },
  {
    path: '/albums/:album_id/:view',
    name: 'albumview',
    component: Album,
    meta: {
      title: 'album',
    },
  },
  {
    path: '/albums/:album_id/:view/:category',
    name: 'albumsettings',
    component: Album,
    meta: {
      title: 'album',
    },
  },
  {
    path: '/albums/:album_id/:view/:category/:action',
    name: 'albumsettingsaction',
    component: Album,
    meta: {
      title: 'album',
    },
  },
  {
    path: '/albums/:album_id/:view/:category/:action/:id',
    name: 'albumsettingsactionid',
    component: Album,
    meta: {
      title: 'album',
    },
  },
  {
    path: '/user',
    name: 'user',
    component: User,
    meta: {
      title: 'user',
    },
  },
  {
    path: '/user/:category',
    name: 'usercategory',
    component: User,
    meta: {
      title: 'user',
    },
  },
  {
    path: '/user/:category/:action',
    name: 'useraction',
    component: User,
    meta: {
      title: 'user',
    },
  },
  {
    path: '/user/:category/:action/:id',
    name: 'useractionid',
    component: User,
    meta: {
      title: 'user',
    },
  },
  {
    path: '/view/:token',
    name: 'viewnologin',
    component: ViewWithoutLogin,
    meta: {
      isPublic: true,
    },
  },
  {
    path: '*',
    redirect: '/inbox',
  },
  ],
});

router.beforeEach(vuexOidcCreateRouterMiddleware(store, 'oidcStore'));
export default router;
