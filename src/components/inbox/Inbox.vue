<template>
  <div>
    <component-import-study
      :permissions="permissions"
    />
    <!--
      <template
        slot="dropzone-content"
      >
        <manage-list
          :permissions="permissions"
        />
      </template>
    </component-import-study>
    -->
  </div>
</template>

<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy';
import ManageList from '@/components/studieslist/ManageList';

export default {
  name: 'Inbox',
  components: { ComponentImportStudy, ManageList },
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
