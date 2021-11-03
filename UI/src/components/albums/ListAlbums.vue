<template>
  <div>
    <list-albums-headers
      class="list-header"
      :disabled-btn-share="albumsSelected.length === 0"
      :albums-selected="albumsSelected"
      @searchClick="showFilters = !showFilters"
      @reloadAlbums="searchAlbums"
    />
    <b-table
      striped
      sort-icon-left
      :hover="mobiledetect ? false : true"
      :items="albums"
      :fields="fields"
      :sort-desc="albumsParams.sortDesc"
      :sort-by="albumsParams.sortBy"
      :no-local-sorting="true"
      :dark="false"
      :no-sort-reset="true"
      :tbody-class="'link'"
      @row-clicked="clickAlbum"
      @sort-changed="sortingChanged"
      @row-hovered="setItemHover"
      @row-unhovered="setItemUnhover"
    >
      <template
        #head(name)="data"
      >
        {{ data.label }}
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.name"
            v-focus
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('listalbums.filter')"
          >
        </div>
      </template>
      <template
        #head(number_of_studies)="data"
      >
        {{ data.label }}
      </template>
      <template
        #head(number_of_users)="data"
      >
        {{ data.label }}
      </template>
      <template
        #head(number_of_comments)="data"
      >
        {{ data.label }}
      </template>
      <template
        #head(created_time)="data"
      >
        {{ data.label }}
        <div
          v-if="showFilters"
          class="form-row"
          @click.stop=""
        >
          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.CreateDateFrom"
                :disabled-dates="disabledFromCreateDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('listalbums.fromDate')"
              />
            </div>
          </div>
          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.CreateDateTo"
                :disabled-dates="disabledToCreateDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('listalbums.toDate')"
              />
            </div>
          </div>
        </div>
      </template>
      <template
        #head(last_event_time)="data"
      >
        {{ data.label }}
        <div
          v-if="showFilters"
          class="form-row"
          @click.stop=""
        >
          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.EventDateFrom"
                :disabled-dates="disabledFromEventDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('listalbums.fromDate')"
              />
            </div>
          </div>

          <div class="col form-inline">
            <div class="form-group">
              <datepicker
                v-model="filters.EventDateTo"
                :disabled-dates="disabledToEventDates"
                input-class="form-control form-control-sm search-calendar"
                wrapper-class="calendar-wrapper"
                :placeholder="$t('listalbums.toDate')"
              />
            </div>
          </div>
        </div>
      </template>
      <template
        #cell(is_selected)="row"
      >
        <b-button-group>
          <b-form-checkbox
            v-if="row.item.is_admin || row.item.add_user"
            v-model="row.item.is_selected"
            inline
            @click.native.stop
          />
        </b-button-group>
      </template>
      <template
        #cell(name)="row"
      >
        <div
          :class="'d-flex flex-wrap'"
        >
          <div class="">
            {{ row.value }}
          </div>
          <list-albums-icons
            :album="row.item"
          />
        </div>
      </template>
      <template
        #cell(created_time)="data"
      >
        {{ data.item.created_time | formatDate }}
      </template>
      <template
        #cell(last_event_time)="data"
      >
        {{ data.item.last_event_time | formatDate }}
      </template>
    </b-table>
    <infinite-loading
      :identifier="infiniteId"
      @infinite="infiniteHandler"
    >
      <div slot="spinner">
        <loading />
      </div>
      <div slot="no-more">
        {{ $t('listalbums.nomorealbums') }}
      </div>
      <div slot="no-results">
        {{ $t('listalbums.noresults') }}
      </div>
      <div slot="error">
        <span
          v-if="statusList === 401 || statusList === 403"
        >
          {{ $t('listalbums.nopermissions') }}
        </span>
        <span
          v-else
        >
          {{ $t('listalbums.error') }}<br><br>
          <button
            type="button"
            class=" btn btn-md"
            @click="searchAlbums()"
          >
            {{ $t('listalbums.reload') }}
          </button>
        </span>
      </div>
    </infinite-loading>
  </div>
