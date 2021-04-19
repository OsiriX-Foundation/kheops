<template>
  <div
    v-if="loading === true"
    class="text-center"
  >
    <div
      class="row"
      :class="classRow"
    >
      <div :class="classCol">
        <kheops-clip-loader
          :size="'40px'"
          color="white"
        />
      </div>
    </div>
  </div>
  <span
    v-else-if="show && loading === false"
  >
    <div
      class="row"
      :class="classRow"
    >
      <div :class="classCol">
        <button
          v-if="!loading && showDone === true"
          class="btn btn-primary btn-block"
          :disabled="disabledDone"
          @click="done"
        >
          {{ textButtonDone === '' ? $t('confirm') : textButtonDone }}
        </button>
        <button
          v-if="!confirmDelete && showDelete === true"
          type="button"
          class="btn btn-danger btn-block"
          @click="confirmDelete = true"
        >
          {{ textButtonRemove === '' ? $t('remove') : textButtonRemove }}
        </button>
      </div>
    </div>
    <span
      v-if="confirmDelete === true && showDelete === true"
    >
      <div
        class="row"
        :class="classRow"
      >
        <div :class="classColWarningRemove !== '' ? classColWarningRemove : classCol">
          <p
            class="mt-2"
          >
            {{ textWarningRemove }}
          </p>
        </div>
      </div>
      <div
        class="row"
        :class="classRow"
      >
        <div :class="classCol">
          <button
            v-if="confirmDelete"
            type="button"
            class="btn btn-danger btn-block"
            @click="remove"
          >
            {{ $t('confirm') }}
          </button>
          <button
            v-if="confirmDelete"
            type="button"
            class="btn btn-secondary btn-block"
            @click="confirmDelete = false"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </span>
  </span>
</template>

<script>
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'DoneDeleteButton',
  components: { KheopsClipLoader },
  props: {
    show: {
      type: Boolean,
      required: false,
      default: true,
    },
    showDelete: {
      type: Boolean,
      required: false,
      default: true,
    },
    showDone: {
      type: Boolean,
      required: false,
      default: true,
    },
    disabledDone: {
      type: Boolean,
      required: false,
      default: false,
    },
    loading: {
      type: Boolean,
      required: false,
      default: false,
    },
    classRow: {
      type: String,
      required: false,
      default: '',
    },
    classCol: {
      type: String,
      required: false,
      default: '',
    },
    classColWarningRemove: {
      type: String,
      required: false,
      default: '',
    },
    textWarningRemove: {
      type: String,
      required: true,
      default: '',
    },
    textButtonDone: {
      type: String,
      required: false,
      default: '',
    },
    textButtonRemove: {
      type: String,
      required: false,
      default: '',
    },
  },
  data() {
    return {
      confirmDelete: false,
    };
  },
  methods: {
    done() {
      this.$emit('done');
    },
    remove() {
      this.$emit('remove');
    },
  },
};
</script>
