<template>
  <span>
    <icon
      v-if="sorter.cas!=ccas"
      class="ml-1 pt-1"
      name="sort"
    />
    <icon
      v-if="sorter.cas==ccas && sorter.order=='asc'"
      class="ml-1 pt-1"
      name="sort-up"
    />
    <icon
      v-if="sorter.cas==ccas && sorter.order=='desc'"
      class="ml-1 pt-1"
      name="sort-down"
    />
  </span>
</template>

<script>

export default {
  name: 'SortIcon',
  props: {
    sorter: {
      type: Object,
      required: true,
    },
  },
};

</script>
