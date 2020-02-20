<i18n>
{
  "en": {
    "remove": "Remove"
  },
  "fr": {
    "remove": "Supprimer"
  }
}
</i18n>
<template>
  <span
    v-if="show"
  >
    <div
      class="row"
      :class="classRow"
    >
      <div :class="classCol">
        <button
          class="btn btn-primary btn-block"
          @click="done"
        >
          {{ textButtonDone }}
        </button>
        <button
          v-if="!confirmDelete"
          type="button"
          class="btn btn-danger btn-block"
          @click="confirmDelete = true"
        >
          {{ $t('remove') }}
        </button>
      </div>
    </div>
    <div
      v-if="confirmDelete"
      class="row"
      :class="classRow"
    >
      <div :class="classCol">
        <p
          class="mt-2"
        >
          {{ textWarningRemove }}
        </p>
      </div>
    </div>
    <div
      v-if="confirmDelete"
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
</template>

<script>

export default {
  name: 'DoneDeleteButton',
  props: {
    show: {
      type: Boolean,
      required: false,
      default: true,
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
    textWarningRemove: {
      type: String,
      required: true,
      default: '',
    },
    textButtonDone: {
      type: String,
      required: true,
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
