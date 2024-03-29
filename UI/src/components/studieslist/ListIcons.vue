<template>
  <span>
    <span
      :class="classIconPN(showIcons)"
      class="ml-1"
    >
      <a
        v-if="showDownloadIcon"
        href="#"
        @click.stop="getURLDownload()"
      >
        <v-icon
          class="align-middle icon-margin kheopsicon"
          name="download"
          :title="$t('download')"
        />
      </a>
      <span
        v-if="OS.match(/(Mac|iPhone|iPod|iPad)/i) && showViewerIcon"
        class="ml-1"
        :title="$t('viewer.osirix')"
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
        :title="$t('viewer.weasis')"
        @click.stop="openViewer('Weasis')"
      >
        <weasis-icon
          width="24"
          height="24"
        />
      </span>
      <span
        v-if="showSlicerIcon"
        class="ml-1"
        :title="$t('viewer.slicer')"
        @click.stop="openViewer('Slicer')"
      >
        <slicer-icon
          width="24"
          height="24"
        />
      </span>
      <span
        v-if="showViewerIcon"
        class="ml-1"
        :title="$t('viewer.ohif')"
        @click.stop="openViewer('default')"
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
        :title="$t('study.import')"
        @click="setStudyUID()"
      >
        <v-icon
          name="add"
          width="24px"
          height="24px"
          class="kheopsicon"
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
        :class="study.flag.is_commented ? 'bg-neutral fill-neutral' : ''"
        :title="$t('study.opencomments')"
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
        :class="study.flag.is_favorite ? 'bg-neutral fill-neutral' : ''"
        :title="$t('favorite')"
      />
    </span>
  </span>
</template>
<script>
import { mapGetters } from 'vuex';
import OsirixIcon from '@/components/kheopsSVG/OsirixIcon.vue';
import SlicerIcon from '@/components/kheopsSVG/SlicerIcon.vue';
import WeasisIcon from '@/components/kheopsSVG/WeasisIcon.vue';
import VisibilityIcon from '@/components/kheopsSVG/VisibilityIcon.vue';
import { ViewerToken } from '@/mixins/tokens.js';
import { Viewer } from '@/mixins/viewer.js';
import { CurrentUser } from '../../mixins/currentuser.js';

export default {
  name: 'ListIcons',
  components: {
    OsirixIcon, VisibilityIcon, WeasisIcon, SlicerIcon,
  },
  mixins: [ViewerToken, CurrentUser, Viewer],
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
    showSlicerIcon: {
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
    ...mapGetters('oidcStore', [
      'oidcIsAuthenticated',
      'oidcAccessToken',
    ]),
    ...mapGetters({
      album: 'album',
    }),
    OS() {
      return navigator.platform;
    },
    access_token() {
      if (this.oidcIsAuthenticated) {
        return this.oidcAccessToken;
      }
      if (window.location.pathname.includes('view')) {
        const [, , token] = window.location.pathname.split('/');
        return token;
      }
      return '';
    },
    showIcons() {
      // eslint-disable-next-line
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
      const token = this.getCurrentuserAccessToken(this.oidcIsAuthenticated);
      this.getViewerToken(token, StudyInstanceUID, this.source).then((res) => {
        const queryparams = `accept=application%2Fzip${sourceQuery !== '' ? '&' : ''}${sourceQuery}`;
        const URL = `${process.env.VUE_APP_URL_API}/link/${res.data.access_token}/studies/${StudyInstanceUID}?${queryparams}`;
        window.location.href = URL;
      }).catch(() => {
      });
    },
    openViewer(viewer) {
      const StudyInstanceUID = this.study.StudyInstanceUID.Value[0];
      const sourceQuery = this.getSourceQueries();
      const openWindow = this.setWindowsProps(viewer, StudyInstanceUID);

      const token = this.getCurrentuserAccessToken(this.oidcIsAuthenticated);
      const scope = this.setScope(this.source.key, this.album);
      this.getViewerToken(token, StudyInstanceUID, this.source, scope).then((res) => {
        const viewerToken = res.data.access_token;
        let url = '';
        if (viewer === 'Osirix') {
          url = this.openOsiriX(StudyInstanceUID, viewerToken);
          window.open(url, '_self');
        } else if (viewer === 'Weasis') {
          url = this.openWeasis(StudyInstanceUID, viewerToken);
          window.open(url, '_self');
        } else if (viewer === 'Slicer') {
          url = this.openSlicer(StudyInstanceUID, viewerToken);
          window.open(url, '_self');
        } else if (viewer === 'default' && openWindow.ohif !== undefined) {
          const queryparams = {
            url: `${process.env.VUE_APP_URL_API}/link/${viewerToken}/ohifservermetadata`,
            studyInstanceUIDs: StudyInstanceUID,
          };
          url = this.openOhif(queryparams);
          openWindow.ohif.location.href = url;
        } else if (viewer === 'default' && openWindow.wsi !== undefined) {
          openWindow.wsi.location.href = this.openWSI(StudyInstanceUID, viewerToken, sourceQuery);
        }
      }).catch(() => {
      });
    },
    setWindowsProps(viewer, StudyInstanceUID) {
      const openWSI = this.mustOpenWSI();
      const openWindow = {};
      if (viewer === 'default' && openWSI === false) {
        openWindow.ohif = window.open('', `OHIFViewer-${StudyInstanceUID}`);
      } else if (viewer === 'default' && openWSI === true) {
        openWindow.wsi = window.open('', `WSIViewer-${StudyInstanceUID}`);
      }
      return openWindow;
    },
    mustOpenWSI() {
      return (this.study.ModalitiesInStudy !== undefined
        && this.study.ModalitiesInStudy.Value.length === 1
        && this.study.ModalitiesInStudy.Value[0] === 'SM'
        && process.env.VUE_APP_URL_VIEWER_SM !== undefined
        && process.env.VUE_APP_URL_VIEWER_SM.length > 0);
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
        value: true,
      });
    },
    setStudyUID() {
      const StudyInstanceUID = this.study.StudyInstanceUID.Value[0];
      this.$store.dispatch('setStudyUIDtoSend', { studyUID: StudyInstanceUID });
    },
  },
};
</script>
