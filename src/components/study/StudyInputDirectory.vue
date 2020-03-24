<template>
  <input
    id="directory"
    ref="inputdir"
    type="file"
    name="file"
    class="inputfile"
    webkitdirectory
    :disabled="sendingFiles"
    @change="inputLoadDirectories"
  >
</template>
<script>
import { mapGetters } from 'vuex';

export default {
  name: 'StudyInputDirectory',
  computed: {
    ...mapGetters({
      sendingFiles: 'sending',
    }),
  },
  methods: {
    inputLoadDirectories() {
      if (this.$refs.inputdir.files.length > 0) {
        const filesFromInput = this.$refs.inputdir.files;
        this.$emit('loaddirectories', filesFromInput);
      }
    },
  },
};
</script>
