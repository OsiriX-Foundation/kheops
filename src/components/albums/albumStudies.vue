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
    <manage-import-send
      :scope="scope"
      :album="album"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { AlbumRedirect } from '../../mixins/redirect.js'
import ManageImportSend from '@/components/study/ManageImportSend'

export default {
	name: 'AlbumStudies',
	components: { ManageImportSend },
	mixins: [ AlbumRedirect ],
	data () {
		return {
			scope: 'album'
		}
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
