import { Bus } from '@/bus'
import store from '@/store'

// Initialize the annoying-background directive.
export default {
	bind: function (el, binding) {
		Bus.$on('user.updated', () => {
			store.dispatch('checkPermissions', { permissions: binding.value, condition: 'any' }).then(check => {
				el.style.display = (check) ? '' : 'none'
			})
		})
		store.dispatch('checkPermissions', { permissions: binding.value, condition: 'any' }).then(check => {
			if (!check) el.style.display = 'none'
		})
	}
}
