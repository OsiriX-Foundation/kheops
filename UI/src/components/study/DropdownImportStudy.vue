<template>
  <b-dropdown
    id="dropdown-divider"
    toggle-class="kheopsicon"
    variant="link"
    right
  >
    <template slot="button-content">
      <v-icon
        name="add"
        width="34px"
        height="34px"
      />
    </template>
    <b-dropdown-item-button
      :disabled="sendingFiles"
    >
      <label for="file">
        {{ $t("upload.importfiles") }}
      </label>
    </b-dropdown-item-button>
    <b-dropdown-item-button
      v-if="determineWebkitDirectory()"
      :disabled="sendingFiles"
    >
      <label for="directory">
        {{ $t("upload.importdir") }}
      </label>
    </b-dropdown-item-button>
    <b-dropdown-divider />
    <b-dropdown-item-button
      v-if="determineWebkitDirectory()"
      @click="showDragAndDrop"
    >
      {{ $t("upload.draganddrop") }}
    </b-dropdown-item-button>
  </b-dropdown>
</template>
<script>
import { mapGetters } from 'vuex';

export default {
  name: 'DropdownImportStudy',
  props: {},
  computed: {
    ...mapGetters({
      sendingFiles: 'sending',
    }),
  },
  methods: {
    determineWebkitDirectory() {
      // https://stackoverflow.com/questions/11381673/detecting-a-mobile-browser
      const tmpInput = document.createElement('input');
      if ('webkitdirectory' in tmpInput && typeof window.orientation === 'undefined') return true;
      return false;
    },
    showDragAndDrop() {
      this.$store.dispatch('setDemoDragAndDrop', true);
    },
  },
};
</script>
