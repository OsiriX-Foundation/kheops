<template>
  <span>
    <div
      ref="studiesList"
    >
      <b-table
        class="container-fluid"
        striped
        :hover="mobiledetect ? false : true"
        :items="studies"
        :fields="fields"
        :sort-desc="true"
        :no-local-sorting="true"
        :no-sort-reset="true"
        :tbody-class="'link'"
        @sort-changed="sortingChanged"
        @row-hovered="setItemHover"
        @row-unhovered="setItemUnhover"
        @row-clicked="showRowDetails"
      >
        <!--
          CONTENT TABLE
        -->
        <template
          v-slot:cell(is_selected)="row"
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
                @click.stop="row.toggleDetails"
              />
            </b-button>
            <study-checkbox
              :flag="row.item.flag"
              :study-instance-u-i-d="row.item.StudyInstanceUID.Value[0]"
            />
          </b-button-group>
        </template>
        <template
          v-slot:cell(PatientName)="row"
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
                :show-import-icon="permissions.add_series && permissions.can_upload"
                :show-report-provider-icon="source.key === 'album' ? true : false"
                :show-weasis-icon="!mobiledetect"
                :album-id="source.key === 'album' ? source.value : ''"
                :source="source"
              >
                <template
                  slot="reportprovider"
                >
                  <!--
                  <icon-list-providers
                    :study="row.item"
                    :providers="providersEnable"
                    :album-id="source.key === 'album' ? source.value : ''"
                    @dropdownState="setShowIcons"
                  />
                  -->
                </template>
              </list-icons>
            </div>
          </div>
        </template>
        <template
          v-slot:cell(StudyDate)="row"
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
  </span>
</template>
<script>
import { mapGetters } from 'vuex';
import mobiledetect from '@/mixins/mobiledetect.js';
import ListIcons from '@/components/studieslist/ListIcons';
import IconListProviders from '@/components/providers/IconListProviders.vue';
import ListItemDetails from '@/components/studieslist/ListItemDetails.vue';
import StudyCheckbox from '@/components/studieslist/StudyCheckbox.vue';

export default {
  name: 'StudiesList',
  components: {
    ListIcons, IconListProviders, ListItemDetails, StudyCheckbox,
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
      source: 'source',
    }),
    mobiledetect() {
      return mobiledetect.mobileAndTabletcheck();
    },
  },
  created() {
    this.setQueryParams();
    this.scrollTo();
  },
  mounted() {
  },
  methods: {
    setQueryParams() {
      if (this.$route.query.StudyInstanceUID !== undefined) {
        this.firstScrollTo = decodeURIComponent(Array.isArray(this.$route.query.StudyInstanceUID) ? this.$route.query.StudyInstanceUID[0] : this.$route.query.StudyInstanceUID);
      }
    },
    scrollTo() {
      if (this.firstScrollTo !== '') {
        const study = this.getStudyByUID(this.firstScrollTo);
        if (study.length > 0) {
          const el = this.$el.querySelector(`[id='${this.firstScrollTo}']`);
          const elStickyHeader = this.$el.querySelector(`[id='${this.headerID}']`);
          let offset = 0;
          if (el !== null) {
            offset = el.offsetHeight + 75;
          }
          if (elStickyHeader !== null) {
            offset += elStickyHeader.offsetHeight;
          }
          this.scrollTo(el, -offset);
          this.showRowDetails(study[0]);
        }
        this.firstScrollTo = '';
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
