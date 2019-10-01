<template>
  <div>
    <component-import-study
      :source="source"
    />
  </div>
</template>

<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy';

export default {
  name: 'Inbox',
  components: { ComponentImportStudy },
  props: {
  },
  data() {
    return {
      source: {
        inbox: true,
      },
    };
  },
  computed: {
  },
  watch: {
  },
  created() {
  },
  mounted() {
  },
  methods: {
  },
};

</script>
