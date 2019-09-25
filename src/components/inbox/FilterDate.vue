<template>
  <div>
    hello
    <v-calendar />
  </div>
</template>
<script>
import VCalendar from 'v-calendar';

export default {
  name: 'FilterDate',
  components: { VCalendar },
  props: {
  },
  data() {
    return {
      attrs: [
        {
          key: 'today',
          dates: new Date(2018, 0, 1),
        },
      ],
    };
  },
  computed: {
  },

  watch: {
  },
  created() {
  },
  mounted() {
  },
  methods: {
  },
};

</script>
