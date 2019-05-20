import Vue from 'vue'

export const CurrentUser = {
	data: function () {
		return {
			scope: 'viewer',
			grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer'
		}
	},
	computed: {
		currentuserAccessToken () {
			return Vue.prototype.$keycloak.token
		},
		currentuserSub () {
			let jwtInfo = this.decodeToken(Vue.prototype.$keycloak.token)
			return jwtInfo.sub
		},
		currentuserEmail () {
			let jwtInfo = this.decodeToken(Vue.prototype.$keycloak.token)
			return jwtInfo.email
		},
		currentuserFullname () {
			return Vue.prototype.$keycloak.fullName
		}
	},
	methods: {
		decodeToken (str) {
			str = str.split('.')[1]

			str = str.replace('/-/g', '+')
			str = str.replace('/_/g', '/')
			switch (str.length % 4) {
			case 0:
				break
			case 2:
				str += '=='
				break
			case 3:
				str += '='
				break
			default:
				throw new Error('Invalid token')
			}

			str = (str + '===').slice(0, str.length + (str.length % 4))
			str = str.replace(/-/g, '+').replace(/_/g, '/')

			str = decodeURIComponent(escape(atob(str)))

			str = JSON.parse(str)
			return str
		}
	}
}
