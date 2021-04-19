<template>
  <input
    id="directory"
    ref="inputdir"
    type="file"
    name="file"
    class="inputfile"
    webkitdirectory
    :disabled="disabled"
    @change="inputLoadDirectories"
  >
</template>
<script>
export default {
  name: 'StudyInputDirectory',
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false,
    },
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
