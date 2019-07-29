import axios from 'axios'
import { serverURL } from '@/app_config'
import Vue from 'vue'

export var HTTP = axios.create({ baseURL: serverURL })

HTTP.interceptors.request.use(function (config) {
	config.headers.Authorization = `Bearer ${Vue.prototype.$keycloak.token}`
	return config
}, function (error) {
	return Promise.reject(error)
})

HTTP.interceptors.response.use(function (response) {
	return response
}, function (error) {
	return Promise.reject(error.response)
})
