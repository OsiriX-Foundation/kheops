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
	}
})
