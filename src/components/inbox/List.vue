/* eslint-disable */

<i18n>
{
  "en": {
	  "selectednbstudies": "{count} study is selected | {count} studies are selected",
	  "send": "Send",
	  "addalbum": "Add to an album",
	  "download": "Download",
	  "addfavorite": "Add to favorite",
	  "delete": "Delete",
    "PatientName": "Patient Name",
	"Modality": "Modality",
	"StudyDate": "Study Date",
	"AccessionNumber": "Accession #",
	"PatientID": "Patient ID",
	"filter": "Filter",
	"fromDate": "From",
	"toDate": "To"
  },
  "fr": {
	  "selectednbstudies": "{count} étude est sélectionnée | {count} études sont sélectionnées",
	  "send": "Envoyer",
	  "addalbum": "Ajouter à un album",
	  "download": "Télécharger",
	  "addfavorite": "Ajouter aux favoris",
	  "delete": "Supprimer",
    "PatientName": "Nom du patient",
	"Modality": "Modalité",
	"StudyDate": "Date de l'étude",
	"AccessionNumber": "# accession",
	"PatientID": "ID patient",
	"filter": "Filtrer",
	"fromDate": "De",
	"toDate": "A"
	
  }
}
</i18n>



<template>
	<div class = 'container-fluid'>
		<div class="my-3 selection-button-container">
			<span  :style="(selectedStudiesNb)?'':'visibility: hidden'">
				<span >{{ $tc("selectednbstudies",selectedStudiesNb,{count: selectedStudiesNb}) }}</span>
				<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="paper-plane"></v-icon></span><br>{{ $t("send") }}</button>
				<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="book"></v-icon></span><br>{{ $t("addalbum") }}</button>
				<button type="button" class="btn btn-link btn-sm text-center" @click = "downloadSelectedStudies()"><span><v-icon class="align-middle" name="download"></v-icon></span><br>{{ $t("download") }}</button>
				<button type="button" class="btn btn-link btn-sm text-center"><span><v-icon class="align-middle" name="star"></v-icon></span><br>{{ $t("addfavorite") }}</button>
				<button type="button" class="btn btn-link btn-sm text-center" @click = "deleteSelectedStudies()"><span><v-icon class="align-middle" name="trash"></v-icon></span><br>{{ $t("delete") }}</button>
			</span>
			<span style = 'margin-left: 30px;'><toggle-button v-model="filters.inbox_and_albums" :labels="{checked: 'Yes', unchecked: 'No'}" /> Include series from albums</span>
			<button type = 'button' class = "btn btn-link btn-lg float-right" @click='showFilters=!showFilters'><v-icon name = 'search' scale='2'/></button>

		</div>
		<b-table  striped :items="studies" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"  @sort-changed="sortingChanged" :no-local-sorting="true">
	
			<template slot="HEAD_is_selected" scope="head">
				<b-button variant="link" size="sm"  class="mr-2" >
					<v-icon  class="align-middle"   name="chevron-down" style = 'visibility: hidden'></v-icon>
				</b-button>
  				<b-form-checkbox @click.native.stop @change="selectAll(studies.allSelected)" v-model="studies.allSelected" name="allSelected">
  				</b-form-checkbox>
			</template>
			<template slot="HEAD_PatientName" slot-scope="data">
					<div v-if='showFilters' @click.stop='' ><input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientName' :placeholder="$t('filter')"> <br></div>
					{{$t(data.label)}}
					
			</template>
		 	<template slot="HEAD_PatientID" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.PatientID' :placeholder="$t('filter')"> <br></div>
				{{$t(data.label)}}

			</template>
		 	<template slot="HEAD_AccessionNumber" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.AccessionNumber' :placeholder="$t('filter')"> <br></div>
				{{$t(data.label)}}
				
			</template>
		 	<template slot="HEAD_StudyDate" slot-scope="data">
				<div v-if='showFilters' @click.stop='' class = 'form-row'>
					
						<div class = 'col form-inline'>
							<div class = 'form-group'>
								<datepicker v-model="filters.StudyDateFrom"  :bootstrap-styling='false' :disabledDates="disabledFromDates" input-class="form-control form-control-sm  search-calendar" :calendar-button="false" calendar-button-icon=""  wrapper-class='calendar-wrapper' :placeholder="$t('fromDate')" :clear-button="true" clear-button-icon='fa fa-times'></datepicker>
							</div>
						</div>
						 
						<div class = 'col form-inline'>
							<div class = 'form-group'>
								<datepicker v-model="filters.StudyDateTo" :bootstrap-styling='false' :disabledDates="disabledToDates"  input-class="form-control form-control-sm search-calendar" :calendar-button="false"  calendar-button-icon="" wrapper-class='calendar-wrapper' :placeholder="$t('toDate')" :clear-button="true" clear-button-icon='fa fa-times'></datepicker>
							</div>
						</div>					
					<!-- <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateFrom' placeholder="From"> - <input type = 'search' class = 'form-control form-control-sm' v-model='filters.StudyDateTo' placeholder="To"> <br> -->
				</div>
				<br v-if='showFilters' >
				{{$t(data.label)}}
				
			</template>
		 	<template slot="HEAD_ModalitiesInStudy" slot-scope="data">
				<div v-if='showFilters' @click.stop=''><input type = 'search' class = 'form-control form-control-sm' v-model='filters.ModalitiesInStudy' :placeholder="$t('filter')"><br></div>
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
					<b-form-checkbox v-model = "row.item.is_selected" @click.native.stop @change="toggleSelected(row.item,'study',!row.item.is_selected)" >
					</b-form-checkbox>
					
				</b-form-group>
				

			</template>
			<template slot="row-details" slot-scope="row">
				<b-card>
					<!-- <p>{{row.item.series.length}} serie{{row.item.series.length>1?"s":""}}</p> -->
					<div class = 'row'>
						<div class = 'col-sm-12 col-md-6' v-for='serie in row.item.series'>
							<series-summary
							      :SeriesInstanceUID="serie.SeriesInstanceUID[0]"
								  :selected="serie.is_selected"
								  :StudyInstanceUID="row.item.StudyInstanceUID[0]"
							      :key="serie.SeriesInstanceUID[0]">
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

