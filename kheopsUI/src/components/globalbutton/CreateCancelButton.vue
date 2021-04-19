<template>
  <div
    v-if="loading"
    class="row"
  >
    <div
      :class="classCol"
    >
      <kheops-clip-loader
        :size="'30px'"
        color="white"
        class="text-left"
      />
    </div>
  </div>
  <div
    v-else
    class="row"
  >
    <div
      class="d-none d-sm-none d-md-block"
      :class="classCol"
    >
      <button
        type="submit"
        class="btn btn-primary"
        :disabled="disabled"
        @click="create"
      >
        {{ $t('create') }}
      </button>
      <button
        type="reset"
        class="btn btn-secondary ml-3"
        @click="cancel"
      >
        {{ $t('cancel') }}
      </button>
    </div>
    <div class="col-12 d-md-none">
      <button
        type="submit"
        class="btn btn-primary btn-block"
        :disabled="disabled"
      >
        {{ $t('create') }}
      </button>
    </div>
  </div>
</template>
<script>
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'ConfirmButton',
  components: { KheopsClipLoader },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false,
    },
    loading: {
      type: Boolean,
      required: false,
      default: false,
    },
    classCol: {
      type: String,
      required: false,
      default: '',
    },
  },
  methods: {
    create() {
      this.$emit('create');
    },
    cancel() {
      this.$emit('cancel');
    },
  },
};
</script>
