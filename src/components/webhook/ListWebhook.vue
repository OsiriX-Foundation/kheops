<i18n>
{
  "en": {
    "nowebhooks": "There are no webhook to show",
    "name": "Description",
    "url": "URL",
    "events": "Events",
    "enabled": "Enabled",
    "new_series": "New series",
    "new_user": "New user",
    "edit": "Edit"
  },
  "fr": {
    "nowebhooks": "Aucun webhook créé",
    "name": "Description",
    "url": "URL",
    "events": "Evènements",
    "enabled": "Activé",
    "new_series": "Nouvelles séries",
    "new_user": "Nouvel utilisateur",
    "edit": "Editer"
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
      :fields="fields"
      tbody-tr-class="link"
      @row-clicked="rowSelectedWebhook"
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
      <template v-slot:cell(enabled)="row">
        <toggle-button
          :value="row.item.enabled"
          :sync="true"
          :color="{checked: '#5fc04c', unchecked: 'grey'}"
          @change="setEnbaled(row.item.id, row.item.enabled)"
        />
      </template>
      <template
        v-slot:cell(btn_edit)="data"
      >
        <button
          class="btn btn-sm btn-primary"
          @click.stop="editWebhook(data.item.id)"
        >
          {{ $t('edit') }}
        </button>
      </template>
    </b-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

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
      fields: [
        {
          key: 'name',
          label: this.$t('name'),
          sortable: true,
          tdClass: 'word-break',
        },
        {
          key: 'url',
          label: this.$t('url'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-sm-table-cell',
        },
        {
          key: 'events',
          label: this.$t('events'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-md-table-cell',
          formatter: (values) => {
            if (Array.isArray(values)) {
              return values.map((value) => this.$t(value)).join(', ');
            }
            return '';
          },
        },
        {
          key: 'enabled',
          label: this.$t('enabled'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-md-table-cell',
        },
        {
          key: 'btn_edit',
          label: '',
          sortable: false,
        },
      ],
    };
  },
  computed: {
    ...mapGetters({
      webhooks: 'webhooks',
    }),
  },
  created() {
    const params = {
      albumId: this.albumId,
    };
    this.$store.dispatch('initWebhooks');
    this.$store.dispatch('getWebhooks', params);
  },
  beforeDestroy() {
    this.$store.dispatch('initWebhooks');
  },
  methods: {
    rowSelectedWebhook(rowSelected) {
      this.selectWebhook(rowSelected.id);
    },
    selectWebhook(webhookId) {
      this.$emit('webhookselectedshow', webhookId);
    },
    editWebhook(webhookId) {
      this.$emit('webhookselectededit', webhookId);
    },
    setEnbaled(webhooksId, value) {
      const params = {
        albumId: this.albumId,
        webhookId: webhooksId,
        queries: {
          enabled: !value,
        },
      };
      this.$store.dispatch('updateWebhook', params)
    },
  },
};
</script>