import seriesSummary from '@/components/inbox/seriesSummary'
import ToggleButton from 'vue-js-toggle-button'
import Datepicker from 'vuejs-datepicker';
import Vue from 'vue'

Vue.use(ToggleButton)

export default {
  name: 'studies',
	data () {
		return {
			pageNb: 1,
			active: false,
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
				StudyDateFrom: '',
				StudyDateTo: '',
				ModalitiesInStudy: '',
				inbox_and_albums: false
			}
		}
	},
	components: {seriesSummary, Datepicker},
  computed: {
	  ...mapGetters({
	  	  studies: 'studies'
	    }),
		totalRows () {
			return this.studies.length;
		},
		selectedStudiesNb () {
			return _.filter(this.studies,s => {return s.is_selected === true;}).length;
		},
  	  	disabledToDates: function(){
  	  		  let vm = this;
  	  		  return {
  	  			  to: vm.filters.StudyDateFrom,
  	  			  from: new Date()
  	  		  }
  	  	},
  	  	disabledFromDates: function(){
  	  		  let vm = this;
  	  		  return {
  	  			  from: new Date()
  	  		  }
  	  	}
  },
  methods: {
	  scroll () {
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
	  toggleSelected(item,type,is_selected) {
		  let index = _.findIndex(this.studies,s => {return s.StudyInstanceUID[0] == item.StudyInstanceUID[0]})
		  var vm = this;
		  this.$store.dispatch('toggleSelected',{type: type, index: index,is_selected: is_selected}).then(res => {
		  })
	  },

	  toggleFavorite(index,type){
		  var vm = this;
		  this.$store.dispatch('toggleFavorite',{type: type, index: index}).then(res => {
			  if (res) vm.$snotify.success(type+ 'is now in favorites');
			  else vm.$snotify.error('Sorry, an error occured');		  	
		  })

	  },
	  handleComments(index,entity){
		  this.studies[index][entity]= !this.studies[index][entity];

	  },
	  selectAll(is_selected){
		this.$store.commit("SELECT_ALL_STUDIES",!is_selected);
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
						StudyDateFrom: '',
						StudyDateTo: '',
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
 
 input.search-calendar{
	 width: 100px !important;
 }
 
 div.calendar-wrapper{
	 color: #333;
 }
</style>

/* eslint-disable */