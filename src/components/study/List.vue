/* eslint-disable */

<i18n>
{
  "en": {
    "PatientName": "Patient Name",
	"Modality": "Modality",
	"StudyDate": "Study Date",
	"AccessionNumber": "Accession #",
	"PatientID": "Patient ID"
  },
  "fr": {
    "PatientName": "Nom du patient",
	"Modality": "Modalité",
	"StudyDate": "Date de l'étude",
	"AccessionNumber": "# accession",
	"PatientID": "ID patient"
	
  }
}
</i18n>



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
		<b-table  striped :items="studies" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"  @sort-changed="sortingChanged" :no-local-sorting="true">
	
			<template slot="HEAD_is_selected" scope="head">
				<b-button variant="link" size="sm"  class="mr-2" >
					<v-icon  class="align-middle"   name="chevron-down"></v-icon>
				</b-button>
  				<b-form-checkbox @click.native.stop @change="selectAll(studies.allSelected)" v-model="studies.allSelected" name="allSelected">
  				</b-form-checkbox>
			</template>
			<template slot="HEAD_PatientName" slot-scope="data">
					<div v-if='showFilters' @click.stop='' ><input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientName' placeholder="filter"> <br></div>
					{{$t(data.label)}}
					
			</template>
		 	<template slot="HEAD_PatientID" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientID' placeholder="filter"> <br></div>
				{{$t(data.label)}}

			</template>
		 	<template slot="HEAD_AccessionNumber" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.AccessionNumber' placeholder="filter"> <br></div>
				{{$t(data.label)}}
				
			</template>
		 	<template slot="HEAD_StudyDate" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDate' placeholder="filter"> <br></div>
				{{$t(data.label)}}
				
			</template>
		 	<template slot="HEAD_ModalitiesInStudy" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.ModalitiesInStudy' placeholder="filter"><br></div>
				{{$t(data.label)}}
				
			</template>
		
			<template slot='ModalitiesInStudy' slot-scope="data">
				{{data.value[0].replace(","," / ")}}
			</template>
		
			<template slot="is_selected" slot-scope="row">
				<b-form-group>

					<b-button variant="link" size="sm" @click.stop="showSeries(row)" class="mr-2">
						<v-icon v-if= "row.detailsShowing" class="align-middle"  @click.stop="row.toggleDetails" name="chevron-down"></v-icon>
						<v-icon v-else class="align-middle"  @click.stop="row.toggleDetails" name="chevron-right"></v-icon>

					</b-button>
					<b-form-checkbox v-model = "row.item.is_selected" @click.native.stop @change="toggleSelected(row.item.is_selected)" >
					</b-form-checkbox>
					
				</b-form-group>
				

			</template>
			<template slot="row-details" slot-scope="row">
				<b-card>
					<!-- <p>{{row.item.series.length}} serie{{row.item.series.length>1?"s":""}}</p> -->
					<div class = 'row'>
						<div class = 'col-sm-12 col-md-6' v-for='serie in row.item.series'>
							<series-summary
							      v-bind:series="serie"
								  v-bind:StudyInstanceUID="row.item.StudyInstanceUID[0]"
							      v-bind:key="serie.SerieInstanceUID">
							    </series-summary>
						</div>
						
					</div>

					<!-- <b-row class="mb-2">
						<b-col sm="3" class="text-sm-right"><b>Age:</b></b-col>
						<b-col>{{ row.item.age }}</b-col>
					</b-row>
					<b-row class="mb-2">
						<b-col sm="3" class="text-sm-right"><b>Is Active:</b></b-col>
						<b-col>{{ row.item.isActive }}</b-col>
					</b-row> -->
				</b-card>
			</template>
		 	<template slot = 'PatientName' slot-scope='data'>
				{{data.item.PatientName}}
				<div class = 'patientNameIcons'>
					<span @click = "toggleFavorite(data.index,'study')" :class="data.item.is_favorite?'selected':''"><v-icon  v-if="data.item.is_favorite" class="align-middle" style="margin-right:0" name="star"></v-icon>
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

