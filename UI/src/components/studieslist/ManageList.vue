<template>
  <div>
    <study-input-file
      :disabled="sendingFiles && canUpload"
      @loadfiles="inputLoadFiles"
    />
    <study-input-directory
      :disabled="sendingFiles && canUpload"
      @loaddirectories="inputLoadFiles"
    />
    <list-headers
      :id="headerID"
      :studies="studies"
      :show-send-button="permissions.send_series"
      :show-album-button="permissions.send_series"
      :show-favorite-button="permissions.add_series && $route.name !== 'viewnologin'"
      :show-delete-button="permissions.delete_series"
      :show-import-button="permissions.add_series && canUpload"
      :show-inbox-button="permissions.add_inbox"
      :album-id="albumID"
      class="list-header"
      @reloadStudies="reloadStudies"
    />
    <studies-list
      v-if="topstyle !== null && firstLoading === false"
      :key="studiesListRender"
      :permissions="permissions"
      :can-upload="canUpload"
      :topstyle="topstyle"
      :sort-by="studiesParams.sortBy"
      :sort-desc="studiesParams.sortDesc"
      @sorting-changed="sortingChanged"
    />
    <infinite-loading
      ref="infiniteLoading"
      :identifier="infiniteId"
      @infinite="infiniteHandler"
    >
      <div slot="spinner">
        <loading />
      </div>
      <div slot="no-more">
        {{ $t('study.nomorestudies') }}
      </div>
      <div slot="no-results">
        <div
          v-if="mobiledetect || !canUpload || !permissions.add_series"
        >
          {{ $t('study.noresults') }}
        </div>
        <drag-and-drop-icon
          v-else
          height="400px"
          width="400px"
          :text="$t('study.dragDrop')"
        />
      </div>
      <div slot="error">
        <span
          v-if="statusList === 401 || statusList === 403"
        >
          {{ $t('study.nopermissions') }}
        </span>
        <span
          v-else
        >
          {{ $t('study.error') }} <br> <br>
          <button
            type="button"
            class=" btn btn-md"
            @click="searchStudies()"
          >
            {{ $t('reload') }}
          </button>
        </span>
      </div>
    </infinite-loading>
  </div>
</template>
<script>
import { mapGetters } from 'vuex';
import moment from 'moment';
import InfiniteLoading from 'vue-infinite-loading';
import Loading from '@/components/globalloading/Loading';
import StudiesList from '@/components/studieslist/StudiesList';
import ListHeaders from '@/components/studieslist/ListHeaders';
import StudyInputFile from '@/components/study/StudyInputFile';
import StudyInputDirectory from '@/components/study/StudyInputDirectory';
import { CurrentUser } from '@/mixins/currentuser.js';
import DragAndDropIcon from '@/components/kheopsSVG/DragAndDropIcon.vue';
import mobiledetect from '@/mixins/mobiledetect.js';

