/* eslint-disable */

<i18n>
{
  "en": {
  },
  "fr": {	
  }
}
</i18n>



<template>
	<div class = 'container-fluid'>
	</div>
</template>

<script>

import {Bus} from '@/bus'
import { mapGetters } from 'vuex'


export default {
  name: 'albums',
	data () {
		return {
		}
	},
	components: {},
  computed: {
	  ...mapGetters({
	  	  albums: 'albums'
	    })
  },
  methods: {
  },
  created () {

  },
  mounted () {
  },
}

</script>

<style>
</style>

/* eslint-disable */