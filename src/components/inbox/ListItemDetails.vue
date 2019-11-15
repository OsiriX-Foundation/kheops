<i18n>
{
  "en": {
    "selectednbstudies": "{count} study is selected | {count} studies are selected",
    "addalbum": "Add to an album",
    "infoFavorites": "Favorites",
    "addfavorites": "Add too favorites",
    "addfavorites": "Remove too favorites",
    "confirmDelete": "Are you sure you want to delete {count} study | Are you sure you want to delete {count} studies",
    "confirmDeleteSeries": "containing {count} serie? Once deleted, you will not be able to re-upload any series if other users still have access to them. | containing {count} series? Once deleted, you will not be able to re-upload any series if other users still have access to them.",
    "series": "Series",
    "comments": "Comments",
    "metadata": "Metadata",
    "errorSeries": "An error occured, please reload the series list",
    "reload": "Reload"
  },
  "fr": {
    "selectednbstudies": "{count} étude est sélectionnée | {count} études sont sélectionnées",
    "addalbum": "Ajouter à un album",
    "infoFavorites": "Favoris",
    "addfavorites": "Ajouter aux favoris",
    "addfavorites": "Supprimer des favoris",
    "confirmDelete": "Etes vous de sûr de vouloir supprimer {count} étude | Etes vous de sûr de vouloir supprimer {count} études",
    "confirmDeleteSeries": "contenant {count} série? Une fois supprimée, vous ne pouvais plus charger cette série tant qu'un autre utilisateur a accès à cette série. | contenant {count} séries? Une fois supprimées, vous ne pouvais plus charger ces séries tant qu'un autre utilisateur a accès à ces séries.",
    "series": "Séries",
    "comments": "Commentaires",
    "metadata": "Métadonnées",
    "errorSeries": "Une erreur est survenue, veuillez recharger les séries.",
    "reload": "Recharger"
  }
}
</i18n>

<template>
  <div class="row">
    <div class="col-lg-2 col-xl-auto mb-4">
      <nav class="nav nav-pills nav-justified flex-column text-center text-xl-left">
        <a
          class="nav-link"
          :class="(study.flag.view === 'series')?'active':''"
          @click="setViewDetails(study.StudyInstanceUID.Value[0], 'series')"
        >
          {{ $t('series') }}
        </a>
        <a
          class="nav-link"
          :class="(study.flag.view === 'comments')?'active':''"
          @click="setViewDetails(study.StudyInstanceUID.Value[0], 'comments')"
        >
          {{ $t('comments') }}
        </a>
        <a
          class="nav-link"
          :class="(study.flag.view === 'study')?'active':''"
          @click="setViewDetails(study.StudyInstanceUID.Value[0], 'study')"
        >
          {{ $t('metadata') }}
        </a>
      </nav>
    </div>
    <div
      class="col-sm-12 col-md-12 col-lg-10 col-xl-10"
    >
      <div
        v-if="study.flag.view === 'series'"
      >
        <div
          v-if="loadingSerie === false && errorSeries === false"
          class="row"
        >
          <div
            v-for="serie in series[studyUID]"
            :key="serie.id"
            class="col-12 col-sm-6 col-md-4 col-lg-3 col-xl-2 mb-5"
          >
            <series-summary
              :serie="serie"
              :study="study"
              :source="source"
            />
          </div>
        </div>
        <pulse-loader
          :loading="loadingSerie"
          color="white"
        />
        <div
          v-if="loadingSerie === false && errorSeries === true"
        >
          <div
            class="d-flex flex-column justify-content-center align-items-center full-height"
          >
            <div class="mb-3">
              {{ $t('errorSeries') }}
            </div>
            <div class="">
              <button
                type="button"
                class=" btn btn-md"
                @click="getSeries()"
              >
                {{ $t('reload') }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <comments-and-notifications
        v-if="study.flag.view === 'comments'"
        :id="study.StudyInstanceUID.Value[0]"
        :write-comments="this.$route.name !== 'viewnologin'"
        scope="studies"
      />
      <study-metadata
        v-if="study.flag.view === 'study'"
        :id="study.StudyInstanceUID.Value[0]"
        scope="studies"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import PulseLoader from 'vue-spinner/src/PulseLoader.vue';
import commentsAndNotifications from '@/components/comments/commentsAndNotifications';
import studyMetadata from '@/components/study/studyMetadata';
import SeriesSummary from '@/components/inbox/SeriesSummary';

export default {
  name: 'ListItemDetails',
  components: {
    commentsAndNotifications, studyMetadata, PulseLoader, SeriesSummary,
  },
  props: {
    studyUID: {
      type: String,
      required: true,
      default: '',
    },
    albumId: {
      type: String,
      required: true,
      default: '',
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {
      loadingSerie: true,
      errorSeries: false,
      includefield: ['00080021', '00080031'],
    };
  },
  computed: {
    ...mapGetters({
      series: 'series',
      sendingFiles: 'sending',
    }),
    study() {
      return this.$store.getters.getStudyByUID(this.studyUID);
    },
  },
  watch: {
    sendingFiles() {
      if (!this.sendingFiles) {
        this.getSeries();
      }
    },
  },
  created() {
    this.getSeries();
  },
  methods: {
    getSeries() {
      const params = {
        StudyInstanceUID: this.studyUID,
        studySelected: this.study.flag.is_selected,
        queries: {},
      };
      if (Object.keys(this.source).length > 0) {
        params.queries[this.source.key] = this.source.value;
      }
      params.queries.includefield = this.includefield;
      this.loadingSerie = true;
      this.$store.dispatch('getSeries', params).then((res) => {
        if (res.status === 200) {
          this.loadingSerie = false;
          this.errorSeries = false;
        }
      }).catch((err) => {
        console.log(err);
        this.loadingSerie = false;
        this.errorSeries = true;
      });
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
  },
};
</script>
