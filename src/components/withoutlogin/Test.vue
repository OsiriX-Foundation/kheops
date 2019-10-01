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
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'Inbox',
  components: { ComponentImportStudy, List },
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
        add_series: this.logged && this.scope.includes('write'),
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
      console.log(res.data)
      console.log(res.data.scope.split(' '))
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
