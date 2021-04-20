<template>
  <icon-list-providers
    :study="study"
    :providers="providersEnable"
    :album-id="source.key === 'album' ? source.value : ''"
    @dropdownState="setShowIcons"
  />
</template>
<script>
import { mapGetters } from 'vuex';
import IconListProviders from '@/components/providers/IconListProviders.vue';

export default {
  name: 'ManageProviders',
  components: { IconListProviders },
  props: {
    study: {
      type: Object,
      required: true,
      default: () => ({}),
    },
    source: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  computed: {
    ...mapGetters({
      providers: 'providers',
      studies: 'studies',
    }),
    providersEnable() {
      return this.providers.filter((provider) => provider.stateURL.checkURL === true);
    },
  },
  methods: {
    setShowIcons(value, studyUID, index = -1) {
      let studyIndex = index;
      if (studyIndex === -1) {
        studyIndex = this.studies.findIndex((study) => study.StudyInstanceUID.Value[0] === studyUID);
      }
      this.studies[studyIndex].showIcons = value;
    },
  },
};
</script>
