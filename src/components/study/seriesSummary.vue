/* eslint-disable */
<i18n>

{
	"en": {
		"modality": "Modality",
		"numberimages": "Number of images",
		"description": "Description",
		"seriesdate": "Series date",
		"seriestime": "Series time"
	},
	"fr": {
		"modality": "Modalité",
		"numberimages": "Nombre d'images",
		"description": "Description",
		"seriesdate": "Date de la série",
		"seriestime": "Heure de la série"
	}
}

</i18n>


<template>
	<div>
		<div class = 'card-title'>
			<b-form-checkbox v-model = "isSelected" >{{series.RetrieveAETitle[0]}}</b-form-checkbox>
		</div>
		<div class = 'row'>
			<div class = 'preview'>
				<img :src='previewImg()' width= '250' height = '250'>
			</div>
			<div class = 'col description'>
				<dl class = 'row'>
					<dt v-if='series.Modality' class = 'col-4 text-right'>{{ $t('modality') }}</dt>
					<dd v-if='series.Modality' class = 'col-8'>{{series.Modality[0]}}</dd>
					<dt v-if='series.NumberOfSeriesRelatedInstances' class = 'col-4 text-right'>{{ $t("numberimages") }}</dt>
					<dd v-if='series.NumberOfSeriesRelatedInstances' class = 'col-8'>{{series.NumberOfSeriesRelatedInstances[0]}}</dd>
					<dt v-if='series.SeriesDescription' class = 'col-4 text-right'>{{ $t("description") }}</dt>
					<dd v-if='series.SeriesDescription' class = 'col-8'>{{series.SeriesDescription[0]}}</dd>
					<dt v-if='series.SeriesDate' class = 'col-4 text-right'>{{ $t("seriesdate") }}</dt>
					<dd v-if='series.SeriesDate' class = 'col-8'>{{series.SeriesDate[0]|formatDate}}</dd>
					<dt v-if='series.SeriesTime' class = 'col-4	text-right'>{{ $t("seriestime") }}</dt>
					<dd v-if='series.SeriesTime' class = 'col-8'>{{series.SeriesTime[0]|formatTime}}</dd>
				</dl>
			</div>			
		</div>
	</div>
</template>

<script>
import { mapGetters } from 'vuex'
export default{
	name: "seriesSummary",
	props: ['SeriesInstanceUID','StudyInstanceUID','selected'],
	data () {
		return {
		}
	},
    computed: {
  	  ...mapGetters({
  	  	  studies: 'studies'
  	    }),
		series () {
			let studyIndex = _.findIndex(this.studies,s => {return s.StudyInstanceUID[0] == this.StudyInstanceUID});
			if (studyIndex > -1){
				let seriesIndex = _.findIndex(this.studies[studyIndex].series, d => {return d.SeriesInstanceUID[0] == this.SeriesInstanceUID})
				if (seriesIndex > -1) return this.studies[studyIndex].series[seriesIndex];
			}
			return {};
		},
	    isSelected: {
	       // getter
	       get: function () {
			   return this.selected;
		   },
		   // setter
		   set: function (newValue) {
			   let studyIndex = _.findIndex(this.studies,s => {return s.StudyInstanceUID[0] == this.StudyInstanceUID});
			   if (studyIndex > -1){
				   let seriesIndex = _.findIndex(this.studies[studyIndex].series, d => {return d.SeriesInstanceUID[0] == this.SeriesInstanceUID})
				   if (seriesIndex > -1){
					   this.$store.dispatch('toggleSelected',{type: 'series', index: studyIndex+":"+seriesIndex,selected: newValue});
				   } 
			   }
		   }
	         
	   }
   	},
	methods: {
		previewImg () {
			let imgSrcData = '';
			if (this.series.imgSrc !== undefined) return this.series.imgSrc;
			else {
				this.$store.dispatch('getImage',{SeriesInstanceUID: this.SeriesInstanceUID, StudyInstanceUID: this.StudyInstanceUID}).then(img => {
					return img.data;
				})
			}			
		}
	},
	watch: {
		series: {
			handler(val){
				console.log('changed1');				
				console.log(val.is_selected);
			},
			deep: true
		}
	}
}

</script>

<style>
div.preview{
	width: 290px;
	padding: 0 20px;
	float: left;
}

</style>