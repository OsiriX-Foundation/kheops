<i18n>

{
	"en": {
		"modality": "Modality",
		"numberimages": "Number of images",
		"description": "Description",
		"seriesdate": "Series date",
		"seriestime": "Series time",
		"openviewer": "Open viewer",
		"applicationentity": "Application entity"
	},
	"fr": {
		"modality": "Modalité",
		"numberimages": "Nombre d'images",
		"description": "Description",
		"seriesdate": "Date de la série",
		"seriestime": "Heure de la série",
		"openviewer": "Ouvrir une visionneuse",
		"applicationentity": "Application entity"
	}
}

</i18n>


<template>
	<div class = 'seriesSummaryContainer'>
		<div class = 'card-title'>
			<b-form-checkbox v-model = "isSelected" v-if='series.SeriesDescription'>{{series.SeriesDescription[0]}}</b-form-checkbox>
			<b-form-checkbox v-model = "isSelected" v-else>No description</b-form-checkbox>
		</div>
		<div class = 'row'>
			<div class = 'preview'>
				<img :src='previewImg()' width= '250' height = '250'>
			</div>
			<div class = 'col description'>
				
				<table class="table table-striped col-md-8">
					<tbody>
						<tr v-if="series.Modality">
							<th>{{ $t('modality') }}</th>
							<td>{{ series.Modality[0] }}</td>
						</tr>
						<tr v-if="series.RetrieveAETitle">
							<th>{{ $t('applicationentity') }}</th>
							<td>{{ series.RetrieveAETitle[0] }}</td>
						</tr>
						<tr v-if="series.NumberOfSeriesRelatedInstances">
							<th>{{ $t('numberimages') }}</th>
							<td>{{ series.NumberOfSeriesRelatedInstances[0] }}</td>
						</tr>
						<tr v-if="series.SeriesDescription">
							<th>{{ $t('description') }}</th>
							<td>{{ series.SeriesDescription[0] }}</td>
						</tr>
						<tr v-if="series.SeriesDate">
							<th>{{ $t('seriesdate') }}</th>
							<td>{{ series.SeriesDate[0]|formatDate }}</td>
						</tr>
						<tr v-if="series.SeriesTime">
							<th>{{ $t('seriestime') }}</th>
							<td>{{ series.SeriesTime[0]|formatTime }}</td>
						</tr>
					</tbody>
				</table>

				<dl class = 'row' v-if='series.Modality'>
					<dt class = 'col-md-4 col-sm-12 text-right'></dt>
					<dd class = 'col-md-8 col-sm-12'><button type = 'button' class = 'btn-primary btn-sm' @click='openViewer'>{{$t('openviewer')}}</button></dd>
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
			window.open('https://ohif.kheops.online/?url=https://test.kheops.online/studies/'+this.StudyInstanceUID+'/ohifmetadata#token='+this.user.jwt,'OHIFViewer')
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