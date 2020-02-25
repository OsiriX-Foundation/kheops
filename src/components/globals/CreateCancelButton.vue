<template>
  <div class="row">
    <div class="offset-md-3 col-md-9 d-none d-sm-none d-md-block">
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

export default {
  name: 'ConfirmButton',
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false,
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