import seriesSummary from '@/components/study/seriesSummary'

export default {
  name: 'studies',
	data () {
		return {
			pageNb: 1,
			active: false,
			selectedStudiesNb:0,
			fields: [
				{
					key: 'is_selected',
					label: '',
					sortable: true,
					class: 'td_checkbox'
				},
				{
					key: "PatientName",
					label: "PatientName",
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
					label: "AccessionNumber",
					sortable: true
				},
				{
					key: 'StudyDate',
					label: "StudyDate",
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
			limit: 8,
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
	components: {seriesSummary},
  computed: {
	  ...mapGetters({
	  	  studies: 'studies'
	    }),
		totalRows () {
			return this.studies.length;
		}
  },
  methods: {
	  scroll (person) {
	    window.onscroll = () => {
	      let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight;

	      if (bottomOfWindow) {
			  this.pageNb++;
			  this.$store.dispatch('getStudies',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})
	      }
	    };
	  },
	  sortingChanged (ctx) {
	      // ctx.sortBy   ==> Field key for sorting by (or null for no sorting)
	      // ctx.sortDesc ==> true if sorting descending, false otherwise

		  this.pageNb = ctx.currentPage;
		  this.sortBy = ctx.sortBy;
		  this.sortDesc = ctx.sortDesc;
		  this.limit = this.studies.length;
		  this.$store.dispatch('getStudies',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})

	    },
		showSeries (row) {
			if (!row.detailsShowing){
				this.$store.dispatch('getSeries',{StudyInstanceUID: row.item.StudyInstanceUID[0]})
			}
			row.toggleDetails();
		},
	  toggleSelected(is_selected) {
		  if (is_selected ) this.selectedStudiesNb = this.selectedStudiesNb -1;
		  else {this.selectedStudiesNb += 1 }


	  },

	  toggleFavorite(index,type){
		  var vm = this;
		  this.$store.dispatch('toggleFavorite',{type: type, index: index}).then(res => {
			  if (res) vm.$snotify.success(type+ 'is now in favorites');
			  else vm.$snotify.error('Sorry, an error occured');		  	
		  })
		  // this.studies[index][entity]= !this.studies[index][entity];
		  // console.log(this.studies[index]);

	  },
	  handleComments(index,entity){
		  this.studies[index][entity]= !this.studies[index][entity];

	  },
	  selectAll(is_selected){

		  if (is_selected) this.selectedStudiesNb = 0;
		  else{this.selectedStudiesNb =  _.size(this.studies)}
		_.forEach(this.studies, function(study,index) {
			study.is_selected = !is_selected;


		});
		this.studies.allSelected = ! this.studies.allSelected;
	  },
	  deleteSelectedStudies(){
		 var vm = this;
		 var i;
		 for (i = this.studies.length-1; i > -1; i--) {
			 if(this.studies[i].is_selected){
 			 	vm.$store.dispatch('deleteStudy',{StudyInstanceUID:this.studies[i].StudyInstanceUID})
 				vm.$delete(vm.studies, i);
 			 }
		 }

	  },
	  downloadSelectedStudies(){
		   var vm = this;
		   _.forEach(this.studies, function(study,index) {
			   if( study.is_selected){
				   console.log(study);
				   vm.$store.dispatch('downloadStudy',{StudyInstanceUID:study.StudyInstanceUID})
			   }
		   });

	  },
	  searchOnline(filters){
		  this.$store.dispatch('getStudies',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})
	  }


  },
  created () {

	  this.$store.dispatch('getStudies',{pageNb: this.pageNb,filters: this.filters,sortBy: this.sortBy, sortDesc: this.sortDesc,limit: this.limit})

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
		},
		showFilters: {
			handler: function(showFilters){
				if (!showFilters){
					this.filters = {
						PatientName: '',
						PatientID: '',
						AccessionNumber: '',
						StudyDate: '',
						ModalitiesInStudy: '',
					}
				}
			}
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
 
 .td_checkbox {
	 width: 150px;
 }
</style>

/* eslint-disable */