<template>
  <div
    ref="studiesList"
  >
    <b-table
      class="container-fluid"
      striped
      sort-icon-left
      :hover="mobiledetect ? false : true"
      :items="studies"
      :fields="fields"
      :no-local-sorting="true"
      :no-sort-reset="true"
      :tbody-class="'link'"
      :sort-desc="sortDesc"
      :sort-by="sortBy"
      @sort-changed="sortingChanged"
      @row-hovered="setItemHover"
      @row-unhovered="setItemUnhover"
      @row-clicked="showRowDetails"
    >
      <!--
        HEADER TABLE
      -->
      <template
        #head(PatientName)="data"
      >
        {{ data.label }}
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <div class="d-flex">
            <div class="flex-fill">
              <input
                v-model="filters.PatientName"
                v-focus
                type="search"
                class="form-control form-control-sm"
                :placeholder="$t('study.filter')"
              >
            </div>
            <span>
              <select
                v-model="filters.ModalitiesInStudy"
                class="form-control form-control-sm d-block d-sm-none"
                :placeholder="$t('study.filter')"
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
            </span>
          </div>
        </div>
      </template>

      <template
        #head(PatientID)="data"
      >
        {{ data.label }}
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.PatientID"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('study.filter')"
          >
        </div>
      </template>

      <template
        #head(StudyDescription)="data"
      >
        {{ data.label }}
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <input
            v-model="filters.StudyDescription"
            type="search"
            class="form-control form-control-sm"
            :placeholder="$t('study.filter')"
          >
        </div>
      </template>

      <template
        #head(StudyDate)="data"
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
                v-model="filters.StudyDateFrom"
                :disabled-dates="disabledFromDates"
                input-class="form-control form-control-sm  search-calendar"
                wrapper-class="wrapper-class"
                calendar-class="calendar-class"
                :placeholder="$t('study.fromDate')"
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
                :placeholder="$t('study.toDate')"
                :clear-button="true"
                :typeable="true"
              />
            </div>
          </div>
        </div>
      </template>

      <template
        #head(ModalitiesInStudy)="data"
      >
        {{ data.label }}
        <div
          v-if="showFilters"
          @click.stop=""
        >
          <select
            v-model="filters.ModalitiesInStudy"
            class="form-control form-control-sm display-inline"
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
        </div>
      </template>
      <!--
        CONTENT TABLE
      -->
      <template
        #cell(is_selected)="row"
      >
        <span
          v-if="row.item.StudyInstanceUID !== undefined && row.item.StudyInstanceUID.Value !== undefined"
          :id="`${row.item.StudyInstanceUID.Value[0]}`"
        />
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
            />
          </b-button>
          <study-checkbox
            :flag="row.item.flag"
            :study-instance-u-i-d="row.item.StudyInstanceUID.Value[0]"
          />
        </b-button-group>
      </template>
      <template
        #cell(PatientName)="row"
      >
        <div
          :class="mobiledetect===true ? '' : 'd-flex flex-wrap'"
        >
          <div class="">
            {{ row.value["Alphabetic"] }} {{ row.value["Ideographic"] }}
            <span
              class="d-block d-sm-none"
            >
              {{ row.item.ModalitiesInStudy !== undefined ? row.item.ModalitiesInStudy.Value.join(', ') : '' }}
            </span>
          </div>
          <div :class="mobiledetect===true ? '' : 'ml-auto'">
            <list-icons
              :study="row.item"
              :mobiledetect="mobiledetect"
              :show-favorite-icon="permissions.add_series && $route.name !== 'viewnologin'"
              :show-comment-icon="true"
              :show-download-icon="permissions.download_series"
              :show-import-icon="permissions.add_series && canUpload"
              :show-report-provider-icon="source.key === 'album' ? true : false"
              :show-weasis-icon="!mobiledetect"
              :album-id="source.key === 'album' ? source.value : ''"
              :source="source"
            >
              <template
                slot="reportprovider"
              >
                <manage-providers
                  v-if="oidcIsAuthenticated === true"
                  :study="row.item"
                  :source="source"
                />
              </template>
            </list-icons>
          </div>
        </div>
      </template>
      <template
        #cell(StudyDate)="row"
      >
        {{ row.value | formatDate }}
      </template>
      <!--Infos study (Series / Comments / Study Metadata) -->
      <template
        slot="row-details"
        slot-scope="row"
      >
        <b-card
          class="pointer-default"
        >
          <list-item-details
            :study-u-i-d="row.item.StudyInstanceUID.Value[0]"
            :source="source"
          />
        </b-card>
      </template>
    </b-table>
  </div>
</template>
<script>
import { mapGetters } from 'vuex';
import Datepicker from 'vuejs-datepicker';
import mobiledetect from '@/mixins/mobiledetect.js';
import ListIcons from '@/components/studieslist/ListIcons';
import ManageProviders from '@/components/studieslist/ManageProviders.vue';
import ListItemDetails from '@/components/studieslist/ListItemDetails.vue';
import StudyCheckbox from '@/components/studieslist/StudyCheckbox.vue';

