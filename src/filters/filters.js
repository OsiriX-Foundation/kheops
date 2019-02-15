import Vue from 'vue'
import moment from 'moment'

Vue.filter('formatDate', value => {
	if (value) {
		return moment(String(value)).format('MMM Do YYYY')
	}
	return value
})

Vue.filter('formatDateTime', value => {
	if (value) {
		return moment(String(value)).format('MMM Do YYYY HH:mm')
	}
	return value
})


Vue.filter('formatTime', value => {
	if (value) {
		return moment(String(value)).format('HH:mm')
	}
	return value
})

Vue.filter('nl2br', value => {
	return value.replace('\n', '<br>')
})

Vue.filter('formatModality', value => {
	return value.split(',').join('/')
})

Vue.filter('formatPermissions', item => {
	let perms = []
	_.forEach(item, (value, key) => {
		if (key.indexOf('permission') > -1 && value) {
			perms.push(key.replace('_permission', ''))
		}
	})
	return (perms.length) ? perms.join(', ') : '-'
})
