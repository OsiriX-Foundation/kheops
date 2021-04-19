<template>
  <span
    v-if="loadingSerie === false"
  >
    <div
      v-if="series[studyUid] !== undefined"
      class="row"
      :class="classRow"
    >
      <div
        v-for="serie in series[studyUid]"
        :key="serie.key"
        class="word-break"
        :class="classCol"
      >
        <detail-serie
          v-if="serie.SeriesInstanceUID.Value[0] !== undefined"
          :serie="series[studyUid][serie.SeriesInstanceUID.Value[0]]"
          :height="height"
          :width="width"
        />
      </div>
    </div>
  </span>
  <span
    v-else
  >
    <loading />
  </span>
</template>
<script>
import { mapGetters } from 'vuex';
import DetailSerie from '@/components/series/DetailsSerie';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'DetailSeries',
  components: { DetailSerie, Loading },
  props: {
    serieUids: {
      type: Array,
      required: true,
      default: () => [],
    },
    studyUid: {
      type: String,
      required: true,
      default: '',
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    classCol: {
      type: String,
      required: false,
      default: '',
    },
    classRow: {
      type: String,
      required: false,
      default: '',
    },
    height: {
      type: String,
      required: false,
      default: '150',
    },
    width: {
      type: String,
      required: false,
      default: '150',
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
    }),
  },
  created() {
    this.getSeries();
  },
  methods: {
    getSeries() {
      const params = {
        StudyInstanceUID: this.studyUid,
        queries: {},
      };
      if (Object.keys(this.source).length > 0) {
        params.queries[this.source.key] = this.source.value;
      }
      params.queries.includefield = this.includefield;
      this.loadingSerie = true;
      this.$store.dispatch('getSeries', params).then(() => {
        this.errorSeries = false;
        this.loadingSerie = false;
        this.missingSeries();
      }).catch(() => {
        this.loadingSerie = false;
        this.errorSeries = true;
      });
    },
    missingSeries() {
      let missingSeries = [];
      if (this.series[this.studyUid] !== undefined) {
        this.serieUids.forEach((serieUID) => {
          if (this.series[this.studyUid][serieUID] === undefined) {
            missingSeries.push(serieUID);
          }
        });
      } else {
        missingSeries = this.serieUids;
      }
      if (missingSeries.length > 0) {
        this.$emit('missingseries', missingSeries);
      }
    },
  },
};

</script>
