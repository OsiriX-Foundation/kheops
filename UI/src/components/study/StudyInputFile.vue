<template>
  <input
    id="file"
    ref="inputfiles"
    type="file"
    name="file"
    class="inputfile"
    multiple
    :disabled="disabled"
    @change="inputLoadFiles"
  >
</template>
<script>
export default {
  name: 'StudyInputFile',
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false,
    },
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
