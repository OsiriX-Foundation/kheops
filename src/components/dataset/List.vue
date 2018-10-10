/* eslint-disable */
<template>
	<div class = 'container-fluid'>
		 <p>{{datasets.length}} datasets</p>
		 <b-table striped hover :items="datasets"></b-table>
		
	</div>
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