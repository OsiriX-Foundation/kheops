<template>
  <span
    v-if="status === -1"
  >
    {{ textEmpty }}
  </span>
  <span
    v-else
  >
    <text-error
      :status="status"
    />
    <span
      v-if="status !== 401 && status !== 403"
    >
      <br><br>
      <button
        type="button"
        class="btn btn-sm btn-primary"
        @click="reload()"
      >
        {{ $t('reload') }}
      </button>
    </span>
  </span>
</template>

<script>
import TextError from '@/components/globals/TextError';

export default {
  name: 'ListEmpty',
  components: { TextError },
  props: {
    status: {
      type: Number,
      required: true,
      default: -1,
    },
    textEmpty: {
      type: String,
      required: true,
      default: '',
    },
  },
  methods: {
    reload() {
      this.$emit('reload');
    },
  },
};
</script>
