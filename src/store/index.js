import Vue from 'vue'
import Vuex from 'vuex'
import users from './modules/users'
import studies from './modules/studies'
import albums from './modules/albums'
import album from './modules/album'
import sending from './modules/sending'
import providers from './modules/providers'
import studiesTest from './modules/studies_test'

Vue.use(Vuex)

export default new Vuex.Store({
	modules: {
		users,
		studies,
		albums,
		album,
		sending,
		providers,
		studiesTest
	}
})
