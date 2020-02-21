<template>
  <span
    class="row"
  >
    <span
      v-for="trigger in triggers"
      :key="trigger.id"
      class="col-2"
    >
      <button
        v-if="trigger.status === 'pass'"
        type="button"
        class="btn btn-success btn-lg btn-squared mr-1"
      />
      <button
        v-if="trigger.status === 'fail'"
        type="button"
        class="btn btn-danger btn-lg btn-squared mr-1"
      />
    </span>
  </span>
</template>

<script>

export default {
  name: 'Triggers',
  components: { },
  props: {
    triggers: {
      type: Array,
      required: true,
      default: () => [],
    },
  },
  data() {
    return {
    };
  },
  computed: {
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
  },
};
</script>
<style>
.btn-squared {
  border: none;
  padding: 12px;
  border-radius: 50%;
}
</style>
