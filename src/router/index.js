import Vue from 'vue';
import Router from 'vue-router';
import ListAlbums from '@/components/albums/ListAlbums';
import NewAlbum from '@/components/albums/NewAlbum';
import Album from '@/components/albums/Album';
import User from '@/components/user/user';
import store from '@/store';
import Inbox from '@/components/inbox/Inbox';

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
    },
  },
  {
    path: '*',
    redirect: '/inbox',
  },
  /*
  {
  path: '/favorites',
  name: 'favorites',
  component: Favorites,
  beforeEnter: requireAuth,
  meta: { permissions: 'active', condition: 'any' }
  }
  */
  ],
});

router.beforeEach((to, from, next) => {
  next();
});

export default router;
