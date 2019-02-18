<i18n>
{
	"en": {
		"commentpostsuccess": "comment posted successfully",
		"imported": "imported",
		"removed": "removed",
		"thestudy": "the study",
		"theseries": "the series",
		"hasadd": "has add",
		"hasgranted": "has granted",
		"hasremoved": "has removed",
		"adminrights": "admin rights",
		"hasleft": "has left",
		"hascreated": "has created",
		"hasedited": "has edited",
		"includenotifications": "include notifications"
	},
	"fr" : {
		"commentpostsuccess": "le commentaire a été posté avec succès",
		"imported": "a importé",
		"removed": "a supprimé",
		"thestudy": "l'étude",
		"theseries": "la série",
		"hasadd": "a ajouté",
		"hasgranted": "a attribué",
		"hasremoved": "a retiré",
		"adminrights": "des droits admin",
		"hasleft": "a quitté",
		"hascreated": "a créé",
		"hasedited": "a édité",
		"includenotifications": "inclure les notifications"
	}
}
</i18n>

<template>
  <div class="container">
    <comments-and-notifications
      :id="albumId"
      scope="album"
    />
  </div>
</template>

<script>
import commentsAndNotifications from '@/components/comments/commentsAndNotifications'
export default {
	name: 'AlbumComments',
	components: { commentsAndNotifications },
	props: {
		'albumId': {
			type: String,
			required: true
		}
	},
	data () {
		return {
		}
	}
}
</script>

<style scoped>

</style>

