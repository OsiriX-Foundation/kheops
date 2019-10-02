<!--
  TODO: Remove settimeout when load studies.
-->
<i18n>
{
  "en": {
    "selectednbstudies": "{count} study is selected | {count} study is selected | {count} studies are selected",
    "addalbum": "Add to an album",
    "download": "Download",
    "addfavorite": "Add to favorites",
    "removefavorite": "Remove to favorites",
    "PatientName": "Patient Name",
    "Modality": "Modality",
    "StudyDate": "Study Date",
    "StudyDescription": "Study description",
    "PatientID": "Patient ID",
    "filter": "Filter",
    "fromDate": "From",
    "toDate": "To",
    "studyputtoalbum": "Studies successfully added to the album",
    "includeseriesfromalbum": "Include series in albums",
    "send": "Send",
    "delete": "Delete",
    "comments": "comments",
    "series": "series",
    "study": "study",
    "nostudy": "No studies found",
    "studiessend": "studies send to inbox",
    "confirmDelete": "Are you sure you want to delete {count} study | Are you sure you want to delete {count} studies",
    "confirmDeleteSeries": "containing {count} serie? Once deleted, you will not be able to re-upload any series if other users still have access to them. | containing {count} series? Once deleted, you will not be able to re-upload any series if other users still have access to them.",
    "cancel": "Cancel",
    "importdir": "Import directory",
    "importfiles": "Import files",
    "draganddrop": "Or drag and drop",
    "favorites": "Favorites",
    "nomorestudies": "No more studies",
    "noresults": "No study found",
    "error": "An error occur please reload the studies.",
    "reload": "Reload"
  },
  "fr": {
    "selectednbstudies": "{count} étude est sélectionnée | {count} étude est sélectionnée | {count} études sont sélectionnées",
    "addalbum": "Ajouter à un album",
    "download": "Télécharger",
    "addfavorite": "Ajouter aux favoris",
    "removefavorite": "Supprimer des favoris",
    "PatientName": "Nom du patient",
    "Modality": "Modalité",
    "StudyDate": "Date de l'étude",
    "StudyDescription": "Description de l'étude",
    "PatientID": "Patient ID",
    "filter": "Filtrer",
    "fromDate": "De",
    "toDate": "A",
    "studyputtoalbum": "L'étude a été enregistrée dans l'album avec succès",
    "includeseriesfromalbum": "inclure des séries présentes dans les albums",
    "send": "Envoyer",
    "delete": "Supprimer",
    "comments": "commentaire",
    "series": "séries",
    "study": "étude",
    "nostudy": "Aucne étude trouvée",
    "studiessend": "études envoyées dans votre boîte de réception",
    "confirmDelete": "Etes vous de sûr de vouloir supprimer ? ",
    "cancel": "Annuler",
    "importdir": "Importer un dossier",
    "importfiles": "Importer des fichiers",
    "draganddrop": "Ou Drag and Drop",
    "favorites": "Favorites",
    "nomorestudies": "Plus d'études",
    "noresults": "Aucune étude trouvée.",
    "error": "Une erreur s'est produite, veuillez recharger les études.",
    "reload": "Recharger"
  }
}
</i18n>

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
      :studies="studies"
      :albums="albums"
      :show-send-button="permissions.send_series"
      :show-album-button="permissions.send_series"
      :show-favorite-button="permissions.add_series && $route.name !== 'test'"
      :show-delete-button="permissions.delete_series"
      :show-import-button="permissions.add_series"
      :show-inbox-button="permissions.add_inbox"
      :album-id="source.key === 'album' ? source.value : ''"
      @setFilters="changeFilterValue"
      @reloadStudies="reloadStudies"
    />
    <div
      id="myHeader"
      ref="myHeader"
      :class="isActive ? 'pt-2 sticky' : ''"
    >
      <list-headers
        v-if="isActive"
        :studies="studies"
        :albums="albums"
        :show-send-button="permissions.send_series"
        :show-album-button="permissions.send_series"
        :show-favorite-button="permissions.add_series && $route.name !== 'test'"
        :show-delete-button="permissions.delete_series"
        :show-import-button="permissions.add_series"
        :show-inbox-button="permissions.add_inbox"
        :album-id="source.key === 'album' ? source.value : ''"
        @setFilters="changeFilterValue"
        @reloadStudies="reloadStudies"
      />
    </div>
    <div
      ref="studiesList"
    >
      <b-table
        class="container-fluid"
        striped
        hover
        :items="studies"
        :fields="fields"
        :sort-desc="true"
        :no-local-sorting="true"
        :no-sort-reset="true"
        :tbody-class="'table-wrapper-scroll-y link'"
        @sort-changed="sortingChanged"
        @row-hovered="setItemHover"
        @row-unhovered="setItemUnhover"
        @row-clicked="showRowDetails"
      >
        <div
          slot="table-busy"
          class="text-center my-2"
        >
          <strong>Loading...</strong>
        </div>

        <!--
          HEADER TABLE
        -->
        <template
          slot="HEAD_PatientName"
          slot-scope="data"
        >
          <div
            v-if="showFilters"
            @click.stop=""
          >
            <input
              v-model="filters.PatientName"
              type="search"
              class="form-control form-control-sm"
              :placeholder="$t('filter')"
            > <br>
          </div>
          <sort-list
            :sort-desc="studiesParams.sortDesc"
            :current-header="data.field.key"
            :sort-by="studiesParams.sortBy"
          />
          {{ data.label }}
        </template>

        <template
          slot="HEAD_PatientID"
          slot-scope="data"
        >
          <div
            v-if="showFilters"
            @click.stop=""
          >
            <input
              v-model="filters.PatientID"
              type="search"
              class="form-control form-control-sm"
              :placeholder="$t('filter')"
            > <br>
          </div>
          <sort-list
            :sort-desc="studiesParams.sortDesc"
            :current-header="data.field.key"
            :sort-by="studiesParams.sortBy"
          />
          {{ data.label }}
        </template>

        <template
          slot="HEAD_StudyDescription"
          slot-scope="data"
        >
          <div
            v-if="showFilters"
            @click.stop=""
          >
            <input
              v-model="filters.StudyDescription"
              type="search"
              class="form-control form-control-sm"
              :placeholder="$t('filter')"
            > <br>
          </div>
          {{ data.label }}
        </template>

        <template
          slot="HEAD_StudyDate"
          slot-scope="data"
        >
          <div
            v-if="showFilters"
            class="form-row"
            @click.stop=""
          >
            <div class="col form-inline">
              <div class="form-group">
                <datepicker
                  v-model="filters.StudyDateFrom"
                  :disabled-dates="disabledFromDates"
                  input-class="form-control form-control-sm  search-calendar"
                  wrapper-class="wrapper-class"
                  calendar-class="calendar-class"
                  :placeholder="$t('fromDate')"
                  :clear-button="true"
                  :typeable="true"
                />
              </div>
            </div>

            <div class="col form-inline">
              <div class="form-group">
                <datepicker
                  v-model="filters.StudyDateTo"
                  :disabled-dates="disabledToDates"
                  input-class="form-control form-control-sm search-calendar"
                  wrapper-class="wrapper-class"
                  calendar-class="calendar-class"
                  :placeholder="$t('toDate')"
                  :clear-button="true"
                  :typeable="true"
                />
              </div>
            </div>
          </div>
          <br v-if="showFilters">
          <sort-list
            :sort-desc="studiesParams.sortDesc"
            :current-header="data.field.key"
            :sort-by="studiesParams.sortBy"
          />
          {{ data.label }}
        </template>

        <template
          slot="HEAD_ModalitiesInStudy"
          slot-scope="data"
        >
          <div
            v-if="showFilters"
            @click.stop=""
          >
            <select
              v-model="filters.ModalitiesInStudy"
              class="form-control form-control-sm"
            >
              <option value="" />
              <option
                v-for="modality in modalities"
                :key="modality.id"
                :value="modality"
              >
                {{ modality }}
              </option>
            </select>
            <br>
            <br>
          </div>
          {{ data.label }}
        </template>
        <!--
          CONTENT TABLE
        -->
        <template
          slot="is_selected"
          slot-scope="row"
        >
          <b-button-group>
            <b-button
              variant="link"
              size="sm"
              class="mr-1 pt-0"
              @click.stop="showSeries(row)"
            >
              <v-icon
                class="align-middle"
                :name="row.detailsShowing ? 'chevron-down' : 'chevron-right'"
                @click.stop="row.toggleDetails"
              />
            </b-button>
            <b-form-checkbox
              v-model="row.item.flag.is_selected"
              :indeterminate="row.item.flag.is_indeterminate"
              class="mr-0"
              inline
              @change="setChecked(row)"
            />
          </b-button-group>
        </template>
        <template
          slot="PatientName"
          slot-scope="row"
        >
          <div
            :class="mobiledetect===true ? '' : 'd-flex flex-wrap'"
          >
            <div class="">
              {{ row.value["Alphabetic"] }} {{ row.value["Ideographic"] }}
            </div>
            <div :class="mobiledetect===true ? '' : 'ml-auto'">
              <!--
        :show-favorite-icon="permissions.add_series"
      -->
              <list-icons
                :study="row.item"
                :mobiledetect="mobiledetect"
                :show-favorite-icon="permissions.add_series && $route.name !== 'test'"
                :show-comment-icon="true"
                :show-download-icon="permissions.download_series"
                :show-import-icon="permissions.add_series"
                :show-report-provider-icon="source.key === 'album' ? true : false"
                :album-id="source.key === 'album' ? source.value : ''"
              >
                <template
                  slot="reportprovider"
                >
                  <icon-list-providers
                    :study="row.item"
                    :providers="providersEnable"
                    @dropdownState="setShowIcons"
                  />
                </template>
              </list-icons>
            </div>
          </div>
        </template>
        <template
          slot="StudyDate"
          slot-scope="row"
        >
          {{ row.value | formatDate }}
        </template>
        <!--Infos study (Series / Comments / Study Metadata) -->
        <template
          slot="row-details"
          slot-scope="row"
        >
          <b-card>
            <list-item-details
              :study-u-i-d="row.item.StudyInstanceUID.Value[0]"
              :album-id="albumID"
              :source="source"
            />
          </b-card>
        </template>
      </b-table>
    </div>
    <infinite-loading
      :identifier="infiniteId"
      @infinite="infiniteHandler"
      ref="infiniteLoading"
    >
      <div slot="spinner">
        <pulse-loader
          color="white"
        />
      </div>
      <div slot="no-more">
        {{ $t('nomorestudies') }}
      </div>
      <div slot="no-results">
        {{ $t('noresults') }}
      </div>
      <div slot="error">
        {{ $t('error') }} <br> <br>
        <button
          type="button"
          class=" btn btn-md"
          @click="searchStudies()"
        >
          {{ $t('reload') }}
        </button>
      </div>
    </infinite-loading>
  </div>
