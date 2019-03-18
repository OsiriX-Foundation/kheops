export const AlbumRedirect = {
	created () {
		this.getAlbum()
	},
	methods: {
		getAlbum () {
			this.$store.dispatch('getAlbum', { album_id: this.$route.params.album_id })
				.catch(res => {
					this.$router.push('/albums')
				})
		}
	}
}