</template>
<script>

import Datepicker from 'vuejs-datepicker';
import InfiniteLoading from 'vue-infinite-loading';
import moment from 'moment';
import ListAlbumsHeaders from '@/components/albums/ListAlbumsHeaders';
import ListAlbumsIcons from '@/components/albums/ListAlbumsIcons';
import mobiledetect from '@/mixins/mobiledetect.js';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'Albums',
  components: {
    InfiniteLoading, ListAlbumsHeaders, Datepicker, Loading, ListAlbumsIcons,
  },
  data() {
    return {
      statusList: 200,
      showFilters: false,
      infiniteId: 0,
      albumsKey: 'all',
      albumsParams: {
        offset: 0,
        limit: 50,
        sortDesc: true,
        sortBy: 'last_event_time',
      },
      fields: [
        {
          key: 'is_selected',
          label: '',
          thClass: 'table-albums-header',
          sortable: false,
          class: 'td_checkbox_albums word-break',
          thStyle: {
            width: '100px',
          },
        },
        {
          key: 'name',
          label: this.$t('listalbums.name'),
          thClass: 'pointer table-albums-header',
          tdClass: 'name',
          sortable: true,
          class: 'word-break',
          thStyle: {
            width: '250px',
          },
        },
        {
          key: 'number_of_studies',
          label: this.$t('listalbums.Study #'),
          sortable: true,
          thClass: 'pointer table-albums-header',
          class: 'd-none d-sm-table-cell word-break',
          thStyle: {
            width: '200px',
          },
        },
        {
          key: 'number_of_users',
          label: this.$t('listalbums.User #'),
          sortable: true,
          thClass: 'pointer table-albums-header',
          class: 'd-none d-md-table-cell word-break',
          thStyle: {
            width: '200px',
          },
        },
        {
          key: 'number_of_comments',
          label: this.$t('listalbums.Message #'),
          sortable: true,
          thClass: 'pointer table-albums-header',
          class: 'd-none d-lg-table-cell word-break',
          thStyle: {
            width: '200px',
          },
        },
        {
          key: 'created_time',
          label: this.$t('listalbums.Date'),
          sortable: true,
          thClass: 'pointer table-albums-header',
          class: 'd-none d-sm-table-cell word-break',
          thStyle: {
            width: '200px',
            zIndex: 11,
          },
        },
        {
          key: 'last_event_time',
          label: this.$t('listalbums.LastEvent'),
          sortable: true,
          thClass: 'pointer table-albums-header',
          class: 'd-none d-lg-table-cell word-break',
          thStyle: {
            width: '200px',
            zIndex: 10,
          },
        },
        {
          key: 'modalities',
          label: this.$t('listalbums.Modalities'),
          sortable: false,
          formatter: (value) => {
            if (value.length > 0) {
              return value.join(', ');
            }
            return this.$t('listalbums.nomodality');
          },
          class: 'word-break',
          thClass: 'table-albums-header',
          thStyle: {
            width: '200px',
          },
        },
      ],
      filters: {
        name: '',
        number_of_studies: '',
        modalities: '',
        number_of_users: '',
        number_of_comments: '',
        CreateDateFrom: '',
        CreateDateTo: '',
        EventDateFrom: '',
        EventDateTo: '',
      },
    };
  },
  computed: {
    albums() {
      return this.$store.getters.getAlbumsByKey(this.albumsKey);
    },
    albumsSelected() {
      return this.albums.filter((album) => album.is_selected === true);
    },
    disabledToCreateDates() {
      const vm = this;
      return {
        to: vm.filters.CreateDateFrom,
        from: new Date(),
      };
    },
    disabledFromCreateDates() {
      return {
        from: new Date(),
      };
    },
    disabledToEventDates() {
      const vm = this;
      return {
        to: vm.filters.EventDateFrom,
        from: new Date(),
      };
    },
    disabledFromEventDates() {
      return {
        from: new Date(),
      };
    },
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
    },
  },
  watch: {
    filters: {
      handler() {
        this.searchAlbums();
      },
      deep: true,
    },
    showFilters: {
      handler(showFilters) {
        if (!showFilters) {
          this.filters = {
            name: '',
            number_of_studies: '',
            modalities: '',
            number_of_users: '',
            number_of_comments: '',
            CreateDateFrom: '',
            CreateDateTo: '',
            EventDateFrom: '',
            EventDateTo: '',
          };
        }
      },
    },
  },
  created() {
    this.$store.dispatch('initAlbums', { key: this.albumsKey });
  },
  destroyed() {
    this.$store.dispatch('initAlbums', { key: this.albumsKey });
  },
  methods: {
    clickAlbum(item) {
      if (item.album_id) {
        this.$router.push(`/albums/${item.album_id}`);
      }
    },
    infiniteHandler($state) {
      this.getAlbums(this.albumsParams.offset, this.albumsParams.limit).then((res) => {
        if (res.status !== undefined) {
          this.statusList = res.status;
        }
        if (this.albums.length === parseInt(res.headers['x-total-count'], 10)) {
          $state.complete();
        }
        if (res.status === 200 && res.data.length > 0) {
          this.albumsParams.offset += this.albumsParams.limit;
          $state.loaded();
        } else {
          $state.complete();
        }
      }).catch((err) => {
        if (err.response !== undefined && err.response.status !== undefined) {
          this.statusList = err.response.status;
        }
        $state.error();
        return err;
      });
    },
    getAlbums(offset = 0, limit = 0) {
      const params = {
        limit,
        offset,
        sort: (this.albumsParams.sortDesc ? '-' : '') + this.albumsParams.sortBy,
      };
      const queries = Object.assign(params, this.prepareFilters());
      return this.$store.dispatch('getAlbums', { queries, key: this.albumsKey });
    },
    prepareFilters() {
      const filtersToSend = {};
      Object.keys(this.filters).forEach((id) => {
        if (this.filters[id] !== '') {
          if (id === 'name') {
            filtersToSend[id] = `*${this.filters[id]}*`;
          } else if (id === 'CreateDateFrom') {
            if (this.filters.CreateDateTo === '') {
              filtersToSend.created_time = `${this.transformDate(this.filters[id])}-`;
            } else {
              filtersToSend.created_time = `${this.transformDate(this.filters[id])}-${this.transformDate(this.filters.CreateDateTo)}`;
            }
          } else if (id === 'CreateDateTo') {
            if (this.filters.CreateDateFrom === '') {
              filtersToSend.created_time = `-${this.transformDate(this.filters[id])}`;
            }
          } else if (id === 'EventDateFrom') {
            if (this.filters.EventDateTo === '') {
              filtersToSend.last_event_time = `${this.transformDate(this.filters[id])}-`;
            } else {
              filtersToSend.last_event_time = `${this.transformDate(this.filters[id])}-${this.transformDate(this.filters.EventDateTo)}`;
            }
          } else if (id === 'EventDateTo') {
            if (this.filters.EventDateFrom === '') {
              filtersToSend.last_event_time = `-${this.transformDate(this.filters[id])}`;
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
    sortingChanged(ctx) {
      this.albumsParams.sortDesc = ctx.sortDesc;
      this.albumsParams.sortBy = ctx.sortBy;
      this.searchAlbums();
    },
    searchAlbums() {
      this.albumsParams.offset = 0;
      this.$store.dispatch('initAlbums', { key: this.albumsKey });
      this.infiniteId += 1;
    },
    setItemHover(item, index) {
      this.albums[index].flag.is_hover = true;
    },
    setItemUnhover(item, index) {
      this.albums[index].flag.is_hover = false;
    },
  },
};
</script>
