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
  <div class="container">
    <h2>Token {{ $t("view") }}</h2>
    <p class="text-center">
      <i>...TO DO ...</i>
    </p>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
	name: 'AlbumSettingsToken',
	data () {
		return {
		}
	},
	computed: {
		...mapGetters({
			album: 'album'
		})
	},
	watch: {
	},
	created () {
	}
}

</script>

<style scoped>

</style>

