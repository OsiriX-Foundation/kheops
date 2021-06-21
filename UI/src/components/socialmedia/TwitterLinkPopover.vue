<template>
  <span>
    <div>
      <div class="row mt-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <label>
            {{ $t('sharinglink.revoke') }}
          </label>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            v-if="loading === false"
            type="submit"
            class="btn btn-danger btn-block"
            @click="revoke"
          >
            {{ $t('revoke') }}
          </button>
          <kheops-clip-loader
            v-if="loading === true"
          />
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-12 col-md-12">
          <button
            type="reset"
            class="btn btn-secondary btn-block"
            @click="cancel"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </div>
  </span>
</template>
<script>
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'TwitterLinkPopover',
  components: { KheopsClipLoader },
  props: {
    tokens: {
      type: Array,
      required: true,
      default: () => [],
    },
    loading: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  data() {
    return {
      toggle_revoke: false,
    };
  },
  methods: {
    revoke() {
      this.$emit('revoke', this.tokens);
    },
    cancel() {
      this.$emit('cancel');
    },
  },
};
</script>
