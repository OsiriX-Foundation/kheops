import Vue from 'vue'
import Router from 'vue-router'
import Study from '@/components/inbox/List'
import Albums from '@/components/albums/List'
import NewAlbum from '@/components/albums/NewAlbum'
import Album from '@/components/albums/Album'
import store from '@/store'

// import PermissionDenied from '@/components/user/permissionDenied'

// import {ServerTable, ClientTable, Event} from 'vue-tables-2';

Vue.use(Router)
// Vue.use(ClientTable);

const router = new Router({
	mode: 'history',
	routes: [{
		path: '/inbox',
		name: 'studies',
		component: Study,
		beforeEnter: requireAuth,
		meta: { permissions: 'active', condition: 'any' }
	},
	{
		path: '/albums',
		name: 'albums',
		component: Albums,
		beforeEnter: requireAuth,
		meta: { permissions: 'active', condition: 'any' }
	},
	{
		path: '/albums/new',
		name: 'new_album',
		component: NewAlbum,
		beforeEnter: requireAuth,
		meta: { permissions: 'active', condition: 'any' }

	},
	{
		path: '/albums/:album_id',
		name: 'album',
		component: Album,
		beforeEnter: requireAuth,
		meta: { permissions: 'active', condition: 'any' }

	},
	{
		path: '*',
		name: 'inbox',
		component: Study,
		beforeEnter: requireAuth,
		meta: { permissions: 'active', condition: 'any' }
	}
	]
})

function requireAuth (to, from, next) {
	store.dispatch('getCredentials').then(test => {
		if (!test) {
			next({
				path: '/',
				query: { redirect: to.fullPath }
			})
		} else {
			if (to.matched.some(record => record.meta.permissions.length > 0)) {
				store.dispatch('checkPermissions', { permissions: to.meta.permissions, condition: to.meta.condition }).then(res => {
					if (res) {
						next()
					} else {
						next({
							path: '/permissionDenied'
						})
					}
				})
			} else {
				next()
			}
		}
	})
}
export default router
