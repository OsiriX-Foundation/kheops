/* eslint-disable */
<template>
<p>{{datasets.length}} datasets</p>
</template>


<script>

import {Bus} from '@/bus'
import { mapGetters } from 'vuex'


export default {
  name: 'datasets',
  computed: {
	  ...mapGetters({
	  	  datasets: 'datasets'
	    })
  },
  created () {
	  
		  		this.$store.dispatch('getDatasets').then(res => {
		  			console.log(res);
		})
  		
  }
}

</script>

<style>

</style>

/* eslint-disable */