<i18n>
{
  "en": {
    "download": "Download",
    "osirix": "Open OsiriX",
    "ohif": "Open OHIF",
    "weasis": "Open Weasis",
    "import": "Import data",
    "comments": "Open comments",
    "favorite": "Favorite"
  },
  "fr": {
    "download": "Télécharger",
    "osirix": "Ouvrir OsiriX",
    "ohif": "Ouvrir OHIF",
    "weasis": "Ouvrir Weasis",
    "import": "Importer des données",
    "comments": "Ouvrir les commentaires",
    "favorite": "Favori"
  }
}

</i18n>

<template>
  <span>
    <span
      :class="classIconPN(showIcons)"
      class="ml-1"
    >
      <a
        v-if="showDownloadIcon"
        href="#"
        class="kheopsicon"
        @click.stop="getURLDownload()"
      >
        <v-icon
          class="align-middle icon-margin"
          name="download"
          :title="$t('download')"
        />
      </a>
      <span
        v-if="OS.match(/(Mac|iPhone|iPod|iPad)/i) && showViewerIcon"
        class="ml-1"
        :title="$t('osirix')"
        @click.stop="openViewer('Osirix')"
      >
        <osirix-icon
          width="22px"
          height="22px"
        />
      </span>
      <span
        v-if="showWeasisIcon"
        class="ml-1"
        :title="$t('weasis')"
        @click.stop="openViewer('Weasis')"
      >
        <weasis-icon />
      </span>
      <span
        v-if="showViewerIcon"
        class="ml-1"
        :title="$t('ohif')"
        @click.stop="openViewer('Ohif')"
      >
        <visibility-icon
          width="24px"
          height="24px"
        />
      </span>
      <label
        v-if="showImportIcon"
        for="file"
        class="ml-1 pointer display-inline"
        :title="$t('import')"
        @click="setStudyUID()"
      >
        <add-icon
          width="24px"
          height="24px"
        />
      </label>
      <span
        v-if="showReportProviderIcon"
      >
        <slot name="reportprovider" />
      </span>
    </span>
    <span
      v-if="showCommentIcon"
      :class="study.flag.is_commented ? '' : classIconPN(showIcons)"
      class="ml-1"
      @click.stop="showComments(study, 'comments')"
    >
      <v-icon
        class="align-middle icon-margin kheopsicon"
        name="comment-dots"
        :class="study.flag.is_commented ? 'bg-neutral' : ''"
        :title="$t('comments')"
      />
    </span>
    <span
      v-if="showFavoriteIcon"
      :class="study.flag.is_favorite ? '' : classIconPN(showIcons)"
      class="ml-1"
      @click.stop="toggleFavorite()"
    >
      <v-icon
        class="align-middle icon-margin kheopsicon"
        name="star"
        :class="study.flag.is_favorite ? 'bg-neutral' : ''"
        :title="$t('favorite')"
      />
    </span>
  </span>
</template>
<script>
import Vue from 'vue';
import OsirixIcon from '@/components/kheopsSVG/OsirixIcon.vue';
import WeasisIcon from '@/components/kheopsSVG/WeasisIcon.vue';
import VisibilityIcon from '@/components/kheopsSVG/VisibilityIcon.vue';
import AddIcon from '@/components/kheopsSVG/AddIcon';
import { ViewerToken } from '@/mixins/tokens.js';
import { CurrentUser } from '../../mixins/currentuser.js';

