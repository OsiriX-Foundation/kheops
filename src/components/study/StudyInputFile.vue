<template>
  <input
    id="file"
    ref="inputfiles"
    type="file"
    name="file"
    class="inputfile"
    multiple
    :disabled="sendingFiles"
    @change="inputLoadFiles"
  >
</template>
<script>
import { mapGetters } from 'vuex';

export default {
  name: 'StudyInputFile',
  computed: {
    ...mapGetters({
      sendingFiles: 'sending',
    }),
  },
  methods: {
    inputLoadFiles() {
      if (this.$refs.inputfiles.files.length > 0) {
        const filesFromInput = this.$refs.inputfiles.files;
        this.$emit('loadfiles', filesFromInput);
      }
    },
  },
};
</script>
