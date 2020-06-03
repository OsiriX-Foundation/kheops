<template>
  <div class="row">
    <div class="col-lg-2 col-xl-auto mb-4">
      <study-menu
        :study="study"
      />
    </div>
    <div
      class="col-sm-12 col-md-12 col-lg-10 col-xl-10"
    >
      <list-series
        v-if="study.flag.view === 'series'"
        :study="study"
        :source="source"
      />
      <comments-and-notifications
        v-if="study.flag.view === 'comments'"
        :id="study.StudyInstanceUID.Value[0]"
        :write-comments="this.$route.name !== 'viewnologin'"
        scope="studies"
      />
      <study-metadata
        v-if="study.flag.view === 'study'"
        :id="study.StudyInstanceUID.Value[0]"
        scope="studies"
      />
    </div>
  </div>
</template>

<script>
import commentsAndNotifications from '@/components/comments/commentsAndNotifications';
import studyMetadata from '@/components/study/studyMetadata';
import StudyMenu from '@/components/studieslist/StudyMenu';
import ListSeries from '@/components/series/ListSeries';

export default {
  name: 'ListItemDetails',
  components: {
    commentsAndNotifications, studyMetadata, StudyMenu, ListSeries,
  },
  props: {
    studyUID: {
      type: String,
      required: true,
      default: '',
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  computed: {
    study() {
      return this.$store.getters.getStudyByUID(this.studyUID);
    },
  },
};
</script>
