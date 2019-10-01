<template>
  <div>
    <component-import-study
      :permissions="permissions"
    />
  </div>
</template>

<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy';
import List from '@/components/inbox/List';

export default {
  name: 'Inbox',
  components: { ComponentImportStudy, List },
  props: {
  },
  data() {
    return {
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
  },
  mounted() {
  },
  methods: {
  },
};

</script>
