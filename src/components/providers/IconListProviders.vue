<template>
  <span>
    <b-dropdown
      v-if="providers.length > 0"
      size="sm"
      variant="link"
      no-caret
      right
      toggle-class="kheopsicon"
      @shown="setShow(true)"
      @hidden="setShow(false)"
    >
      <template slot="button-content">
        <v-icon
          class="align-middle icon-margin"
          name="build"
        />
      </template>
      <b-dropdown-form
        v-for="provider in providers"
        :id="study.StudyInstanceUID.Value[0]"
        :key="provider.id"
        :action="serverURL + '/report'"
        method="post"
      >
        <b-form-input
          v-if="checkProviderModalities(study, provider)"
          type="text"
          hidden
          name="access_token"
          :value="accessToken"
        />
        <b-form-input
          v-if="checkProviderModalities(study, provider)"
          type="text"
          hidden
          name="client_id"
          :value="provider.client_id"
        />
        <b-form-input
          v-if="checkProviderModalities(study, provider)"
          type="text"
          hidden
          name="studyUID"
          :value="study.StudyInstanceUID.Value[0]"
        />
        <button
          v-if="checkProviderModalities(study, provider)"
          type="submit"
          class="dropdown-item pointer"
        >
          {{ provider.name }}
        </button>
      </b-dropdown-form>
    </b-dropdown>
  </span>
</template>
<script>

import Vue from 'vue';
import { serverURL } from '@/app_config';

export default {
  name: 'IconListProviders',
  components: { },
  mixins: [],
  props: {
    providers: {
      type: Array,
      required: true,
      default: () => ([]),
    },
    study: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {
      serverURL,
      show: false,
    };
  },
  computed: {
    accessToken() {
      return Vue.prototype.$keycloak.token;
    },
  },
  watch: {
    show() {
      this.$emit('dropdownState', this.show, this.study.StudyInstanceUID.Value[0]);
    },
  },
  created() {
  },
  mounted() {
  },
  methods: {
    setShow(value) {
      this.show = value;
    },
    checkProviderModalities(study, provider) {
      if (provider.data.supported_modalities === undefined) {
        return true;
      }
      let result = false;
      const modalitiesInStudy = study.ModalitiesInStudy.Value;
      modalitiesInStudy.forEach((modality) => {
        if (provider.data.supported_modalities.includes(modality)) {
          result = true;
        }
      });

      return result;
    },
  },
};
</script>
