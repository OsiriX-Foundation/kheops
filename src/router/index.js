import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/Home'
import Login from '@/components/user/Login'
import Dataset from '@/components/dataset/List'
import store from '@/store'

import PermissionDenied from '@/components/user/PermissionDenied'

// import {ServerTable, ClientTable, Event} from 'vue-tables-2';

Vue.use(Router);
// Vue.use(ClientTable);

const router = new Router({
	mode: 'history',
  routes: [
    {
     path: '/login',
     name: 'login',
     component: Login
     },
	 {
		 path: '/datasets',
		 name: 'datasets',
		 component: Dataset
	 },
	{
		path: '*',
		name: 'home',
		component: Home,
  	    beforeEnter: requireAuth,
  	    meta: {permissions: 'active',condition: 'any'}
	}
  ]
});

function requireAuth (to, from, next){
	store.dispatch('getCredentials').then(test => {
		if (!test) {
			next({
				path: '/login',
				query: {redirect: to.fullPath}
			})
		} else {
			if (to.matched.some(record => record.meta.permissions.length > 0)) {
				store.dispatch('checkPermissions',{permissions: to.meta.permissions, condition: to.meta.condition}).then(res => {
					if (res) {
						next();
					} else {
						next({
							path: '/permissionDenied'
						});
					}					
				})
			} else {
				next();
			}

		}
	});
}
export default router;


