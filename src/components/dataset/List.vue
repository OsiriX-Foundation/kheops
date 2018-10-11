/* eslint-disable */
<template>
	<div class = 'container-fluid'>
		 <b-table striped hover :items="datasets" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy">
		 	<!-- <template slot="HEAD_PatientName" slot-scope="data">
				{{data.label}} <br>
				<input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientName' placeholder="filter">
			</template>
		 	<template slot="HEAD_PatientID" slot-scope="data">
				{{data.label}} <br>
				<input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientID' placeholder="filter">
			</template>
		 	<template slot="HEAD_AccessionNumber" slot-scope="data">
				{{data.label}} <br>
				<input type = 'search' class = 'form-control form-control-sm' v-model='filters.AccessionNumber' placeholder="filter">
			</template>
		 	<template slot="HEAD_StudyDate" slot-scope="data">
				{{data.label}} <br>
				<input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDate' placeholder="filter">
			</template>
		 	<template slot="HEAD_ModalitiesInStudy" slot-scope="data">
				{{data.label}} <br>
				<input type = 'search' class = 'form-control form-control-sm' v-model='filters.ModalitiesInStudy' placeholder="filter">
			</template>
		 	<template slot="HEAD_StudyDescription" slot-scope="data">
				{{data.label}} <br>
				<input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDescription' placeholder="filter">
			</template> -->
	 	 	<template slot = 'StudyDate' slot-scope='data'>{{data.item.StudyDate[0] | formatDate}}</template>
		 </b-table>		
	</div>
</template>


<script>

import {Bus} from '@/bus'
import { mapGetters } from 'vuex'


export default {
  name: 'datasets',
	data () {
		return {
			pageNb: 1,
			fields: [
				{
					key: "PatientName",
					label: "Patient Name",
					sortable: true
				},
				{
					key: "PatientID",
					label: "MRN",
					sortable: true
				},
				{
					key: "AccessionNumber",
					label: "Accession #",
					sortable: true
				},
				{
					key: 'StudyDate',
					label: "Study Date",
					sortable: true
				},
				{
					key: 'ModalitiesInStudy',
					label: 'Modality',
					sortable: true
				}
				// {
				// 	key: 'StudyDescription',
				// 	label: 'Study Description',
				// 	sortable: true
				// }
			],
			sortBy: 'StudyDate',
			optionsNbPages: [5,10,25,50,100],
			filters: {
				PatientName: '',
				PatientID: '',
				AccessionNumber: '',
				StudyDate: '',
				ModalitiesInStudy: '',
				StudyDescription: ''
			}
		}
	},
  computed: {
	  ...mapGetters({
	  	  datasets: 'datasets'
	    }),
		totalRows () {
			return this.datasets.length;
		}
  },
  methods: {
	  scroll (person) {
	    window.onscroll = () => {
	      let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight;

	      if (bottomOfWindow) {
			  this.pageNb++;
			  this.$store.dispatch('getDatasets',{pageNb: this.pageNb})
	      }
	    };
	  },
  	
  },
  created () {
	  
	  this.$store.dispatch('getDatasets',{pageNb: this.pageNb});
  		
  },
  mounted () {
	  this.scroll();
  }
}

</script>

<style>
select{
	display: inline !important;
	width: auto;
}
</style>

/* eslint-disable */