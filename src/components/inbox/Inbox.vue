<template>
  <div>
    <component-import-study
      :permissions="permissions"
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
        key: 'inbox',
        value: true,
      },
      permissions: {
        add_series: true,
        delete_series: true,
        download_series: true,
        send_series: true,
        write_comments: true,
        add_inbox: false,
      },
    };
  },
  computed: {
  },
  watch: {
  },
  created() {
    this.$store.dispatch('setSource', this.source);
  },
  mounted() {
  },
  methods: {
  },
};

</script>
