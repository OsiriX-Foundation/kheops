/* eslint-disable */

<i18n scoped>
{
	"en": {
		"albumName": "Album Name",
		"albumDescription": "Album Description",
		"users": "Users",
		"addUser": "Add user",
		"addSeries": "Add studies / series",
		"downloadSeries": "Show download button",
		"sendSeries": "Get studies / series",
		"deleteSeries": "Remove studies / series",
		"writeComments": "Write comments"

	},
	"fr": {
		"albumName": "Nom de l'album",
		"albumDescription": "Description de l'album",
		"users": "Utilisateurs",
		"addUser": "Ajouter un utilisateur",
		"addSeries": "Ajouter une étude / série",
		"downloadSeries": "Télécharger une étude / série",
		"sendSeries": "Ajouter à un album / inbox",
		"deleteSeries": "Supprimer une étude / série",
		"writeComments": "Commenter"
	}
}
</i18n>

<template>
  <div>
    <div
      v-if="loading && album !== {}"
      class="container"
    >
      <p class="text-center fade">
        loading...
      </p>
    </div>
    <div
      v-if="!loading"
      class="container"
    >
      <div class="row">
        <div class="col-12 col-sm-12 col-md-12 col-lg-6">
          <h3>
            <v-icon
              name="book"
              scale="2"
            />
            <span class="p-2">
              {{ album.name }}
            </span>
            <v-icon
              v-if="album.is_favorite"
              name="star"
              scale="2"
            />
          </h3>
        </div>
        <div class="col-12 col-sm-12 col-md-12 col-lg-6 mb-3">
          <nav class="nav nav-pills nav-fill flex-column flex-lg-row text-center justify-content-lg-end">
            <a
              class="nav-link"
              :class="(view === 'studies' || view === '')?'active':''"
              @click.stop="view='studies'"
            >
              Studies
            </a>
            <a
              class="nav-link"
              :class="(view  === 'comments')?'active':''"
              @click.stop="view='comments'"
            >
              Comments
            </a>
            <a
              class="nav-link"
              :class="(view  === 'settings')?'active':''"
              @click.stop="view='settings'"
            >
              Settings
            </a>
          </nav>
        </div>
        <!-- <div class = 'col-md'></div> -->
      </div>
    </div>
    <!--
			https://fr.vuejs.org/v2/guide/components-dynamic-async.html
    -->
    <span v-if="view === 'studies' || view === '' && loading === false">
        <component-import-study
            :album="album"
        />
    </span>
    <album-comments
      v-if="view=='comments' && loading === false"
      :id="album.album_id"
    />
    <album-settings v-if="view=='settings' && loading === false" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import albumComments from '@/components/albums/albumComments'
import albumSettings from '@/components/albums/albumSettings'
import ComponentImportStudy from '@/components/study/ComponentImportStudy'

export default {
	name: 'Album',
	components: { ComponentImportStudy, albumSettings, albumComments },
	data () {
		return {
			view: '',
			newUserName: '',
			loading: false
		}
	},
	computed: {
		...mapGetters({
			album: 'albumTest'
		})
	},
	watch: {
		view () {
			let queryParams = { view: this.view }
			if (this.$route.query.cat !== undefined) queryParams.cat = this.$route.query.cat
			this.$router.push({ query: queryParams })
            
			this.loadAlbum()
		}
	},
	created () {
        this.loadAlbum()
	},
	methods: {
		loadAlbum () {
			this.loading = true
			this.$store.dispatch('getAlbumTest', { album_id: this.$route.params.album_id }).then((res) => {
				this.loading = false
				this.view = this.$route.query.view !== undefined ? this.$route.query.view : ''
			})
		}
	}
}
</script>

<style scoped>
h3 {
	margin-bottom: 40px;
	float: left;
}

h5.user{
	float: left;
	margin-right: 10px;
}

.icon{
	margin-left: 10px;
}
.pointer{
	cursor: pointer;
}
label{
	margin-left: 10px;
}
a.nav-link{
	cursor: pointer;
}
</style>
