<template>
  <span
    v-if="loading"
    class="nowrap"
    :title="$t('provider.urlWorking')"
  >
    <kheops-clip-loader
      :loading="loading"
      :size="'20px'"
      :color="'white'"
      :class="classIcon"
    />
  </span>
  <span
    v-else-if="checkURL"
    class="nowrap"
    :title="$t('provider.urlWorking')"
  >
    <v-icon
      name="check-circle"
      :class="classIcon"
      :color="'#41B883'"
    />
  </span>
  <span
    v-else
    class="nowrap"
    :title="$t('provider.urlNotWorking')"
  >
    <v-icon
      name="ban"
      :class="classIcon"
      :color="'red'"
    />
  </span>
</template>

<script>
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'StateProvider',
  components: { KheopsClipLoader },
  props: {
    loading: {
      type: Boolean,
      required: true,
      default: false,
    },
    checkURL: {
      type: Boolean,
      required: true,
      default: false,
    },
    classIcon: {
      type: String,
      requried: false,
      default: '',
    },
  },
};
</script>