export default {
  name: 'StudiesList',
  components: {
    ListIcons, ManageProviders, ListItemDetails, StudyCheckbox, Datepicker,
  },
  props: {
    permissions: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    canUpload: {
      type: Boolean,
      required: false,
      default: true,
    },
    sortBy: {
      type: String,
      required: false,
      default: 'StudyDate',
    },
    sortDesc: {
      type: Boolean,
      required: false,
      default: true,
    },
    albumID: {
      type: String,
      required: false,
      default: undefined,
    },
    topstyle: {
      type: String,
      required: false,
      default: '125px',
    },
  },
  data() {
    return {
      firstScrollTo: '',
      fields: [
        {
          key: 'is_selected',
          label: '',
          sortable: false,
          thClass: 'pointer table-header',
          class: ['td_checkbox_inbox', 'word-break'],
          thStyle: {
            width: '100px',
            top: this.topstyle,
          },
        },
        {
          key: 'PatientName',
          label: this.$t('study.PatientName'),
          sortable: true,
          thClass: 'pointer table-header',
          tdClass: 'word-break',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value[0];
            }
            return '';
          },
          thStyle: {
            width: '250px',
            top: this.topstyle,
          },
        },
        {
          key: 'PatientID',
          label: this.$t('study.PatientID'),
          sortable: true,
          thClass: 'pointer table-header',
          tdClass: 'word-break',
          class: 'word-break d-none d-md-table-cell d-lg-table-cell',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value[0];
            }
            return '';
          },
          thStyle: {
            width: '250px',
            top: this.topstyle,
          },
        },
        {
          key: 'StudyDescription',
          label: this.$t('study.StudyDescription'),
          sortable: false,
          thClass: 'pointer table-header',
          tdClass: 'word-break',
          class: 'word-break d-none d-lg-table-cell',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value[0];
            }
            return '';
          },
          thStyle: {
            width: '400px',
            top: this.topstyle,
          },
        },
        {
          key: 'StudyDate',
          label: this.$t('study.StudyDate'),
          sortable: true,
          thClass: 'pointer table-header',
          tdClass: 'word-break',
          class: 'word-break d-none d-sm-table-cell d-md-table-cell d-lg-table-cell',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value[0];
            }
            return '';
          },
          thStyle: {
            width: '150px',
            top: this.topstyle,
          },
        },
        {
          key: 'ModalitiesInStudy',
          label: this.$t('study.Modality'),
          sortable: false,
          thClass: 'pointer table-header',
          tdClass: 'word-break',
          class: 'word-break d-none d-sm-table-cell',
          formatter: (value) => {
            if (value !== null && value.Value !== undefined) {
              return value.Value.join(', ');
            }
            return '';
          },
          thStyle: {
            width: '150px',
            top: this.topstyle,
          },
        },
      ],
    };
  },
  computed: {
    ...mapGetters({
      studies: 'studies',
      modalities: 'modalities',
      source: 'source',
      showFilters: 'showFilters',
      filters: 'filters',
    }),
    ...mapGetters('oidcStore', [
      'oidcIsAuthenticated',
    ]),
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
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
  },
  created() {
    this.setQueryParams();
  },
  mounted() {
    if (this.firstScrollTo !== '') {
      this.scrollToStudy(this.firstScrollTo);
      this.firstScrollTo = '';
    }
  },
  methods: {
    setQueryParams() {
      if (this.$route.query.StudyInstanceUID !== undefined) {
        this.firstScrollTo = decodeURIComponent(Array.isArray(this.$route.query.StudyInstanceUID) ? this.$route.query.StudyInstanceUID[0] : this.$route.query.StudyInstanceUID);
      }
    },
    scrollToStudy(studyUID) {
      const study = this.getStudyByUID(studyUID);
      if (study.length > 0) {
        const el = this.$el.querySelector(`[id='${studyUID}']`);
        let offset = 0;
        if (el !== null) {
          offset = el.offsetHeight + 200;
        }
        this.scrollTo(el, -offset);
        this.showRowDetails(study[0]);
      }
    },
    scrollTo(el, offset = 0) {
      if (el !== null) {
        const options = {
          offset,
          cancelable: true,
        };
        this.$scrollTo(el, options);
      }
    },
    getStudyByUID(StudyUID) {
      return this.studies.filter((study) => {
        if (study.StudyInstanceUID !== undefined && study.StudyInstanceUID.Value !== undefined) {
          return study.StudyInstanceUID.Value[0] === StudyUID;
        }
        return false;
      });
    },
    sortingChanged(ctx) {
      this.$emit('sorting-changed', ctx);
    },
    setItemHover(item, index) {
      this.studies[index].flag.is_hover = true;
    },
    setItemUnhover(item, index) {
      this.studies[index].flag.is_hover = false;
    },
    showRowDetails(item) {
      // eslint-disable-next-line
      if (!item._showDetails) {
        this.setViewDetails(item.StudyInstanceUID.Value[0], item.flag.view);
        // eslint-disable-next-line
        item._showDetails = true;
      } else {
        // eslint-disable-next-line
        item._showDetails = false;
      }
    },
    showSeries(row) {
      if (!row.item.detailsShowing) {
        this.toggleDetails(row);
      }
    },
    toggleDetails(row) {
      this.setViewDetails(row.item.StudyInstanceUID.Value[0], row.item.flag.view);
      row.toggleDetails();
    },
    setViewDetails(StudyInstanceUID, flagView = '') {
      const viewSelected = flagView === '' ? 'series' : flagView;
      const params = {
        StudyInstanceUID,
        flag: 'view',
        value: viewSelected,
      };
      this.$store.dispatch('setFlagByStudyUID', params);
    },
  },
};
</script>
