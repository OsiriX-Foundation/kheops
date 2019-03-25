import { HTTP } from '@/router/http'

export const ViewerToken = {
	data: function () {
		return {
			scope: 'viewer',
			grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer'
		}
	},
	methods: {
		getViewerToken (token, studyInstanceUID, source) {
			const body = {
				grant_type: this.grant_type,
				assertion: token,
				scope: this.scope,
				study_instance_uid: studyInstanceUID,
				source_type: source === 'inbox' ? source : 'album',
				source_id: source === 'inbox' ? '' : source
			}
			let bodyParams = []
			Object.entries(body).forEach(function (param) {
				if (param[1] !== '') bodyParams.push(param.join('='))
			})

			return new Promise((resolve, reject) => {
				HTTP.post(`/token`, bodyParams.join('&')).then(res => {
					resolve(res)
				}).catch(err => {
					reject(err)
				})
			})
		}
	}
}