export default {
  name: 'ListIcons',
  components: {
    OsirixIcon, VisibilityIcon, AddIcon, WeasisIcon,
  },
  mixins: [ViewerToken, CurrentUser],
  props: {
    study: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    mobiledetect: {
      type: Boolean,
      required: true,
      default: false,
    },
    albumId: {
      type: String,
      required: true,
      default: '',
    },
    showFavoriteIcon: {
      type: Boolean,
      required: false,
      default: true,
    },
    showCommentIcon: {
      type: Boolean,
      required: false,
      default: true,
    },
    showDownloadIcon: {
      type: Boolean,
      required: false,
      default: true,
    },
    showViewerIcon: {
      type: Boolean,
      required: false,
      default: true,
    },
    showImportIcon: {
      type: Boolean,
      required: false,
      default: true,
    },
    showReportProviderIcon: {
      type: Boolean,
      required: false,
      default: true,
    },
    showWeasisIcon: {
      type: Boolean,
      required: false,
      default: true,
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {
    };
  },
  computed: {
    OS() {
      return navigator.platform;
    },
    access_token() {
      if (this.$keycloak.authenticated) {
        return Vue.prototype.$keycloak.token;
      }
      if (window.location.pathname.includes('view')) {
        const [, , token] = window.location.pathname.split('/');
        return token;
      }
      return '';
    },
    showIcons() {
      return (this.study.flag.is_hover || this.study._showDetails || this.study.showIcons);
    },
  },

  watch: {
    studies: {
      handler(studies) {
        if (studies.length > 0) {
          this.UI.loading = false;
        }
      },
    },
  },

  created() {
  },
  mounted() {
  },
  methods: {
    getSourceQueries() {
      if (Object.keys(this.source).length > 0) {
        return `${encodeURIComponent(this.source.key)}=${encodeURIComponent(this.source.value)}`;
      }
      return '';
    },
    classIconPN(visibility) {
      if (visibility || this.mobiledetect) {
        return 'iconsHover';
      }
      return 'iconsUnhover';
    },
    toggleFavorite() {
      const params = {
        StudyInstanceUID: this.study.StudyInstanceUID.Value[0],
        value: !this.study.flag.is_favorite,
      };
      if (Object.keys(this.source).length > 0) {
        params.queries = {};
        params.queries[this.source.key] = this.source.value;
      }
      this.$store.dispatch('favoriteStudy', params);
    },
    getURLDownload() {
      const sourceQuery = this.getSourceQueries();
      const StudyInstanceUID = this.study.StudyInstanceUID.Value[0];
      this.getViewerToken(this.currentuserAccessToken, StudyInstanceUID, this.source).then((res) => {
        const queryparams = `accept=application%2Fzip${sourceQuery !== '' ? '&' : ''}${sourceQuery}`;
        const URL = `${process.env.VUE_APP_URL_API}/link/${res.data.access_token}/studies/${StudyInstanceUID}?${queryparams}`;
        location.href = URL;
      }).catch((err) => {
        console.log(err);
      });
    },
    openViewer(viewer) {
      const StudyInstanceUID = this.study.StudyInstanceUID.Value[0];
      const sourceQuery = this.getSourceQueries();
      let ohifWindow;
      if (viewer === 'Ohif') {
        ohifWindow = window.open('', `OHIFViewer-${StudyInstanceUID}`);
      }
      this.getViewerToken(this.currentuserAccessToken, StudyInstanceUID, this.source).then((res) => {
        if (viewer === 'Osirix') {
          this.openOsiriX(StudyInstanceUID, res.data.access_token);
        } else if (viewer === 'Ohif') {
          ohifWindow.location.href = this.openOhif(StudyInstanceUID, res.data.access_token, sourceQuery);
        } else if (viewer === 'Weasis') {
          this.openWeasis(StudyInstanceUID, res.data.access_token);
        }
      }).catch((err) => {
        console.log(err);
      });
    },
    openOsiriX(StudyInstanceUID, token) {
      const url = `${process.env.VUE_APP_URL_API}/link/${token}/studies/${StudyInstanceUID}?accept=application/zip`;
      window.open(`osirix://?methodName=downloadURL&URL='${encodeURIComponent(url)}'`, '_self');
    },
    openWeasis(StudyInstanceUID, token) {
      const url = `$dicom:rs --url="${process.env.VUE_APP_URL_API}" --request="studyUID=${StudyInstanceUID}" --header="Authorization: Bearer ${token}"`;
      window.open(`weasis://?${encodeURIComponent(url)}`, '_self');
    },
    openOhif(StudyInstanceUID, token, queryparams) {
      const url = `${process.env.VUE_APP_URL_API}/link/${token}/studies/${StudyInstanceUID}/ohifmetadata${queryparams !== '' ? '?' : ''}${queryparams}`;
      return `${process.env.VUE_APP_URL_VIEWER}/viewer/?url=${encodeURIComponent(url)}`;
    },
    showComments(study, flagView) {
      const params = {
        StudyInstanceUID: study.StudyInstanceUID.Value[0],
        flag: 'view',
        value: flagView,
      };
      this.$store.dispatch('setFlagByStudyUID', params);
      this.$store.dispatch('setShowDetails', {
        StudyInstanceUID: study.StudyInstanceUID.Value[0],
        value: !study._showDetails,
      });
    },
    setStudyUID() {
      const StudyInstanceUID = this.study.StudyInstanceUID.Value[0];
      this.$store.dispatch('setStudyUIDtoSend', { studyUID: StudyInstanceUID });
    },
  },
};
</script>
