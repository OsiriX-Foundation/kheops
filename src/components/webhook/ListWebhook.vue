<i18n>
{
  "en": {
    "nowebhooks": "There are no webhook to show"
  },
  "fr": {
    "nowebhooks": "Aucun webhook créé"
  }
}
</i18n>
<template>
  <div>
    <b-table
      v-if="loadingData === false"
      stacked="sm"
      striped
      hover
      show-empty
      :items="webhooks"
      tbody-tr-class="link"
    >
      <template v-slot:empty>
        <div
          class="text-warning text-center"
        >
          {{ $t('nowebhooks') }}
        </div>
      </template>
      <template v-slot:emptyfiltered>
        <div
          class="text-warning text-center"
        >
          {{ $t('nowebhooks') }}
        </div>
      </template>
    </b-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import moment from 'moment';

export default {
  name: 'ListTokens',
  components: { },
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      loadingData: false,
      webhooks: [],
    };
  },
  computed: {
  },
  created() {
  },
  beforeDestroy() {
    this.$store.commit('initWebhooks')
  },
  methods: {
  },
};
</script>
