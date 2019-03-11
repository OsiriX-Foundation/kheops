/* eslint-disable */

<i18n>
{
}
</i18n>

<template>
  <div>
    <div class="container">
      <div
        v-if="formattedAlbumDescription[0] !== ''"
        class="card"
      >
        <div class="card-body">
          <p
            v-for="(p,idx) in formattedAlbumDescription"
            :key="idx"
            class="py-0 my-0"
            :class="(idx)?'pl-3':''"
          >
            {{ p }}
          </p>
        </div>
      </div>
    </div>
    <studies :album="album" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Studies from '@/components/inbox/List.vue'

export default {
	name: 'AlbumStudies',
	components: { Studies },
	data () {
		return {

		}
  },
  created () {
    this.$store.dispatch('getAlbum', { album_id: this.$route.params.album_id })
	},
	computed: {
		...mapGetters({
			album: 'album'
		}),
		formattedAlbumDescription () {
			return this.album.description.split('\n')
		}
	}
}

</script>

<style scoped>

</style>
