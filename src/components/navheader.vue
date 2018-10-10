/* eslint-disable */
<template>
	<!-- Navbar -->
	<b-navbar toggleable="md" type="light" variant="light" fixed='top'>

	<b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

	<b-navbar-brand href="#"><img src="../assets/sib_logo_small.gif" style="margin-right:5px"><router-link to="/">Kheops</router-link></b-navbar-brand>

	<b-collapse is-nav id="nav_collapse">

	<b-navbar-nav>
	<b-nav-item v-access = '"active"'><router-link to="/datasets">Datasets</router-link></b-nav-item>
</b-navbar-nav>

<!-- Right aligned nav items -->
<b-navbar-nav class="ml-auto">

<b-navbar-nav right>
<b-nav-item v-access = '"admin"'><router-link to="/admin">Admin</router-link></b-nav-item>
<b-nav-item v-access = '"active"'>Welcome {{user.fullname}}</b-nav-item>
<b-nav-item v-access = '"active"'><a class = 'pointer' @click='logout()'><icon name = 'sign-out'></icon></a></b-nav-item>
</b-navbar-nav>



</b-navbar-nav>

</b-collapse>
</b-navbar>

</template>

<script>

import {Bus} from '@/bus';
import { mapGetters } from 'vuex'
import store from '@/store'
import Vue from 'vue'

export default {
  name: 'navHeader',
  computed: {
	  ...mapGetters({
	  	  user: 'currentUser'
	    })
  },
  methods: {
	  logout () {
	  	store.dispatch('logout').then(data => {
		`${Vue.prototype.$keycloak.logoutFn()}`
	  	})
	  }
  }
}

</script>

/* eslint-disable */