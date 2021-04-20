<template>
  <span>
    <span
      v-if="show === true"
    >
      <slot name="value-toshow" />
    </span>
    <br>
    <a
      :class="classShowHide"
      @click="show = !show"
    >
      {{ show === true ? computedHideText : computedShowText }}
    </a>
  </span>
</template>
<script>
export default {
  name: 'ShowHide',
  props: {
    classShowHide: {
      type: String,
      required: false,
      default: '',
    },
    textShow: {
      type: String,
      required: false,
      default: '',
    },
    textHide: {
      type: String,
      required: false,
      default: '',
    },
  },
  data() {
    return {
      show: false,
    };
  },
  computed: {
    computedHideText() {
      if (this.textHide === '') {
        return this.$t('hide');
      }
      return this.textHide;
    },
    computedShowText() {
      if (this.textShow === '') {
        return this.$t('show');
      }
      return this.textShow;
    },
  },
};
</script>
