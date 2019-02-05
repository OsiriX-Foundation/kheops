import Vue from 'vue'
import axios from 'axios'
import Base64 from '@/mixins/base64'
import {serverURL} from '@/app_config'

export var HTTP = axios.create({ baseURL: serverURL })

HTTP.interceptors.response.use(function (response) {
	return response
}, function (error) {
	return Promise.reject(error.response.data)
})
