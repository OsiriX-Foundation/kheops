<i18n>
{
  "en": {
    "invalidtoken": "This link is no longer active"
  },
  "fr": {
    "invalidtoken": "Ce lien n'est plus actif"
  }
}
</i18n>
<template>
  <div>
    <component-import-study
      v-if="active === true"
      :permissions="permissions"
      :album-i-d="albumID"
    />
    <div
      v-if="active === false"
    >
      <b-card class="text-center">
        <div class="bg-secondary font-large">
          {{ $t('invalidtoken') }}
        </div>
      </b-card>
    </div>
  </div>
</template>

<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy';
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'ViewWithoutLogin',
  components: { ComponentImportStudy },
  props: {
  },
  data() {
    return {
      scope: [],
      active: -1,
      albumID: '',
      album: {},
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
        send_series: this.scope.includes('send') && this.logged,
        write_comments: false,
        add_inbox: this.scope.includes('send') && this.logged,
      };
    },
  },
  watch: {
  },
  created() {
    this.$store.dispatch('setSource', {});
    const params = {
      queries: {
        token: this.$route.params.token,
      },
    };
    let queries = '';
    if (params.queries !== undefined) {
      queries = httpoperations.getFormData(params.queries);
    }
    HTTP.post('token/introspect', queries).then((res) => {
      const { active } = res.data;
      if (active === true) {
        this.scope = res.data.scope.split(' ');
        this.albumID = res.data.album_id;
        if (this.albumID === undefined) {
          this.active = active;
        } else {
          this.getAlbum(this.albumID).then((resAlbum) => {
            this.active = active;
            this.album = resAlbum.data;
          });
        }
      } else {
        this.active = active;
      }
    }).catch((err) => {
      console.log(err);
    });
  },
  mounted() {
  },
  methods: {
    getAlbum(albumID) {
      return this.$store.dispatch('getAlbum', { album_id: albumID }).then((res) => res)
        .catch((err) => {
          this.$router.push('/albums');
          Promise.reject(err);
        });
    },
  },
};

</script>
