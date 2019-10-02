<template>
  <div>
    <component-import-study
      :permissions="permissions"
    />
  </div>
</template>

<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'Inbox',
  components: { ComponentImportStudy },
  props: {
  },
  data() {
    return {
      scope : []
    };
  },
  computed: {
    logged() {
      return this.$keycloak.authenticated;
    },
    permissions() {
      return {
        add_series: this.scope.includes('write'),
        delete_series: this.scope.includes('write'),
        download_series: this.scope.includes('downloadbutton'),
        send_series: false,
        write_comments: false,
        add_inbox: false,
      }
    },
  },
  watch: {
  },
  created() {
    const [, , token] = window.location.pathname.split('/');
    const params = {
      queries: {
        token,
        // token: '87EM86qCVvG7gICDTUoJVA',
      }
    }
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getFormData(params.queries);
    }
    HTTP.post('token/introspect', queries).then((res) => {
      this.scope = res.data.scope.split(' ')
    }).catch((err) => {
      console.log(err)
    })
  },
  mounted() {
  },
  methods: {
  },
};

</script>
