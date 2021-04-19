<template>
  <v-icon
    v-if="currentHeader !== sortBy"
    class="align-middle"
    :width="width"
    :height="height"
    name="unfold_more"
  />
  <v-icon
    v-else
    class="align-middle"
    :width="width"
    :height="height"
    :name="sortDesc ? 'unfold_up' : 'unfold_down'"
  />
</template>
<script>
export default {
  name: 'SortList',
  components: { },
  mixins: [],
  props: {
    sortBy: {
      type: String,
      required: true,
      default: '',
    },
    currentHeader: {
      type: String,
      required: true,
      default: '',
    },
    sortDesc: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {
      height: 24,
      width: 26,
    };
  },
};
</script>
