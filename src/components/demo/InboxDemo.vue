<i18n>
{
  "en": {
    "demotext": "Do you wan't a demonstration ?"
  },
  "fr" : {
  }
}
</i18n>

<template>
  <div>
    <inbox />
    <b-modal
      id="demoModal"
      ref="demoModal"
      header-class="bg-primary"
      body-class="bg-secondary"
      centered
      no-fade
      hide-footer
      no-close-on-backdrop
      size="lg"
    >
      <template v-slot:modal-title>
        Kheops Demonstration
      </template>
      <dl class="my-2 row">
        <dt class="col-12 font-large">
          {{ $t('demotext') }}
        </dt>
      </dl>
      <dl
        class="my-2 row"
      >
        <dd class="col-6 pointer">
          <button
            type="button"
            class="btn btn-secondary btn-sm"
            @click="setResponse()"
          >
            Yes
          </button>
        </dd>
        <div class="col-6 pointer">
          <button
            type="button"
            class="btn btn-secondary btn-sm"
            @click="setResponse()"
          >
            No
          </button>
        </div>
      </dl>
    </b-modal>
  </div>
</template>

<script>

import Inbox from '@/components/inbox/Inbox';

export default {
  name: 'InboxDemonstration',
  components: { Inbox },
  mixins: [],
  props: {
  },
  data() {
    return {};
  },
  computed: {
  },
  watch: {
  },
  mounted() {
    const demoDone = localStorage.getItem('inboxdemo');
    if (demoDone !== true) {
      this.$refs.demoModal.show();
    }
  },
  created() {
  },
  destroyed() {
  },
  methods: {
    setDemoInbox(value) {
      localStorage.setItem('inboxdemo', value);
    },
    setResponse() {
      this.$refs.demoModal.hide();
      this.setDemoInbox(true);
    }
  },
};

</script>
