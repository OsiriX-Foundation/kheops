/* eslint-disable */
<i18n>

{
	"en": {
		"modality": "Modality",
		"numberimages": "Number of images",
		"description": "Description",
		"seriesdate": "Series date",
		"seriestime": "Series time",
		"openviewer": "Open viewer"
	},
	"fr": {
		"modality": "Modalité",
		"numberimages": "Nombre d'images",
		"description": "Description",
		"seriesdate": "Date de la série",
		"seriestime": "Heure de la série",
		"openviewer": "Ouvrir un explorateur"
	}
}

</i18n>


<template>
	<div class = 'seriesSummaryContainer'>
		<div class = 'card-title'>
			<b-form-checkbox v-model = "isSelected" >{{series.RetrieveAETitle[0]}}</b-form-checkbox>
		</div>
		<div class = 'row'>
			<div class = 'preview'>
				<img :src='previewImg()' width= '250' height = '250'>
			</div>
			<div class = 'col description'>
				<dl class = 'row'>
					<dt v-if='series.Modality' class = 'col-md-4 col-sm-12 text-right'>{{ $t('modality') }}</dt>
					<dd v-if='series.Modality' class = 'col-md-8 col-sm-12'>{{series.Modality[0]}}</dd>
					<dt v-if='series.NumberOfSeriesRelatedInstances' class = 'col-md-4 col-sm-12 text-right'>{{ $t("numberimages") }}</dt>
					<dd v-if='series.NumberOfSeriesRelatedInstances' class = 'col-md-8 col-sm-12'>{{series.NumberOfSeriesRelatedInstances[0]}}</dd>
					<dt v-if='series.SeriesDescription' class = 'col-md-4 col-sm-12 text-right'>{{ $t("description") }}</dt>
					<dd v-if='series.SeriesDescription' class = 'col-md-8 col-sm-12'>{{series.SeriesDescription[0]}}</dd>
					<dt v-if='series.SeriesDate' class = 'col-md-4 col-sm-12 text-right'>{{ $t("seriesdate") }}</dt>
					<dd v-if='series.SeriesDate' class = 'col-md-8 col-sm-12'>{{series.SeriesDate[0]|formatDate}}</dd>
					<dt v-if='series.SeriesTime' class = 'col-md-4 col-sm-12	text-right'>{{ $t("seriestime") }}</dt>
					<dd v-if='series.SeriesTime' class = 'col-md-8 col-sm-12'>{{series.SeriesTime[0]|formatTime}}</dd>
				</dl>
				<p class = 'text-center'><button type = 'button' class = 'btn btn-sm' @click='openViewer'>{{$t('openviewer')}}</button></p>
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
  	  	  studies: 'studies',
		  user: 'currentUser'
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
		},
		openViewer () {
			let url = 'https://test.kheops.online/studies/'+this.StudyInstanceUID+'/ohifmetadata#token='+this.user.jwt;
			window.open('https://ohif.kheops.online/?url='+encodeURI(url),'OHIFViewer')
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
div.seriesSummaryContainer{
	font-size: 80%;
	line-height: 1.5em;
}
label{
	font-size: 130%;
}

</style>