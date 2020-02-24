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
    "edit": "Edit",
    "refresh": "Refresh",
    "webhook": "Webhook"
  },
  "fr": {
    "nowebhooks": "Aucun webhook créé",
    "name": "Description",
    "url": "URL",
    "events": "Evènements",
    "enabled": "Activé",
    "new_series": "Nouvelles séries",
    "new_user": "Nouvel utilisateur",
    "edit": "Editer",
    "refresh": "Rafraîchir",
    "webhook": "Webhook"
  }
}
</i18n>
<template>
  <div>
    <div class="d-flex">
      <div>
        <h4>
          {{ $t('webhook') }}
        </h4>
      </div>
      <div class="ml-auto">
        <button
          class="btn btn-sm btn-primary"
          @click.stop="getWebhooks()"
        >
          {{ $t('refresh') }}
        </button>
      </div>
    </div>
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
        v-slot:cell(status)="data"
      >
        <triggers
          v-if="data.item.number_of_triggers > 0"
          :triggers="data.item.last_triggers"
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
import Triggers from '@/components/webhook/Triggers';

export default {
  name: 'ListTokens',
  components: { Triggers },
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
          class: 'd-none d-lg-table-cell',
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
        // {
        //   key: 'status',
        //   label: 'Response status',
        //   sortable: false,
        //   class: 'd-none d-md-table-cell',
        // },
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
    this.$store.dispatch('initWebhooks');
    this.getWebhooks();
  },
  beforeDestroy() {
    this.$store.dispatch('initWebhooks');
  },
  methods: {
    getWebhooks() {
      const params = {
        albumId: this.albumId,
      };
      this.$store.dispatch('getWebhooks', params);
    },
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
