// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
// require('../node_modules/vue-snotify/styles/material.css')

import Vue from 'vue'
import App from './App'
import router from './router'
import BootstrapVue from 'bootstrap-vue'
// import 'bootstrap/dist/css/bootstrap.css'
import '@/css/bootstrap_dark.css'
import '@/css/main.css'
import Snotify, { SnotifyPosition } from 'vue-snotify'
import 'vue-snotify/styles/material.css'
import Access from '@/directives/access'
import 'vue-awesome/icons'
import lodash from 'lodash'
import Icon from 'vue-awesome/components/Icon'
import VeeValidate from 'vee-validate'
import store from './store'
import VueKeyCloak from '@dsb-norge/vue-keycloak-js'
import '@/filters/filters.js'
import VueI18n from 'vue-i18n'
import messages from '@/lang/messages'

Vue.config.productionTip = false
Vue.config.performance = true

// globally (in your main .js file)
const snotifyOptions = {
	toast: {
		position: SnotifyPosition.rightTop
	}
}

Vue.use(Snotify, snotifyOptions)
Vue.use(BootstrapVue)
Vue.use(VeeValidate, { fieldsBagName: 'formFields' })
Vue.use(VueI18n)
Vue.use(lodash)
// Vue.use(Vuex)
Vue.component('v-icon', Icon)
Vue.directive('access', Access)
const keycloakconfig = {
	realm: process.env.VUE_APP_REALM_KEYCLOAK,
	url: process.env.VUE_APP_URL_KEYCLOAK + '/auth',
	clientId: process.env.VUE_APP_CLIENTID
	// logoutRedirectUri: 'http://logout'
}

function tokenInterceptor () {
	let user = {
		permissions: ['active']
	}
	store.dispatch('login', user)
}

// Create VueI18n instance with options
const i18n = new VueI18n({
	locale: 'en',
	messages
})

/* eslint-disable no-new */
Vue.use(VueKeyCloak, {
	config: keycloakconfig,
	onReady: () => {
		tokenInterceptor()
		/* eslint-disable no-new */
		new Vue({
			el: '#app',
			router,
			store,
			i18n,
			components: { App },
			template: '<App/>'
		})
	}
})

// if we don't need authentication...

// new Vue({
//   el: '#app',
//   router,
//   store,
//   components: { App },
//   template: '<App/>'
// })

Icon.register({
	baidu: {
		width: 24,
		height: 24,
		d: 'M13,14H11V10H13M13,18H11V16H13M1,21H23L12,2L1,21Z'
	},
	refresh: {
		width: 24,
		height: 24,
		viewBox: '0 0 24 24',
		d: 'M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z'
	},
	unfold_more: {
		width: 24,
		height: 24,
		viewBox: '0 0 24 24',
		d: 'M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83zm0 12.34L8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z'
	},
	build: {
		width: 24,
		height: 24,
		viewBox: '0 0 24 24',
		d: 'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z'
	}
})
