import axios from 'axios'
import { serverURL } from '@/app_config'

export var HTTP = axios.create({ baseURL: serverURL })

HTTP.interceptors.response.use(function (response) {
	return response
}, function (error) {
	return Promise.reject(error.response.data)
})
