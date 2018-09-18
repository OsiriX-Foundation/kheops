// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
// require('../node_modules/vue-snotify/styles/material.css')

import Vue from 'vue'
import App from './App'
import router from './router'
import BootstrapVue from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import Snotify, { SnotifyPosition } from 'vue-snotify'
import 'vue-snotify/styles/material.css'
import Access from '@/directives/access'
import 'vue-awesome/icons';
import lodash from 'lodash'
import Icon from 'vue-awesome/components/Icon'
import VeeValidate from 'vee-validate'
import store from './store'
import VueKeyCloak from '@dsb-norge/vue-keycloak-js'

Vue.config.productionTip = false

Vue.use(Snotify, options)
Vue.use(BootstrapVue)
Vue.use(VeeValidate, {fieldsBagName: 'formFields'})
// Vue.use(Vuex)
Vue.component('icon', Icon)
Vue.directive('access',Access);

// globally (in your main .js file)
const options = {
  toast: {
    position: SnotifyPosition.rightTop
  }
}

const keycloakconfig = {
  authRealm: 'test',
  authUrl: 'http://test',
  authClientId: '12345',
  logoutRedirectUri: 'http://logout'
}

function tokenInterceptor () {
  axios.interceptors.request.use(config => {
    config.headers.Authorization = `Bearer ${Vue.prototype.$keycloak.token}`
    return config
  }, error => {
    return Promise.reject(error)
  })
}


/* eslint-disable no-new */
// Vue.use(VueKeyCloak, {
// 	config: keycloakconfig,
//   onReady: (keycloak) => {
//     console.log(`I wonder what Keycloak returns: ${keycloak}`)
// 	   tokenInterceptor()
//     /* eslint-disable no-new */
// 	  new Vue({
// 	    el: '#app',
// 	    router,
// 	    store,
// 	    components: { App },
// 	    template: '<App/>'
// 	  })
//   }
// })

	  new Vue({
	    el: '#app',
	    router,
	    store,
	    components: { App },
	    template: '<App/>'
	  })
