<template>
  <div>
    <div
      class="pt-2"
    >
      <div
        class="d-flex flex-wrap"
      >
        <div class="p-2 align-self-center d-none d-md-block">
          <span>{{ $tc("study.selectednbstudies", selectedStudiesNb, { count: selectedStudiesNb }) }}</span>
        </div>
        <div
          v-if="showSendButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
            @click.stop="formSendStudy=!formSendStudy"
          >
            <span>
              <v-icon
                class="align-middle"
                name="paper-plane"
              />
            </span><br>
            <span>{{ $t("send") }}</span>
          </button>
        </div>
        <!--
          v-if="!albumId || (album.send_series || album.is_admin)"
        -->

        <dropdown-albums
          v-if="showAlbumButton === true"
          :disabled="selectedStudiesNb === 0"
          @create-album="goToCreateAlbum"
          @add-to-album="addToAlbum"
        />
        <div
          v-if="showInboxButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
            @click="addToInbox()"
          >
            <span>
              <v-icon
                class="align-middle"
                name="bars"
              />
            </span><br>
            <span>{{ $t("study.addInbox") }}</span>
          </button>
        </div>
        <div
          v-if="showFavoriteButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
            @click="favoriteSelectedStudies()"
          >
            <span>
              <v-icon
                class="align-middle"
                name="star"
              />
            </span><br>
            <span>{{ $t("study.infoFavorites") }}</span>
          </button>
        </div>
        <div
          v-if="showDeleteButton === true"
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center"
            :disabled="selectedStudiesNb === 0"
            @click="confirmDelete=!confirmDelete"
          >
            <span>
              <v-icon
                class="align-middle"
                name="trash"
              />
            </span><br>
            <span>{{ $t("delete") }}</span>
          </button>
        </div>
        <div
          class="align-self-center ml-auto text-right"
        >
          <dropdown-import-study
            v-if="showImportButton === true"
          />
          <button
            type="button"
            class=" btn btn-link "
            @click="reloadStudies()"
          >
            <v-icon
              name="refresh"
              scale="2"
              class="kheopsicon"
            />
          </button>
          <button
            type="button"
            class=" btn btn-link"
            @click="setFilters()"
          >
            <v-icon
              name="search"
              class="kheopsicon"
              scale="1.8"
            />
          </button>
        </div>
      </div>
    </div>
    <confirm-button
      v-if="confirmDelete && selectedStudiesNb"
      :btn-danger-text="$t('delete')"
      :btn-primary-text="$t('cancel')"
      :text="$tc('study.confirmDelete', selectedStudiesNb, {countStudies: selectedStudiesNb, countSeries: selectedSeriesNb})"
      :text-delete-contact="$t('study.confirmDeleteContact', {deleteContactEmail: deleteContactEmail})"
      :method-confirm="() => confirmDelete=false"
      :method-cancel="deleteStudies"
    />
    <form-get-user
      v-if="formSendStudy && selectedStudiesNb"
      @get-user="sendToUser"
      @cancel-user="formSendStudy=false"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import formGetUser from '@/components/user/getUser';
import ConfirmButton from '@/components/globalbutton/ConfirmButton.vue';
import DropdownAlbums from '@/components/albums/DropdownAlbums';
import DropdownImportStudy from '@/components/study/DropdownImportStudy';
import { CurrentUser } from '@/mixins/currentuser.js';

