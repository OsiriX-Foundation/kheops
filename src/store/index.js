import Vue from 'vue'
import Vuex from 'vuex'
import users from './modules/users'
import datasets from './modules/datasets'

Vue.use(Vuex)


export default new Vuex.Store({
  modules: {
    users,
    datasets
  }
})