export default {
  name: 'ManageList',
  components: {
    ListHeaders, InfiniteLoading, Loading, StudiesList, StudyInputFile, StudyInputDirectory, DragAndDropIcon,
  },
  mixins: [CurrentUser],
  props: {
    permissions: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    albumID: {
      type: String,
      required: false,
      default: undefined,
    },
  },
  data() {
    return {
      infiniteId: 0,
      studiesListRender: 0,
      statusList: 200,
      firstLoading: true,
      studiesParams: {
        offset: 0,
        limit: 50,
        sortDesc: true,
        sortBy: 'StudyDate',
      },
      headerID: 'listheaders',
      topstyle: null,
      defaulttop: 65,
      tmpFilters: {},
      sortable: [
        'StudyDate',
        'PatientID',
        'PatientName',
      ],
    };
  },
  computed: {
    ...mapGetters({
      source: 'source',
      studies: 'studies',
      filters: 'filters',
      sendingFiles: 'sending',
    }),
    canUpload() {
      let canUpload = true;
      if (process.env.VUE_APP_DISABLE_UPLOAD !== undefined) {
        canUpload = !process.env.VUE_APP_DISABLE_UPLOAD.includes('true');
      }
      return canUpload;
    },
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
    },
  },
  watch: {
    sendingFiles() {
      if (!this.sendingFiles) {
        this.updateStudies(0, this.studiesParams.offset > 0 ? this.studiesParams.offset : this.studiesParams.limit);
        this.updateAlbumInbox();
        this.$refs.infiniteLoading.stateChanger.reset();
      }
    },
    filters: {
      handler() {
        // const noFiltersSet = Object.values(this.filters).every((filterValue) => filterValue === '');
        this.searchStudies();
      },
      deep: true,
    },
  },
  mounted() {
    this.setTopstyle();
    // https://stackoverflow.com/questions/12452349/mobile-viewport-height-after-orientation-change
    window.addEventListener('orientationchange', () => {
      const afterOrientationChange = () => {
        this.setTopstyle();
        this.studiesListRender += 1;
        window.removeEventListener('resize', afterOrientationChange);
      };
      window.addEventListener('resize', afterOrientationChange);
    }, false);
  },
  created() {
    this.initData();
    this.setAlbumInbox();
    this.setFilters();
    this.setQueryParams();
  },
  onDestroyed() {
    this.initData();
  },
  methods: {
    setQueryParams() {
      if (this.$route.query.sort !== undefined) {
        const sort = decodeURIComponent(Array.isArray(this.$route.query.sort) ? this.$route.query.sort[0] : this.$route.query.sort);
        if (this.sortable.includes(sort.replace('-', ''))) {
          this.studiesParams.sortDesc = sort.includes('-');
          this.studiesParams.sortBy = sort.replace('-', '');
        }
      }
    },
    setFilters() {
      let showFilters = false;
      const filters = {};
      Object.keys(this.$route.query).forEach((key) => {
        const value = decodeURIComponent(Array.isArray(this.$route.query[key]) ? this.$route.query[key][0] : this.$route.query[key]);
        if (this.filters[key] !== undefined && key !== 'StudyDateFrom' && key !== 'StudyDateTo') {
          filters[key] = value;
          showFilters = true;
        }
        if (key === 'StudyDate') {
          let date = [];
          if (value.includes('-')) {
            date = value.split('-');
          } else {
            date.push(value);
            date.push(value);
          }
          filters.StudyDateFrom = this.dateFormatter(date[0]);
          filters.StudyDateTo = this.dateFormatter(date[1]);
          showFilters = true;
        }
      });
      if (showFilters === true) {
        // this.changeFilterValue();
        this.$store.dispatch('setShowFilters', showFilters);
        this.$store.dispatch('setFilters', filters);
      }
    },
    initData() {
      const source = this.albumID === undefined ? 'inbox' : this.albumID;
      this.$store.dispatch('initStudies', source);
      this.$store.dispatch('initSeries');
      this.$store.dispatch('initModalities');
      this.$store.dispatch('initFilters');
      this.$store.dispatch('initShowFilters');
    },
    setTopstyle() {
      const elStickyHeader = this.$el.querySelector(`[id='${this.headerID}']`);
      this.topstyle = `${elStickyHeader.offsetHeight + this.defaulttop}px`;
    },
    getStudies(offset = 0, limit = 0) {
      const queries = this.setStudiesQueries(offset, limit);
      this.tmpFilters = { ...this.filters };
      return this.$store.dispatch('getStudies', { queries });
    },
    updateStudies(offset = 0, limit = 0) {
      const queries = this.setStudiesQueries(offset, limit);
      return this.$store.dispatch('updateStudies', { queries });
    },
    searchStudies() {
      this.studiesParams.offset = 0;
      this.$store.dispatch('initStudies', { });
      this.$store.dispatch('initSeries');
      this.infiniteId += 1;
    },
    reloadStudies() {
      this.searchStudies();
      this.updateAlbumInbox();
    },
    updateAlbumInbox() {
      if (this.albumID !== undefined) {
        this.getAlbum().then(() => {
          this.setAlbumInbox();
        });
      } else {
        this.setAlbumInbox();
      }
    },
    getAlbum() {
      return this.$store.dispatch('getAlbum', { album_id: this.albumID }).catch((err) => {
        this.$router.push('/albums');
        return err;
      });
    },
    setAlbumInbox() {
      if (this.albumID !== undefined) {
        if (!this.currentuserOnView) {
          this.$store.dispatch('getProviders', { albumID: this.albumID });
        }
        this.$store.dispatch('setModalitiesAlbum');
      } else {
        this.$store.dispatch('getInboxInfo');
      }
    },
    // https://peachscript.github.io/vue-infinite-loading/old/#!/getting-started/trigger-manually
    infiniteHandler($state) {
      this.firstLoading = this.studies.length === 0 && this.firstLoading;
      this.getStudies(this.studiesParams.offset, this.studiesParams.limit).then((res) => {
        if (res.status !== undefined) {
          this.statusList = res.status;
        }
        if (this.studies.length === parseInt(res.headers['x-total-count'], 10)) {
          if (this.studies.length > 0) {
            $state.loaded();
          }
          $state.complete();
        }
        if (res.status === 200 && res.data.length > 0) {
          this.studiesParams.offset += this.studiesParams.limit;
          $state.loaded();
        } else if (res.status === 204 && res.data.length === 0) {
          $state.complete();
        }
        if (this.checkFilters() === false) {
          this.searchStudies();
        }
        this.firstLoading = false;
      }).catch((err) => {
        if (err.response !== undefined && err.response.status !== undefined) {
          this.statusList = err.response.status;
        }
        $state.error();
        this.firstLoading = false;
        return err;
      });
    },
    checkFilters() {
      if (Object.keys(this.tmpFilters).length > 0) {
        if (Object.keys(this.tmpFilters).length !== Object.keys(this.filters).length) {
          return false;
        }
        const difference = Object.keys(this.filters).every((key) => this.tmpFilters[key] === this.filters[key]);
        return difference;
      }
      return true;
    },
    setStudiesQueries(offset = 0, limit = 0) {
      const params = {
        limit,
        offset,
        includefield: ['comments', '00081030'],
        sort: (this.studiesParams.sortDesc ? '-' : '') + this.studiesParams.sortBy,
      };
      if (Object.keys(this.source).length > 0) {
        params.includefield.push('favorite');
        params[this.source.key] = this.source.value;
      }
      const queries = Object.assign(params, this.prepareFilters());
      return queries;
    },
    prepareFilters() {
      const filtersToSend = {};
      Object.keys(this.filters).forEach((id) => {
        if (this.filters[id] !== '' && this.filters[id] !== null) {
          if (id === 'PatientName' || id === 'StudyDescription' || id === 'PatientID') {
            filtersToSend[id] = `*${this.filters[id]}*`;
          } else if (id === 'StudyDateFrom') {
            if (this.filters.StudyDateTo === '' || this.filters.StudyDateTo === null) {
              filtersToSend.StudyDate = `${this.transformDate(this.filters[id])}-`;
            } else {
              filtersToSend.StudyDate = `${this.transformDate(this.filters[id])}-${this.transformDate(this.filters.StudyDateTo)}`;
            }
          } else if (id === 'StudyDateTo') {
            if (this.filters.StudyDateFrom === '' || this.filters.StudyDateFrom === null) {
              filtersToSend.StudyDate = `-${this.transformDate(this.filters[id])}`;
            }
          } else {
            filtersToSend[id] = this.filters[id];
          }
        }
      });
      return filtersToSend;
    },
    transformDate(date) {
      return moment(date).format('YYYYMMDD');
    },
    inputLoadFiles(filesFromInput) {
      if (filesFromInput.length > 0) {
        this.$emit('loadfiles', filesFromInput);
      }
    },
    inputLoadDirectories(filesFromInput) {
      if (filesFromInput.length > 0) {
        this.$emit('loaddirectories', filesFromInput);
      }
    },
    sortingChanged(ctx) {
      this.studiesParams.sortDesc = ctx.sortDesc;
      this.studiesParams.sortBy = ctx.sortBy;
      this.searchStudies();
    },
  },
};
</script>