export default {
  name: 'ListHeaders',
  components: {
    formGetUser, ConfirmButton, DropdownImportStudy, DropdownAlbums,
  },
  mixins: [CurrentUser],
  props: {
    studies: {
      type: Array,
      required: true,
      default: () => ([]),
    },
    albumId: {
      type: String,
      required: true,
      default: '',
    },
    showSendButton: {
      type: Boolean,
      required: false,
      default: true,
    },
    showAlbumButton: {
      type: Boolean,
      required: false,
      default: true,
    },
    showInboxButton: {
      type: Boolean,
      required: false,
      default: true,
    },
    showFavoriteButton: {
      type: Boolean,
      required: false,
      default: true,
    },
    showDeleteButton: {
      type: Boolean,
      required: false,
      default: true,
    },
    showImportButton: {
      type: Boolean,
      required: false,
      default: true,
    },
  },
  data() {
    return {
      formSendStudy: false,
      confirmDelete: false,
      albumNameMaxLength: 25,
    };
  },
  computed: {
    ...mapGetters({
      series: 'series',
      source: 'source',
      showFilters: 'showFilters',
    }),
    selectedStudiesNb() {
      return _.filter(this.studies, (s) => (s.flag.is_selected === true || s.flag.is_indeterminate === true)).length;
    },
    selectedSeriesNb() {
      let selectedStudiesSeriesNb = 0;
      this.selectedStudies.forEach((study) => {
        selectedStudiesSeriesNb += parseInt(study.NumberOfStudyRelatedSeries.Value[0], 10);
      });

      let selectedSerieNb = 0;
      Object.keys(this.selectedSeries).forEach((studyUID) => {
        selectedSerieNb += Object.keys(this.selectedSeries[studyUID]).length;
      });
      return selectedSerieNb + selectedStudiesSeriesNb;
    },
    selectedStudies() {
      return _.filter(this.studies, (s) => (s.flag.is_selected === true));
    },
    selectedSeries() {
      const selectedSeries = {};
      const studiesIndeterminate = this.studies.filter((study) => (study.flag.is_indeterminate === true));
      studiesIndeterminate.forEach((study) => {
        const seriesSelected = [];
        const studyUID = study.StudyInstanceUID.Value[0];
        Object.keys(this.series[studyUID]).forEach((serieUID) => {
          if (this.series[studyUID][serieUID].flag.is_selected === true) {
            seriesSelected.push(this.series[studyUID][serieUID]);
          }
          if (seriesSelected.length > 0) {
            selectedSeries[studyUID] = seriesSelected;
          }
        });
      });
      return selectedSeries;
    },
    allSelectedStudies() {
      return _.filter(this.studies, (s) => (s.flag.is_selected === true || s.flag.is_indeterminate === true));
    },
    deleteContactEmail() {
      let deleteContactEmail = false;
      if (process.env.VUE_APP_DELETE_CONTACT !== undefined) {
        deleteContactEmail = !process.env.VUE_APP_DELETE_CONTACT.includes('false');
      }
      return deleteContactEmail;
    },
  },

  watch: {
    selectedStudiesNb: {
      handler(selectedStudiesNb) {
        if (selectedStudiesNb === 0) {
          this.confirmDelete = false;
          this.formSendStudy = false;
        }
      },
    },
  },
  created() {
  },
  mounted() {
  },
  methods: {
    sendToUser(userSub) {
      if (this.selectedStudiesNb > 0) {
        const promises = [];
        this.selectedStudies.forEach((study) => {
          const params = {
            StudyInstanceUID: study.StudyInstanceUID.Value[0],
            userSub,
            queries: this.getSource(),
          };
          promises.push(this.$store.dispatch('sendStudy', params));
        });
        Object.keys(this.selectedSeries).forEach((studyUID) => {
          this.selectedSeries[studyUID].forEach((serie) => {
            const params = {
              StudyInstanceUID: studyUID,
              SeriesInstanceUID: serie.SeriesInstanceUID.Value[0],
              userSub,
              queries: this.getSource(),
            };
            promises.push(this.$store.dispatch('sendSerie', params));
          });
        });

        Promise.all(promises).then(() => {
          this.$snotify.success(`${this.selectedStudiesNb} ${this.$t('study.studiessharedsuccess')}`);
          this.formSendStudy = false;
          this.deselectStudySeries();
        }).catch(() => {
        });
      }
    },
    setObjectFlagStudy(StudyInstanceUID, flag, value) {
      const paramsIsSelected = {
        StudyInstanceUID,
        flag,
        value,
      };
      return paramsIsSelected;
    },
    setObjectFlagSerie(StudyInstanceUID, SeriesInstanceUID, flag, value) {
      const paramsIsIndeterminate = {
        StudyInstanceUID,
        SeriesInstanceUID,
        flag,
        value,
      };
      return paramsIsIndeterminate;
    },
    deselectStudySeries() {
      this.allSelectedStudies.forEach((study) => {
        const StudyInstanceUID = study.StudyInstanceUID.Value[0];
        if (study.flag.is_selected === true) {
          this.$store.dispatch('setFlagByStudyUID', this.setObjectFlagStudy(StudyInstanceUID, 'is_selected', false));
        }
        if (this.series[StudyInstanceUID] !== undefined) {
          Object.keys(this.series[StudyInstanceUID]).forEach((serieUID) => {
            const serie = this.series[StudyInstanceUID][serieUID];
            if (serie.flag.is_selected) {
              const SeriesInstanceUID = serie.SeriesInstanceUID.Value[0];
              this.$store.dispatch('setFlagByStudyUID', this.setObjectFlagStudy(StudyInstanceUID, 'is_indeterminate', false));
              this.$store.dispatch('setFlagByStudyUIDSerieUID', this.setObjectFlagSerie(StudyInstanceUID, SeriesInstanceUID, 'is_selected', false));
            }
          });
        }
      });
    },
    favoriteSelectedStudies() {
      const favorites = this.allSelectedStudies.every((s) => s.flag.is_favorite === true);
      const params = {
        StudyInstanceUID: '',
        queries: this.getSource(),
        value: !favorites,
      };
      this.allSelectedStudies.forEach((study) => {
        [params.StudyInstanceUID] = study.StudyInstanceUID.Value;
        this.$store.dispatch('favoriteStudy', params);
      });
    },
    getSource() {
      if (this.source.key !== undefined || this.source.value !== undefined) {
        return {
          [this.source.key]: this.source.value,
        };
      }
      return {};
    },
    getHeaders() {
      if (this.currentuserCapabilitiesToken !== undefined && this.currentuserAccessToken !== undefined) {
        return {
          Authorization: `Bearer ${this.currentuserAccessToken}`,
          'X-Authorization-Source': `Bearer ${this.currentuserCapabilitiesToken}`,
        };
      }
      return undefined;
    },
    deleteStudies() {
      this.manageDeleteStudiesSeries().then(() => {
        this.updateModalities();
      }).catch(() => {
        this.$snotify.error(this.$t('sorryerror'));
      });
      this.confirmDelete = false;
      this.deselectStudySeries();
    },
    manageDeleteStudiesSeries() {
      const promisesDeleteStudies = this.deleteSelectedStudies();
      const promisesDeleteSeries = this.deleteSelectedSeries();
      return Promise.all(promisesDeleteStudies.concat(promisesDeleteSeries));
    },
    deleteSelectedStudies() {
      const promises = [];
      this.selectedStudies.forEach((study) => {
        const source = this.albumId === undefined || this.albumId === '' ? 'inbox' : this.albumId;
        const params = {
          StudyInstanceUID: study.StudyInstanceUID.Value[0],
          source,
        };
        if (this.albumId === '') {
          promises.push(this.$store.dispatch('deleteStudy', params));
        } else {
          params.album_id = this.albumId;
          promises.push(this.$store.dispatch('removeStudyInAlbum', params));
        }
      });
      return promises;
    },
    deleteSelectedSeries() {
      const promises = [];
      Object.keys(this.selectedSeries).forEach((studyUID) => {
        this.selectedSeries[studyUID].forEach((serie) => {
          const serieUID = serie.SeriesInstanceUID.Value[0];
          const params = {
            StudyInstanceUID: studyUID,
            SeriesInstanceUID: serieUID,
          };
          if (this.albumId === '') {
            promises.push(this.$store.dispatch('deleteSerie', params));
          } else {
            params.album_id = this.albumId;
            promises.push(this.$store.dispatch('removeSerieInAlbum', params));
          }
        });
      });
      return Promise.all(promises);
    },
    updateModalities() {
      if (this.albumId !== '') {
        this.$store.dispatch('getAlbum', { album_id: this.albumId }).then(() => {
          this.$store.dispatch('setModalitiesAlbum');
        });
      } else {
        this.$store.dispatch('getInboxInfo');
      }
    },
    checkErrorStatus(status, message) {
      if (status === 403) {
        this.$snotify.error(message[403]);
      } else if (status === 404) {
        this.$snotify.error(message[404]);
      } else {
        this.$snotify.error(message.unknown);
      }
    },
    addToAlbum(albumId) {
      const queries = this.getSource();
      const headers = this.getHeaders();
      let sendSerie = 0;
      let sendStudy = 0;
      const studySerieData = this.generateStudySerieData(albumId);
      const message = {
        403: '',
        404: '',
        unknown: '',
      };
      this.$store.dispatch('putStudiesInAlbum', { queries, data: studySerieData, headers }).then((res) => {
        res.forEach((data) => {
          if (data.res !== undefined && data.res.status === 201) {
            if (data.serieId !== undefined) {
              sendSerie += 1;
            } else {
              sendStudy += 1;
            }
          } else {
            message[403] = `Forbidden to send in album ${data.albumId}`;
            message[404] = 'Not found';
            message.unknown = `Error unknown for the study ${data.studyId}`;
            const status = data.res !== undefined && data.res.status !== undefined ? data.res.status : data.res.request.status;
            this.checkErrorStatus(status, message);
          }
        });
        if (sendStudy > 0 || sendSerie > 0) {
          this.$snotify.success(`${sendStudy} studies send ${sendSerie > 0 ? `and ${sendSerie} series send ` : ''} to the album`);
        }
        this.deselectStudySeries();
      });
    },
    generateStudySerieData(albumId) {
      const data = [];
      this.selectedStudies.forEach((study) => {
        data.push({
          album_id: albumId,
          study_id: study.StudyInstanceUID.Value[0],
        });
      });
      Object.keys(this.selectedSeries).forEach((studyUID) => {
        this.selectedSeries[studyUID].forEach((serie) => {
          data.push({
            album_id: albumId,
            study_id: studyUID,
            serie_id: serie.SeriesInstanceUID.Value[0],
          });
        });
      });
      return data;
    },
    addToInbox() {
      const queries = this.getSource();
      const headers = this.getHeaders();
      const studySerieData = this.generateStudySerieData(this.albumId);
      let sendStudy = 0;
      let sendSerie = 0;
      const message = {
        403: '',
        404: '',
        unknown: '',
      };
      this.$store.dispatch('selfAppropriateStudy', { data: studySerieData, queries, headers }).then((res) => {
        res.forEach((data) => {
          if (data.res !== undefined && data.res.status === 201) {
            if (data.serieId !== undefined) {
              sendSerie += 1;
            } else {
              sendStudy += 1;
            }
          } else {
            message[403] = 'Forbidden to send in your inbox';
            message[404] = 'Not found';
            message.unknown = `Error unknown for the study ${data.studyId}`;
            this.checkErrorStatus(data.res !== undefined ? data.res.status : '', message);
          }
        });
        if (sendStudy > 0 || sendSerie > 0) {
          this.$snotify.success(`${sendStudy} studies send ${sendSerie > 0 ? `and ${sendSerie} series send ` : ''} to the inbox`);
        }
        this.deselectStudySeries();
      });
    },
    setFilters() {
      this.$store.dispatch('setShowFilters', !this.showFilters);
      if (this.showFilters === false) {
        this.$store.dispatch('initFilters');
      }
    },
    reloadStudies() {
      this.$emit('reloadStudies');
    },
    goToCreateAlbum() {
      const StudiesUID = [];
      this.selectedStudies.forEach((study) => {
        StudiesUID.push(study.StudyInstanceUID.Value[0]);
      });

      const SeriesUID = [];
      Object.keys(this.selectedSeries).forEach((studyUID) => {
        this.selectedSeries[studyUID].forEach((serie) => {
          SeriesUID.push(`${studyUID},${serie.SeriesInstanceUID.Value[0]}`);
        });
      });

      const query = {};
      if (StudiesUID.length > 0) query.StudyInstanceUID = StudiesUID;
      if (SeriesUID.length > 0) query.SeriesInstanceUID = SeriesUID;
      if (Object.keys(query).length > 0) {
        query.source = this.albumId === '' ? 'inbox' : this.albumId;
        this.$router.push({ path: '/albums/new', query });
      } else {
        this.$router.push({ path: '/albums/new' });
      }
    },
  },
};

</script>
