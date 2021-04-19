<template>
  <span>
    <div class="row justify-content-center mb-2">
      <div
        class="d-flex flex-row justify-content-center"
      >
        <img
          v-if="loadingImage === false"
          :src="serie.imgSrc"
          :width="width"
          :height="height"
        >
        <bounce-loader
          v-if="loadingImage === true"
          color="white"
        />
      </div>
    </div>
    <div class="row mb-2 ml-2">
      {{ title }}
    </div>
  </span>
</template>
<script>
import BounceLoader from 'vue-spinner/src/BounceLoader.vue';

export default {
  name: 'DetailSerie',
  components: { BounceLoader },
  props: {
    serie: {
      type: Object,
      required: true,
      default: () => ({}),
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
    };
  },
  computed: {
    loadingImage() {
      return (this.serie.imgSrc === '');
    },
    title() {
      let modality = '';
      let description = '';
      let numberInstances = '';
      if (this.serie.Modality !== undefined && this.serie.Modality.Value !== undefined) {
        [modality] = this.serie.Modality.Value;
      }
      if (this.serie.NumberOfSeriesRelatedInstances !== undefined && this.serie.NumberOfSeriesRelatedInstances.Value !== undefined) {
        numberInstances = `[ ${this.serie.NumberOfSeriesRelatedInstances.Value[0]} ]`;
      }
      if (this.serie.SeriesDescription !== undefined && this.serie.SeriesDescription.Value !== undefined) {
        [description] = this.serie.SeriesDescription.Value;
      }
      return `${modality} - ${description} ${numberInstances}`;
    },
  },
  created() {
  },
  methods: {
  },
};

</script>
