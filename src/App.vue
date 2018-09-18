<template>
	<div id="app">
		<vue-snotify></vue-snotify>
		<nav-header></nav-header>

		<!-- content -->
	  <router-view style="margin: 90px auto"/>

		 <!-- footer -->
		<b-navbar toggleable="md" type="info" variant="dark" fixed='bottom' >
		  <b-navbar-brand href="https://www.vital-it.ch/"  tag='h4'>© SIB Swiss Institute of Bioinformatics / Vital-IT {{year}}</b-navbar-brand>
		</b-navbar>

	</div>
</template>


<script>

import navHeader from '@/components/navheader'

export default {
  components: { navHeader },
  name: 'App',
	computed:{
		year () {
			return new Date().getFullYear();
		}
	}
}
</script>


<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
/*  text-align: center;*/
  color: #2c3e50;
}

.pointer{
	cursor:pointer;
}
.ban{
	cursor:not-allowed;
}

.uppercase{
	text-transform:uppercase;
}

.has-error{
	color:#dc3545;
}
.has-error input{
	border:1px solid #dc3545 !important;
}
.has-error input:focus{
	border:1px solid #dc3545;
	box-shadow:0 0 0 0.15rem #F8E6E0 !important;
}
.has-error select{
	border:1px solid #dc3545 !important;
}
.has-error select:focus{
	border:1px solid #dc3545;
	box-shadow:0 0 0 0.15rem #F8E6E0 !important;
}



.has-error small{
	float:left;
	margin-top:5px;
	font-style:italic;
	font-size:11px;
}

a.navbar-brand {
	color: #CCC;
	font-size: 11pt;
}
</style>
