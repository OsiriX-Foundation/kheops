<template>
	<b-jumbotron bg-variant="info" text-variant="white" border-variant="dark">
	  <template slot="header">
	    Permission denied
	  </template>
	  <template slot="lead">
		  Sorry, you are not authorized to view this section
	  </template>
	  <hr class="my-4">
	  <p>
		  <b-button size = 'lg' @click='changeLocation("home")'>Go To Home Page</b-button>
		  <b-button size = 'lg' @click='changeLocation("login")'>Go To Login Page</b-button>
	  </p>
	</b-jumbotron>
</template>

<script>
export default {
	name: 'permissionDenied',
	data () {
		return {
		}
	},
	methods:{
		changeLocation (path) {
			var vm = this;
			vm.$router.push("/"+path);
		}
	}
}

</script>