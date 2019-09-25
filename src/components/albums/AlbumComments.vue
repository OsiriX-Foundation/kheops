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
    "adminrights": "data steward rights",
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
    "adminrights": "des droits gardien des données",
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
      :id="id"
      scope="album"
    />
  </div>
</template>

<script>
import commentsAndNotifications from '@/components/comments/commentsAndNotifications';

export default {
  name: 'AlbumComments',
  components: { commentsAndNotifications },
  props: {
    id: {
      type: String,
      required: true,
    },
  },
};
</script>

<style scoped>

</style>

