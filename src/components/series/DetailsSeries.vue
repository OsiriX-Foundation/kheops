<template>
  <div
    v-if="loadingSerie === false"
    class="row"
    :class="classRow"
  >
    <div
      v-for="serieUid in serieUids"
      :key="serieUid.key"
      class="word-break"
      :class="classCol"
    >
      <detail-serie
        :serie="series[studyUid][serieUid]"
        :height="height"
        :width="width"
      />
    </div>
  </div>
  <span
    v-else
  >
    <loading />
  </span>
</template>
<script>
import { mapGetters } from 'vuex';
import DetailSerie from '@/components/series/DetailsSerie';
import Loading from '@/components/globals/Loading';

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
      this.$store.dispatch('getSeries', params).then((res) => {
        if (res.status === 200) {
          this.loadingSerie = false;
          this.errorSeries = false;
        }
      }).catch(() => {
        this.loadingSerie = false;
        this.errorSeries = true;
      });
    },
  },
};

</script>
