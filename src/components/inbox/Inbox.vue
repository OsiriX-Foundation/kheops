<template>
  <div>
    <component-import-study
      :permissions="permissions"
    >
      <template
        slot="dropzone-content"
      >
        <manage-list
          :permissions="permissions"
        />
      </template>
    </component-import-study>
  </div>
</template>

<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy';
import ManageList from '@/components/studieslist/ManageList';

export default {
  name: 'Inbox',
  components: { ComponentImportStudy, ManageList, },
  props: {
  },
  data() {
    return {
      source: {
        key: 'inbox',
        value: true,
      },
    };
  },
  computed: {
    permissions() {
      let canUpload = true;
      if (process.env.VUE_APP_DISABLE_UPLOAD !== undefined) {
        canUpload = !process.env.VUE_APP_DISABLE_UPLOAD.includes('true');
      }
      return {
        add_series: true,
        delete_series: true,
        download_series: true,
        send_series: true,
        write_comments: true,
        add_inbox: false,
        can_upload: canUpload,
      }
    }
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
