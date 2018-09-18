/* eslint-disable */
<!-- <template src="./login.html"></template> -->
<template>
	<div>Login</div>
</template>

<script>
import store from '@/store'

export default {
	name: 'login',
	data () {
		return {
			auth: {}
		}
		
	},
	created () {
		store.dispatch('login',{
			firstname: 'Robin',
			lastname: 'Liechti',
			email: 'robin.liechti@sib.swiss',
			permissions: ['active'],
			login: 'rliechti',
			jwt: 'eyJhbGciOiJIUzI1NiIsImtpZCI6IjEifQ.eyJhdWQiOiJhdXRob3JpemF0aW9uLmtoZW9wcy5vbmxpbmUiLCJzdWIiOiIxMDcxMDY3NTUxNjE1MjIzOTE5NTIiLCJlbWFpbCI6Im1zYW5rYXI0c3ViQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJleHAiOjI1MjI5NDM1NzIsImlzcyI6ImF1dGhvcml6YXRpb24ua2hlb3BzLm9ubGluZSIsImlhdCI6MTUyMjkzOTk3Mn0.W-hBqaczyiG5Q8peQGY8YvAP0DvGuy57RdMDfwuHDWs'
		}).then(user => {
			console.log(user);
			this.$router.push('/datasets')
		})
	}
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.login_container{
	margin-top:100px;
}
form{
	margin:20px 0;
}
.card{
	margin:20px 0;
}
</style>
/* eslint-disable */