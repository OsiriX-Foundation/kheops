import Vue from 'vue';
import Router from 'vue-router';
import ListAlbums from '@/components/albums/ListAlbums';
import NewAlbum from '@/components/albums/NewAlbum';
// import Album from '@/components/albums/Album';
import Album from '@/components/album/Album';
import User from '@/components/user/user';
import store from '@/store';
import Inbox from '@/components/inbox/Inbox';
import ViewWithoutLogin from '@/components/withoutlogin/ViewWithoutLogin';
// import PermissionDenied from '@/components/user/permissionDenied'

// import {ServerTable, ClientTable, Event} from 'vue-tables-2';

Vue.use(Router);
// Vue.use(ClientTable);
function requireAuth(to, from, next) {
  store.dispatch('getCredentials').then((test) => {
    if (!test) {
      next({
        path: '/',
        query: { redirect: to.fullPath },
      });
    } else if (to.matched.some((record) => record.meta.permissions.length > 0)) {
      store.dispatch('checkPermissions', { permissions: to.meta.permissions, condition: to.meta.condition }).then((res) => {
        if (res) {
          next();
        } else {
          next({
            path: '/permissionDenied',
          });
        }
      });
    } else {
      next();
    }
  });
}

const router = new Router({
  mode: 'history',
  routes: [{
    path: '/',
    redirect: '/inbox',
  },
  {
    path: '/inbox',
    name: 'studies',
    component: Inbox,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      requiresAuth: true,
    },
  },
  {
    path: '/albums',
    name: 'albums',
    component: ListAlbums,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'albums',
      requiresAuth: true,
    },
  },
  {
    path: '/albums/new',
    name: 'newAlbum',
    component: NewAlbum,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'newalbum',
      requiresAuth: true,
    },
  },
  {
    path: '/albums/:album_id',
    name: 'album',
    component: Album,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'album',
      requiresAuth: true,
    },
  },
  {
    path: '/albums/:album_id/:view',
    name: 'albumview',
    component: Album,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'album',
      requiresAuth: true,
    },
  },
  {
    path: '/albums/:album_id/:view/:category',
    name: 'albumsettings',
    component: Album,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'album',
      requiresAuth: true,
    },
  },
  {
    path: '/albums/:album_id/:view/:category/:action',
    name: 'albumsettingsaction',
    component: Album,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'album',
      requiresAuth: true,
    },
  },
  {
    path: '/albums/:album_id/:view/:category/:action/:id',
    name: 'albumsettingsactionid',
    component: Album,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'album',
      requiresAuth: true,
    },
  },
  {
    path: '/user',
    name: 'user',
    component: User,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'user',
      requiresAuth: true,
    },
  },
  {
    path: '/user/:category',
    name: 'usercategory',
    component: User,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'user',
      requiresAuth: true,
    },
  },
  {
    path: '/user/:category/:action',
    name: 'useraction',
    component: User,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'user',
      requiresAuth: true,
    },
  },
  {
    path: '/user/:category/:action/:id',
    name: 'useractionid',
    component: User,
    beforeEnter: requireAuth,
    meta: {
      permissions: 'active',
      condition: 'any',
      title: 'user',
      requiresAuth: true,
    },
  },
  {
    path: '/view/:token',
    name: 'viewnologin',
    component: ViewWithoutLogin,
  },
  {
    path: '*',
    redirect: '/inbox',
  },
  ],
});

router.beforeEach((to, from, next) => {
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (router.app.$keycloak.authenticated) {
      next();
    } else {
      const loginUrl = router.app.$keycloak.createLoginUrl();
      window.location.replace(loginUrl);
    }
  } else {
    next();
  }
});

export default router;