</template>

<script>
// https://peachscript.github.io/vue-infinite-loading/guide/start-with-hn.html
import { mapGetters } from 'vuex';
import InfiniteLoading from 'vue-infinite-loading';
import Datepicker from 'vuejs-datepicker';
import moment from 'moment';
import PulseLoader from 'vue-spinner/src/PulseLoader.vue';
import ListHeaders from '@/components/inbox/ListHeaders';
import ListIcons from '@/components/inbox/ListIcons';
import ListItemDetails from '@/components/inbox/ListItemDetails.vue';
import mobiledetect from '@/mixins/mobiledetect.js';
import SortList from '@/components/inbox/SortList.vue';
import IconListProviders from '@/components/providers/IconListProviders.vue';

export default {
  name: 'Studies',
  components: {
    ListHeaders, ListIcons, ListItemDetails, InfiniteLoading, Datepicker, SortList, IconListProviders, PulseLoader,
  },
  mixins: [],
  props: {
    permissions: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {
      selectedDate: null,
      infiniteId: 0,
      showFilters: false,
      isActive: false,
      showIcons: false,
      studiesParams: {
        offset: 0,
        limit: 50,
        sortDesc: true,
        sortBy: 'StudyDate',
      },
      fields: {
        isSelected: {
          key: 'is_selected',
          label: '',
          sortable: false,
          class: ['td_checkbox', 'breakword'],
          thStyle: {
            width: '100px',
          },
        },
        PatientName: {
          label: this.$t('PatientName'),
          sortable: true,
          tdClass: 'breakwork',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value[0];
            }
            return '';
          },
          thStyle: {
            width: '250px',
          },
        },
        PatientID: {
          label: this.$t('PatientID'),
          sortable: true,
          tdClass: 'breakwork',
          class: 'breakword d-none d-md-table-cell d-lg-table-cell',
          formatter: (value) => value.Value[0],
          thStyle: {
            width: '250px',
          },
        },
        StudyDescription: {
          label: this.$t('StudyDescription'),
          sortable: false,
          tdClass: 'breakwork',
          class: 'breakword d-none d-lg-table-cell',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value[0];
            }
            return '';
          },
          thStyle: {
            width: '400px',
          },
        },
        StudyDate: {
          label: this.$t('StudyDate'),
          sortable: true,
          tdClass: 'breakwork',
          class: 'breakword d-none d-sm-table-cell d-md-table-cell d-lg-table-cell',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value[0];
            }
            return '';
          },
          thStyle: {
            width: '150px',
          },
        },
        ModalitiesInStudy: {
          label: this.$t('Modality'),
          sortable: false,
          tdClass: 'breakwork',
          class: 'breakword d-none d-sm-table-cell',
          formatter: (value) => value.Value.join(', '),
          thStyle: {
            width: '150px',
          },
        },
      },
      filters: {
        PatientName: '',
        PatientID: '',
        StudyDescription: '',
        StudyDateFrom: '',
        StudyDateTo: '',
        ModalitiesInStudy: '',
      },
    };
  },
  computed: {
    ...mapGetters({
      studies: 'studies',
      series: 'series',
      albums: 'albums',
      sendingFiles: 'sending',
      providers: 'providers',
      modalities: 'modalities',
    }),
    OS() {
      return navigator.platform;
    },
    disabledToDates() {
      return {
        to: this.filters.StudyDateFrom,
        from: new Date(),
      };
    },
    disabledFromDates() {
      return {
        from: new Date(),
      };
    },
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
    },
    albumID() {
      if (this.source.key === 'album') {
        return this.source.value
      }
      return undefined;
    },
    providersEnable() {
      return this.providers.filter((provider) => provider.stateURL.checkURL === true);
    },
  },
  watch: {
    sendingFiles() {
      if (!this.sendingFiles) {
        this.updateStudies(0, this.studiesParams.offset > 0 ? this.studiesParams.offset : this.studiesParams.limit);
        this.$refs.infiniteLoading.stateChanger.reset();
      }
    },
    filters: {
      handler() {
        this.searchStudies();
      },
      deep: true,
    },
    showFilters: {
      handler(showFilters) {
        if (!showFilters) {
          this.filters = {
            PatientName: '',
            PatientID: '',
            StudyDescription: '',
            StudyDateFrom: '',
            StudyDateTo: '',
            ModalitiesInStudy: '',
          };
        }
      },
    },
  },
  created() {
    this.initData();
    if (Object.keys(this.source).length > 0) {
      const queriesAlbums = {
        canAddSeries: true,
      };
      this.$store.dispatch('getAlbums', { queries: queriesAlbums });
      this.setAlbumInbox();
    } else {
      let modalities = ['CT', 'SM', 'CR', 'RG', 'DX', 'NM', 'XC', 'AU', 'SR', 'OP']
      this.$store.commit('SET_MODALITIES', modalities);
    }
  },
  destroyed() {
    this.$store.dispatch('initStudies', {});
    this.$store.dispatch('initSeries');
    this.$store.dispatch('initAlbums', {});
  },
  mounted() {
    this.scroll();
  },
  methods: {
    scroll() {
      window.onscroll = () => {
        if (this.$refs.myHeader !== undefined && this.$refs.studiesList !== undefined) {
          const sticky = this.$refs.myHeader.offsetTop;
          const heightSticky = this.$refs.myHeader.clientHeight;
          const studiesList = this.$refs.studiesList.offsetTop;
          if ((window.pageYOffset) > sticky - heightSticky && !this.isActive) {
            this.isActive = true;
          } else if (window.pageYOffset < studiesList - heightSticky) {
            this.isActive = false;
          }
        }
      };
    },
    // https://peachscript.github.io/vue-infinite-loading/old/#!/getting-started/trigger-manually
    infiniteHandler($state) {
      this.getStudies(this.studiesParams.offset, this.studiesParams.limit).then((res) => {
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
        $state.error();
        return err;
      });
    },
    initData() {
      this.$store.dispatch('initStudies', {});
      this.$store.dispatch('initSeries');
      this.$store.dispatch('initModalities');
      this.$store.dispatch('initAlbums', {});
    },
    reloadStudies() {
      this.searchStudies();
      if (Object.keys(this.source).length > 0) {
        if (this.albumID !== undefined) {
          this.getAlbum().then(() => {
            this.setAlbumInbox();
          });
        } else {
          this.setAlbumInbox();
        }
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
        this.$store.dispatch('getProviders', { albumID: this.albumID });
        this.$store.dispatch('setModalitiesAlbum')
      } else {
        this.$store.dispatch('getInboxInfo');
      }
    },
    setItemHover(item, index) {
      this.studies[index].flag.is_hover = true;
    },
    setItemUnhover(item, index) {
      this.studies[index].flag.is_hover = false;
    },
    showSeries(row) {
      if (!row.item.detailsShowing) {
        this.toggleDetails(row);
      }
    },
    setViewDetails(StudyInstanceUID, flagView) {
      const viewSelected = flagView === '' ? 'series' : flagView;
      const params = {
        StudyInstanceUID,
        flag: 'view',
        value: viewSelected,
      };
      this.$store.dispatch('setFlagByStudyUID', params);
    },
    toggleDetails(row) {
      this.setViewDetails(row.item.StudyInstanceUID.Value[0], row.item.flag.view);
      row.toggleDetails();
    },
    createObjectFlag(StudyInstanceUID, studyIndex, flag, value) {
      return {
        StudyInstanceUID,
        studyIndex,
        flag,
        value,
      };
    },
    setChecked(row) {
      const value = row.item.flag.is_selected;
      const StudyInstanceUID = row.item.StudyInstanceUID.Value[0];
      const studyIndex = this.studies.findIndex((study) => study.StudyInstanceUID.Value[0] === StudyInstanceUID);

      const paramsSelected = this.createObjectFlag(StudyInstanceUID, studyIndex, 'is_selected', !value);
      this.$store.dispatch('setFlagByStudyUID', paramsSelected);
      const paramsIndeterminate = this.createObjectFlag(StudyInstanceUID, studyIndex, 'is_indeterminate', false);
      this.$store.dispatch('setFlagByStudyUID', paramsIndeterminate);
      if (this.series[StudyInstanceUID] !== undefined) {
        this.setSeriesCheck(this.series[StudyInstanceUID], paramsSelected);
      }
    },
    setSeriesCheck(series, params) {
      const paramsSetFlag = params;
      Object.keys(series).forEach((serieUID) => {
        paramsSetFlag.SeriesInstanceUID = serieUID;
        this.$store.dispatch('setFlagByStudyUIDSerieUID', paramsSetFlag);
      });
    },
    sortingChanged(ctx) {
      this.studiesParams.sortDesc = ctx.sortDesc;
      this.studiesParams.sortBy = ctx.sortBy;
      this.searchStudies();
    },
    searchStudies() {
      this.studiesParams.offset = 0;
      this.$store.dispatch('initStudies', { });
      this.$store.dispatch('initSeries');
      this.infiniteId += 1;
    },
    setStudiesQueries(offset = 0, limit = 0) {
      const params = {
        limit,
        offset,
        includefield: ['favorite', 'comments', '00081030'],
        sort: (this.studiesParams.sortDesc ? '-' : '') + this.studiesParams.sortBy,
      };
      if (Object.keys(this.source).length > 0) {
        params[this.source.key] = this.source.value;
      }
      const queries = Object.assign(params, this.prepareFilters());
      return queries;
    },
    getStudies(offset = 0, limit = 0) {
      const queries = this.setStudiesQueries(offset, limit);
      return this.$store.dispatch('getStudies', { queries });
    },
    updateStudies(offset = 0, limit = 0) {
      const queries = this.setStudiesQueries(offset, limit);
      return this.$store.dispatch('updateStudies', { queries });
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
    showRowDetails(item) {
      if (!item._showDetails) {
        this.setViewDetails(item.StudyInstanceUID.Value[0], item.flag.view);
        item._showDetails = true;
      } else {
        item._showDetails = false;
      }
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
    changeFilterValue(value) {
      this.showFilters = value;
    },
    setShowIcons(value, studyUID, index = -1) {
      let studyIndex = index;
      if (studyIndex === -1) {
        studyIndex = this.studies.findIndex((study) => study.StudyInstanceUID.Value[0] === studyUID);
      }
      this.studies[studyIndex].showIcons = value;
    },
  },
};

</script>

<style scoped>
  select{
    display: inline !important;
  }
  .iconsHover{
    visibility: visible;
    display: inline;
    cursor: pointer;
  }
  .iconsUnhover{
    visibility: hidden;
    display: inline;
    cursor: pointer;
  }
  .btn-link {
    font-weight: 400;
    color: white;
    background-color: transparent;
  }

  .btn-link:hover {
    color: #c7d1db;
    text-decoration: underline;
    background-color: transparent;
    border-color: transparent;
  }

  .patientNameContainer{
    position: relative;
    white-space: nowrap;
  }

  .patientNameIcons{
    visibility:hidden;
    display: inline;
    cursor: pointer;
  }
  @media (max-width:1024px) {
    .patientNameIcons {
      visibility: visible;
      display: inline-block;
    }
  }
  .patientName:hover .patientNameIcons {
    visibility:visible;
  }

  .patientNameIcons > span.selected{
    visibility:visible !important;
  }

  .patientNameIcons span{
    margin: 0 3px;
  }

  .selection-button-container{
    height: 60px;
  }

  .td_checkbox {
    width: auto;
  }

  input.search-calendar{
    width: 100px !important;
  }

  div.wrapper-class{
    color: #333;
  }

  div.calendar-class{
    color: #333;
  }

  a{
    cursor: pointer;
  }

  a.download{
    color: #FFF;
  }

  a.download:hover{
    color: #fd7e14;
  }
  .inputfile {
    width: 0.1px;
    height: 0.1px;
    opacity: 0;
    overflow: hidden;
    position: absolute;
    z-index: -1;
  }
  .sticky {
    position: fixed;
    top: 70px;
    width: 100%;
    background: #555;
    z-index: 5;
    opacity: 0.95;
  }
  .sticky + .content {
    padding-top: 70px;
  }
  .table-wrapper-scroll-y {
    width: 5px;
    height: 5px;
    overflow: scroll;
    display: block;
  }
  .breakword {
    word-break: break-word;
  }
</style>
