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
    "webhook": "Webhook",
    "reload": "Reload"
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
    "webhook": "Webhook",
    "reload": "Recharger"
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
      stacked="sm"
      striped
      hover
      show-empty
      :items="webhooks"
      :fields="fields"
      :busy="loadingData"
      tbody-tr-class="link"
      @row-clicked="rowSelectedWebhook"
    >
      <template v-slot:table-busy>
        <loading />
      </template>
      <template v-slot:empty>
        <div
          class="text-warning text-center"
        >
          <list-empty
            :status="status"
            :text-empty="$t('nowebhooks')"
            @reload="getWebhooks()"
          />
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
        v-slot:cell(statuswebhook)="data"
      >
        <state-icons
          v-if="data.item.number_of_triggers !== undefined && data.item.number_of_triggers > 0"
          :done="checkPass(data.item.last_triggers)"
          :error="checkFail(data.item.last_triggers)"
          :warning="checkBoth(data.item.last_triggers)"
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
import httpoperations from '@/mixins/httpoperations';
import StateIcons from '@/components/globals/StateIcons';
import ListEmpty from '@/components/globallist/ListEmpty';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'ListTokens',
  components: { ListEmpty, StateIcons, Loading },
  props: {
    albumId: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      status: -1,
      loadingData: false,
      fields: [
        {
          key: 'statuswebhook',
          label: '',
          sortable: true,
          tdClass: 'word-break',
        },
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
      this.loadingData = true;
      this.$store.dispatch('getWebhooks', params).then(() => {
        this.loadingData = false;
        this.status = -1;
      }).catch((err) => {
        this.loadingData = false;
        this.status = httpoperations.getStatusError(err);
      });
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
      this.$store.dispatch('updateWebhook', params);
    },
    checkPass(lastTriggers) {
      return lastTriggers.every((trigger) => trigger.status === 'pass');
    },
    checkFail(lastTriggers) {
      return lastTriggers.every((trigger) => trigger.status === 'fail');
    },
    checkBoth(lastTriggers) {
      const tmpTrigger = {};
      lastTriggers.forEach((trigger) => {
        if (tmpTrigger[trigger.status] === undefined) {
          tmpTrigger[trigger.status] = '1';
        }
      });
      return Object.entries(tmpTrigger).length > 1;
    },
  },
};
</script>
