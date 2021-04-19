<template>
  <div
    v-if="writePermission"
  >
    <div
      v-if="currentView === 'listlinks'"
    >
      <list-links
        :albumid="albumid"
      />
    </div>
  </div>
</template>

<script>
import ListLinks from '@/components/socialmedia/ListLinks';

export default {
  name: 'SocialMedias',
  components: { ListLinks },
  props: {
    scope: {
      type: String,
      required: true,
    },
    albumid: {
      type: String,
      required: false,
      default: '',
    },
    writePermission: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {};
  },
  computed: {
    currentView() {
      return this.$route.params.action !== undefined ? this.$route.params.action : 'listlinks';
    },
  },
  watch: {
  },
  created() {
  },
  methods: {
    loadView(action) {
      this.$router.push({ name: 'albumsettingsaction', params: { action } });
    },
  },
};
</script>
