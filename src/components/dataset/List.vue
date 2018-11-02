/* eslint-disable */
<template>
	<div class = 'container-fluid'>
		<div class="my-3 selection-button-container">
			<span  v-if="selectedStudiesNb > 0">
				<span >{{selectedStudiesNb}} studies are selected</span>
				<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="paper-plane"></v-icon></span><br>Send</button>
				<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="book"></v-icon></span><br>add to an album</button>
				<button type="button" class="btn btn-link btn-sm text-center" @click = "downloadSelectedStudies()"><span><v-icon class="align-middle" name="download"></v-icon></span><br>Download</button>
				<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="star"></v-icon></span><br>add to favorites</button>
				<button type="button" class="btn btn-link btn-sm text-center" @click = "deleteSelectedStudies()"><span><v-icon class="align-middle" name="trash"></v-icon></span><br>Delete</button>
			</span>
			<button type = 'button' class = "btn btn-link btn-lg float-right" @click='showFilters=!showFilters'><v-icon name = 'search' scale='2'/></button>

		</div>
		<b-table  striped :items="datasets" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"  @sort-changed="sortingChanged" :no-local-sorting="true">
	
			<template slot="HEAD_is_selected" scope="head">
				<b-button variant="link" size="sm"  class="mr-2" >
					<v-icon  class="align-middle"   name="chevron-down"></v-icon>
				</b-button>
  				<b-form-checkbox @click.native.stop @change="selectAll(datasets.allSelected)" v-model="datasets.allSelected" name="allSelected">
  				</b-form-checkbox>
			</template>
			<template slot="HEAD_PatientName" slot-scope="data">
					<div v-if='showFilters' @click.stop='' ><input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientName' placeholder="filter"> <br></div>
					{{data.label}} 
					
			</template>
		 	<template slot="HEAD_PatientID" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientID' placeholder="filter"> <br></div>
				{{data.label}}

			</template>
		 	<template slot="HEAD_AccessionNumber" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.AccessionNumber' placeholder="filter"> <br></div>
				{{data.label}}
				
			</template>
		 	<template slot="HEAD_StudyDate" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDate' placeholder="filter"> <br></div>
				{{data.label}}
				
			</template>
		 	<template slot="HEAD_ModalitiesInStudy" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.ModalitiesInStudy' placeholder="filter"><br></div>
				{{data.label}} <br>
				
			</template>
		
			<template slot="is_selected" slot-scope="row">
				<b-form-group>

					<b-button variant="link" size="sm" @click.stop="row.toggleDetails" class="mr-2">
						<v-icon v-if= "row.detailsShowing" class="align-middle"  @click.stop="row.toggleDetails" name="chevron-down"></v-icon>
						<v-icon v-else class="align-middle"  @click.stop="row.toggleDetails" name="chevron-right"></v-icon>

					</b-button>
					<b-form-checkbox v-model = "row.item.is_selected" @click.native.stop @change="toggleSelected(row.item.is_selected)" >
					</b-form-checkbox>
					
				</b-form-group>
				

			</template>
			<template slot="row-details" slot-scope="row">
				<b-card>
					<p>here will come the Series</p>
					<!-- <b-row class="mb-2">
						<b-col sm="3" class="text-sm-right"><b>Age:</b></b-col>
						<b-col>{{ row.item.age }}</b-col>
					</b-row>
					<b-row class="mb-2">
						<b-col sm="3" class="text-sm-right"><b>Is Active:</b></b-col>
						<b-col>{{ row.item.isActive }}</b-col>
					</b-row> -->
					<b-button size="sm" @click="row.toggleDetails">Hide Details</b-button>
				</b-card>
			</template>
		 	<template slot = 'PatientName' slot-scope='data'>
				{{data.item.PatientName}}
				<div class = 'patientNameIcons'>
					<span @click = "toggleFavorite(data.index)" :class="data.item.is_favorite?'selected':''"><v-icon  v-if="data.item.is_favorite" class="align-middle" style="margin-right:0" name="star"></v-icon>
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
			sortDesc: true,
			limit: 10,
			optionsNbPages: [5,10,25,50,100],
			showFilters: false,
			filterTimeout: null,
			filters: {
				PatientName: '',
				PatientID: '',
				AccessionNumber: '',
				StudyDate: '',
				ModalitiesInStudy: '',
			},
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
			  this.$store.dispatch('getDatasets',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})
	      }
	    };
	  },
	  sortingChanged (ctx) {
	      // ctx.sortBy   ==> Field key for sorting by (or null for no sorting)
	      // ctx.sortDesc ==> true if sorting descending, false otherwise

		  this.pageNb = ctx.currentPage;
		  this.sortBy = ctx.sortBy;
		  this.sortDesc = ctx.sortDesc;
		  this.limit = this.datasets.length;
		  this.$store.dispatch('getDatasets',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})

		  console.log(ctx);
	    },
	  toggleSelected(is_selected) {
		  if (is_selected ) this.selectedStudiesNb = this.selectedStudiesNb -1;
		  else {this.selectedStudiesNb += 1 }


	  },

	  toggleFavorite(index){
		  this.$store.commit('TOGGLE_FAVORITE',{index: index})
		  // this.datasets[index][entity]= !this.datasets[index][entity];
		  // console.log(this.datasets[index]);

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
	  },
	  deleteSelectedStudies(){
		 var vm = this;
		 var i;
		 for (i = this.datasets.length-1; i > -1; i--) {
			 if(this.datasets[i].is_selected){
 			 	vm.$store.dispatch('deleteStudy',{StudyInstanceUID:this.datasets[i].StudyInstanceUID})
 				vm.$delete(vm.datasets, i);
 			 }
		 }

	  },
	  downloadSelectedStudies(){
		   var vm = this;
		   _.forEach(this.datasets, function(dataset,index) {
			   if( dataset.is_selected){
				   console.log(dataset);
				   vm.$store.dispatch('downloadStudy',{StudyInstanceUID:dataset.StudyInstanceUID})
			   }
		   });

	  },
	  searchOnline(filters){
		let filterParams = "";
		_.forEach(filters, function(value,filterName) {
			if (value){
				filterParams += '&'+filterName+'=*'+value+"*";
			}
		});
		  this.$store.dispatch('getDatasets',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})
	  }


  },
  created () {

	  this.$store.dispatch('getDatasets',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})

  },
  mounted () {
	  this.scroll();
  },
	watch : {
		filters: {
			handler: function(filters) {
				if (this.filterTimeout) {
				    clearTimeout(this.filterTimeout);
				}
				this.filterTimeout = setTimeout( () => this.searchOnline(filters), 300);
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
	visibility: hidden;
	display: inline;
	cursor: pointer;
}

.patientName:hover .patientNameIcons {
	 visibility:visible; 
 }
 
 .patientNameIcons > span.selected{
	 visibility:visible !important;  	
 }
 
 .patientNameIcons span{
	 margin: 0 3px;
 }
 
 .selection-button-container{
	 height: 60px;
 }
</style>

/* eslint-disable */