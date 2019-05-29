<template>
  <div
    class="container"
  >
    <providers
      :album-i-d="album.album_id"
      :write-permission="album.is_admin"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Providers from '@/components/providers/Providers'
import { AlbumRedirect } from '@/mixins/redirect.js'

export default {
	name: 'AlbumSettingsReportProvider',
	components: { Providers },
	mixins: [ AlbumRedirect ],
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
		this.getAlbum()
	}
}
</script>
