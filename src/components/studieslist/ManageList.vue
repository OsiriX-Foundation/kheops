<template>
  <div>
    <input
      id="file"
      ref="inputfiles"
      type="file"
      name="file"
      class="inputfile"
      multiple
      :disabled="sendingFiles"
      @change="inputLoadFiles"
    >
    <input
      id="directory"
      ref="inputdir"
      type="file"
      name="file"
      class="inputfile"
      webkitdirectory
      :disabled="sendingFiles"
      @change="inputLoadDirectories"
    >
    <list-headers
      :id="headerID"
      :studies="studies"
      :show-send-button="permissions.send_series"
      :show-album-button="permissions.send_series"
      :show-favorite-button="permissions.add_series && $route.name !== 'viewnologin'"
      :show-delete-button="permissions.delete_series"
      :show-import-button="permissions.add_series && permissions.can_upload"
      :show-inbox-button="permissions.add_inbox"
      :album-id="albumID"
      class="list-header"
      @reloadStudies="reloadStudies"
    />
    <studies-list
      :permissions="permissions"
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
        {{ $t('study.noresults') }}
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

export default {
  name: 'ManageList',
  components: {
    ListHeaders, InfiniteLoading, Loading, StudiesList,
  },
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
      statusList: 200,
      studiesParams: {
        offset: 0,
        limit: 50,
        sortDesc: true,
        sortBy: 'StudyDate',
      },
      filters: {
        PatientName: '',
        PatientID: '',
        StudyDescription: '',
        StudyDateFrom: '',
        StudyDateTo: '',
        ModalitiesInStudy: '',
      },
      headerID: 'listheaders',
    };
  },
  computed: {
    ...mapGetters({
      source: 'source',
      studies: 'studies',
      sendingFiles: 'sending',
    }),
  },
  methods: {
    getStudies(offset = 0, limit = 0) {
      const queries = this.setStudiesQueries(offset, limit);
      return this.$store.dispatch('getStudies', { queries });
    },
    searchStudies() {
      this.studiesParams.offset = 0;
      this.$store.dispatch('initStudies', { });
      this.$store.dispatch('initSeries');
      this.infiniteId += 1;
    },
    reloadStudies() {
      this.searchStudies();
      if (this.albumID !== undefined) {
        this.getAlbum().then(() => {
          this.setAlbumInbox();
        });
      } else {
        this.setAlbumInbox();
      }
    },
    // https://peachscript.github.io/vue-infinite-loading/old/#!/getting-started/trigger-manually
    infiniteHandler($state) {
      this.getStudies(this.studiesParams.offset, this.studiesParams.limit).then((res) => {
        if (res.status !== undefined) {
          this.statusList = res.status;
        }
        if (this.studies.length === parseInt(res.headers['x-total-count'], 10)) {
          $state.complete();
        }
        if (res.status === 200 && res.data.length > 0) {
          this.studiesParams.offset += this.studiesParams.limit;
          $state.loaded();
        } else if (res.status === 204 && res.data.length === 0) {
          $state.complete();
        }
      }).catch((err) => {
        console.log(err);
        if (err.response !== undefined && err.response.status !== undefined) {
          this.statusList = err.response.status;
        }
        $state.error();
        return err;
      });
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
    inputLoadFiles() {
      if (this.$refs.inputfiles.files.length > 0) {
        const filesFromInput = this.$refs.inputfiles.files;
        this.$emit('loadfiles', filesFromInput);
      }
    },
    inputLoadDirectories() {
      if (this.$refs.inputdir.files.length > 0) {
        const filesFromInput = this.$refs.inputdir.files;
        this.$emit('loaddirectories', filesFromInput);
      }
    },
  },
};
</script>
