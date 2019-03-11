/* eslint-disable */

<i18n scoped>
{
	"en": {
		"albumName": "Album Name",
		"albumDescription": "Album Description",
		"users": "Users",
		"addUser": "Add User",
		"addSeries": "Add Studies / Series",
		"downloadSeries": "Download Studies / Series",
		"sendSeries": "Get Studies / Series",
		"deleteSeries": "Remove Studies / Series",
		"writeComments": "Write Comments"

	},
	"fr": {
		"albumName": "Nom de l'album",
		"albumDescription": "Description de l'album",
		"users": "Utilisateurs",
		"addUser": "Ajouter un utilisateur",
		"addSeries": "Ajouter une étude / série",
		"downloadSeries": "Télécharger une étude / série",
		"sendSeries": "Récupérer une étude / série",
		"deleteSeries": "Supprimer une étude / série",
		"writeComments": "Commenter"
	}
}
</i18n>

<template>
  <div class="container-fluid">
    <div
      v-if="loading"
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
        <div class="col-md-4 col-lg-6">
          <h3>
            <v-icon
              name="book"
              scale="2"
            />
            <span class="p-2">
              {{ album.name }}
            </span>
            <v-icon
              v-if="view=='studies' && album.is_favorite"
              name="star"
              scale="2"
            />
          </h3>
        </div>
        <div class="col-md-8 col-lg-6">
          <nav class="nav nav-pills nav-fill float-md-right">
            <a
              class="nav-link"
              :class="(view=='studies')?'active':''"
              @click.stop="view='studies'"
            >
              Studies
            </a>
            <a
              class="nav-link"
              :class="(view=='comments')?'active':''"
              @click.stop="view='comments'"
            >
              Comments
            </a>
            <a
              class="nav-link"
              :class="(view=='settings')?'active':''"
              @click.stop="view='settings'"
            >
              Settings
            </a>
          </nav>
        </div>
        <!-- <div class = 'col-md'></div> -->
      </div>
    </div>
    <album-studies v-if="view=='studies'" />
    <album-comments
      v-if="view=='comments'"
      :id="album.album_id"
    />
    <album-settings v-if="view=='settings'" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import albumStudies from '@/components/albums/albumStudies'
import albumComments from '@/components/albums/albumComments'
import albumSettings from '@/components/albums/albumSettings'

export default {
	name: 'Album',
	components: { albumStudies, albumSettings, albumComments },
	data () {
		return {
			view: 'studies',
			newUserName: '',
			loading: false
		}
	},
	computed: {
		...mapGetters({
			album: 'album'
		})
	},
	watch: {
		view () {
			let queryParams = { view: this.view }
			if (this.$route.query.cat !== undefined) queryParams.cat = this.$route.query.cat
			this.$router.push({ query: queryParams })
		},
		'$route.query' () {
			this.view = this.$route.query.view
		}
	},
	created () {
		this.loading = true
		this.$store.dispatch('getAlbum', { album_id: this.$route.params.album_id }).then(() => {
			this.loading = false
			this.view = this.$route.query.view || 'studies'
		})
	},
	methods: {
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
