<template>
	<div id="app">
		<vue-snotify></vue-snotify>
		<nav-header></nav-header>
		<nav-bar></nav-bar>

		<!-- content -->
	  <router-view style="margin: 90px auto"/>

		 <!-- footer -->
		<b-navbar toggleable="md" type="info" navbar-dark bg-primary>
		  <b-navbar-brand href="https://www.vital-it.ch/"  tag='h4'>© SIB Swiss Institute of Bioinformatics / Vital-IT {{year}}</b-navbar-brand>
		</b-navbar>

	</div>
</template>


<script>

import navHeader from '@/components/navheader'
import navBar from '@/components/navbar'

export default {
  components: { navHeader,navBar },
  name: 'App',
	computed:{
		year () {
			return new Date().getFullYear();
		}
	}
}
</script>


<style>

.pointer{
	cursor:pointer;
}
.ban{
	cursor:not-allowed;
}

.uppercase{
	text-transform:uppercase;
}


a.navbar-brand {
	color: #CCC;
	font-size: 11pt;
}
</style>
