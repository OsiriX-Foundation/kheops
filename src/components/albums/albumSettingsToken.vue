/* eslint-disable */

<i18n>
{
	"en": {
		"view": "View"
	},
	"fr": {
		"view": "Vue"
	}
}
</i18n>

<template>
	<div class = 'container'>
		<h2>Token {{ $t("view") }}</h2>
		<p class = 'text-center'><i>...TO DO ...</i></p>
	</div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
	name: 'album_settings_token',
	data () {
		return {
		}
	},
	computed: {
		...mapGetters({
			album: 'album'
		})	
  	},
	created () {
	},
	watch: {
	}
}
</script>


<style>

</style>