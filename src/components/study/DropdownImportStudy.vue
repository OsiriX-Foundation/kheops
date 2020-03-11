<i18n>
{
  "en": {
    "importdir": "Import directory",
    "importfiles": "Import files",
    "draganddrop": "Or drag and drop"
  },
  "fr": {
    "importdir": "Importer un dossier",
    "importfiles": "Importer des fichiers",
    "draganddrop": "Ou Drag and Drop"
  }
}
</i18n>
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
        {{ $t("importfiles") }}
      </label>
    </b-dropdown-item-button>
    <b-dropdown-item-button
      v-if="determineWebkitDirectory()"
      :disabled="sendingFiles"
    >
      <label for="directory">
        {{ $t("importdir") }}
      </label>
    </b-dropdown-item-button>
    <b-dropdown-divider />
    <b-dropdown-item-button
      v-if="determineWebkitDirectory()"
      @click="showDragAndDrop"
    >
      {{ $t("draganddrop") }}
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
