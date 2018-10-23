/* eslint-disable */
<template>	
	<div class = 'container-fluid'>
		<div class="my-3">
			<span v-if="selectedStudiesNb > 0" >number of studies {{selectedStudiesNb}}</span>
			<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="paper-plane"></v-icon></span><br>Send</button>
			<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="book"></v-icon></span><br>add to an album</button>
			<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="download"></v-icon></span><br>Download</button>
			<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="star"></v-icon></span><br>add to favorites</button>
			<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="trash"></v-icon></span><br>Delete</button>

		</div>
		
		<b-table striped hover :items="datasets" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"  > <!-- @row-hovered="rowOver" -->
		  <!-- <b-table striped hover :items="datasets" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"> -->
		<template slot="HEAD_PatientName" slot-scope="data">
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
			
			<template slot="HEAD_is_selected" scope="head">
			      <!-- <input type="checkbox" @click.stop="toggleSelected" v-model="datasets.allSelected"> -->
  				<b-form-checkbox @click.native.stop @change="selectAll(datasets.allSelected)" v-model="datasets.allSelected" name="allSelected">
  				</b-form-checkbox>
			    </template>
			<template slot="is_selected" slot-scope="data">
				<!-- <b-form-checkbox @click.native.stop @change="row.is_selected = true  " >
				</b-form-checkbox> -->
				<b-form-checkbox v-model = "data.item.is_selected" @click.native.stop @change="toggleSelected(data.item.is_selected)" >
				</b-form-checkbox>
			</template>
		 	<template slot = 'PatientName' slot-scope='data'>
				{{data.item.PatientName}}
				<div class = 'patientNameIcons'>
					<span @click = "addFavorite(data.index,'is_favorite')" ><v-icon  v-if="data.item.is_favorite" class="align-middle" style="margin-right:0" name="star"></v-icon>
					<v-icon v-else class="align-middle" style="margin-right:0" name="star-o"></v-icon>
					</span>
						<span @click="handleComments(data.index,'comment')"><v-icon v-if="data.item.comment" class="align-middle" style="margin-right:0" name="comment"></v-icon><v-icon v-else  class="align-middle" style="margin-right:0" name="comment-o"></v-icon>
						</span>
					<span><v-icon class="align-middle" style="margin-right:0" name="link"></v-icon></span>
				</div>				
			</template>
			
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
			active: false,
			selectedStudiesNb:0,
			fields: [
				{
					key: 'is_selected',
					label: '',
					sortable: true
				},
				{
					key: "PatientName",
					label: "Patient Name",
					tdClass: 'patientName',
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
			],
			sortBy: 'StudyDate',
			optionsNbPages: [5,10,25,50,100],
			filters: {
				PatientName: '',
				PatientID: '',
				AccessionNumber: '',
				StudyDate: '',
				ModalitiesInStudy: '',
			},
			test:"",
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
	  toggleSelected(is_selected) {
		  if(is_selected ) this.selectedStudiesNb = this.selectedStudiesNb -1;
		  else{this.selectedStudiesNb += 1 }
		
		  
	  },
	  mouseOver(item, index, event){
          // item.show_icon = true;
		  // console.log(item);
		  console.log('test');
		  // console.log(index);
 // 		  console.log(event);
		  
		  // for (let datasetIndex in this.datasets ){
//  			  if (datasetIndex != index && this.datasets[datasetIndex].show_icon!== 'undefined' &&  this.datasets[datasetIndex]!== false)this.datasets[datasetIndex].show_icon = false;
//
//  		  }
	  },
	  addFavorite(index,entity){
		  this.datasets[index][entity]= !this.datasets[index][entity];
		  console.log(this.datasets[index]);
	  	
	  },
	  handleComments(index,entity){
		  this.datasets[index][entity]= !this.datasets[index][entity];
	  	
	  },
	  selectAll(is_selected){
		 
		  if (is_selected) this.selectedStudiesNb = 0;
		  else{this.selectedStudiesNb =  _.size(this.datasets)}
		_.forEach(this.datasets, function(dataset,index) {
			dataset.is_selected = !is_selected;
			
			 
		});
		this.datasets.allSelected = ! this.datasets.allSelected;
	  }
	 
  	
  },
  created () {
	  
	  this.$store.dispatch('getDatasets',{pageNb: this.pageNb});
  		
  },
  mounted () {
	  this.scroll();
  },
	watch : {
		filters: {
			handler: function(filters) {
				let filterParams = "";
				_.forEach(filters, function(value,filterName) {
					if (value){
						filterParams += '&'+filterName+'='+value;

					}
				});
					if(filterParams)this.$store.dispatch('getDatasets',{pageNb: this.pageNb,filterParams:filterParams});
			 
			},
			deep: true
		}
	  
	  
  }
  
}

</script>

<style>
select{
	display: inline !important;
	width: auto;
}
.btn-link {
  font-weight: 400;
  color: white;
  background-color: transparent;
}

.btn-link:hover {
  color: #c7d1db;
  text-decoration: underline;
  background-color: transparent;
  border-color: transparent;
}

.patientNameIcons{
	margin-left: 10px;
	display: none;
}

.patientName:hover .patientNameIcons {
	 display:inline; 
 }
 
 .patientNameIcons span{
	 margin: 0 3px;
 }
</style>

/* eslint-disable */