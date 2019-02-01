import Vue from 'vue';
import {Bus} from '@/bus'
import store from '@/store'

// Initialize the annoying-background directive.
export default {
  bind: function(el, binding, vnode) {
	  Bus.$on('user.updated',v => {
		  store.dispatch('checkPermissions',{permissions: binding.value,condition: 'any'}).then(check => {
			  el.style.display = (check) ? '' : 'none';
		  });
	  });
	  store.dispatch('checkPermissions',{permissions: binding.value,condition: 'any'}).then(check => {
		  if (!check) el.style.display = 'none';
	  });


  }
}
