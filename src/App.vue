<template>
  <div id="app">
    <vue-snotify />
    <nav-header />
    <nav-bar />

    <!-- content -->
    <router-view style="margin: 25px auto" />

    <send-studies />
    <!-- footer -->
    <footer>
      <b-navbar
        toggleable="md"
        type="info"
        navbar-dark
        bg-primary
      >
        <b-navbar-brand
          href="#"
          tag="h4"
        >
          © KHEOPS, inc {{ year }}
        </b-navbar-brand>
      </b-navbar>
    </footer>
  </div>
</template>

<script>

import navHeader from '@/components/navheader'
import navBar from '@/components/navbar'
import SendStudies from '@/components/study/SendStudies'

export default {
	name: 'App',
	components: { navHeader, navBar, SendStudies },
	computed: {
		year () {
			return new Date().getFullYear()
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
body {
  overflow-y: scroll;
}
</style>

