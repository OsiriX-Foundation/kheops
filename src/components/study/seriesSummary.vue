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
	<div class = 'card'>
		<div class = 'card-body'>
			<div class = 'card-title'>{{series.RetrieveAETitle[0]}}</div>
			<div class = 'row'>
				<div class = 'preview'>
					<img :src='testImg()' width= '250' height = '250'>
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
	</div>
</template>

<script>
export default{
	name: "seriesSummary",
	props: ['series','StudyInstanceUID'],
	data () {
		return {
			imgSrc: this.series.imgSrc+""
		}
	},
	methods: {
		testImg () {
			let imgSrcData = '';
			if (this.series.imgSrc !== undefined) return this.series.imgSrc;
			else {
				this.$store.dispatch('getImage',{SeriesInstanceUID: this.series.SeriesInstanceUID[0], StudyInstanceUID: this.StudyInstanceUID}).then(img => {
					return img.data;
				})
			}			
		}
	},
	watch: {
		imgSrc: function(newValue){
			console.log(newValue);
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