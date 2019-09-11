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
  <div class="container albumSettingsToken">
    <tokens
      scope="album"
      :albumid="album.album_id"
    />
  </div>
</template>

<script>
import tokens from '@/components/tokens/tokens'
export default {
	name: 'AlbumSettingsToken',
	components: { tokens },
	props: {
		album: {
			type: Object,
			required: true,
			default: () => {}
		}
	}
}

</script>

<style scoped>

</style>

