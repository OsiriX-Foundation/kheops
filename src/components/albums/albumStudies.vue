/* eslint-disable */

<i18n>
{
}
</i18n>

<template>
	<div>
		<div class = 'container'>
			<div class = 'card'>
				<div class = 'card-body'>
					<p><strong>Album description:</strong> <span v-html='$options.filters.nl2br(album.description)'></span></p>
				</div>
			</div>
		</div>
		<studies></studies>
	</div>
</template>

<script>
import { mapGetters } from 'vuex'
import Studies from '@/components/inbox/List.vue'

export default {
	name: 'album_studies',
	components: { Studies },
	data () {
		return {

		}
	},
	computed: {
		...mapGetters({
			album: 'album'
		})
	}
}

</script>

<style scoped>

</style>
