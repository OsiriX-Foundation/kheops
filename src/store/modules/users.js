import {HTTP} from '@/router/http';
import Base64 from '@/mixins/base64';
// initial state
const state = {
	all: [],
	current: {
		username: null,
		firstname: null,
		lastname: null,
		fullname: null,
		email: null,
		permissions: [],
		jwt: null
	}

}

// getters
const getters = {
	currentUser: state => state.current
}

// actions
const actions = {
	login ({ commit }, userData) {
		return new Promise((resolve, reject) => {
			
				var loggedUser = {
					username: userData.login,
					jwt: userData.jwt,
					fullname: userData.fullname,
					firstname: userData.firstname,
					lastname: userData.lastname,
					email: userData.email,
					permissions: userData.permissions
				};
				HTTP.defaults.headers.common['authorization'] = 'Bearer '+userData.jwt;
				localStorage.setItem('currentUser',JSON.stringify(loggedUser));
				commit('LOGIN',loggedUser);
				resolve(userData);
		})
	},

	getCredentials({ commit }){
		if (state.current.user_id){
			return state.current
		}
		else {
			let user = localStorage.getItem('currentUser');
			if (user){
				user = JSON.parse(user);
				if (user.jwt){
					HTTP.defaults.headers.common['authorization'] = 'Bearer '+user.jwt;
				}
				commit('LOGIN',user);
				return true;
			}
			else {
				return false;
			}
		}
	},
	checkPermissions({ commit },params){
		let permissionsToCheck = params.permissions
		let condition = params.condition
		if (condition != 'all') condition = 'any';
		if (!state.current.permissions) return false;
		if (condition == 'any') return state.current.permissions.some(v => permissionsToCheck.includes(v));
		else if (condition == 'all') return _.difference(permissionsToCheck, state.current.permissions).length === 0;
		return false;
	},
	logout ({ commit }){
		localStorage.removeItem('currentUser');
		HTTP.defaults.auth = {};
		commit('LOGOUT');
	}


}

// mutations
const mutations = {
	SET_USERS (state, users) {
		state.all = users
	},
	LOGIN (state, user){
		state.current = user;
	},
	LOGOUT (state){
		state.current = {
			user_id: null,
			username: null,
			fullname: null,
			firstname: null,
			lastname: null,
			email: null,
			jwt: null,
			permissions: null
		}

	}
}

export default {
	state,
	getters,
	actions,
	mutations
}