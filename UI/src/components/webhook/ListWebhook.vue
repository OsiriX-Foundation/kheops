<template>
  <div>
    <div class="d-flex">
      <div>
        <h4>
          {{ $t('webhook.webhook') }}
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
      striped
      hover
      show-empty
      sort-icon-left
      :items="webhooks"
      :fields="fields"
      :busy="loadingData"
      tbody-tr-class="link"
      @row-clicked="rowSelectedWebhook"
    >
      <template #table-busy>
        <loading />
      </template>
      <template #empty>
        <div
          class="text-warning text-center"
        >
          <list-empty
            :status="status"
            :text-empty="$t('webhook.nowebhooks')"
            @reload="getWebhooks()"
          />
        </div>
      </template>
      <template #emptyfiltered>
        <div
          class="text-warning text-center"
        >
          {{ $t('webhook.nowebhooks') }}
        </div>
      </template>
      <template #cell(enabled)="row">
        <toggle-button
          :value="row.item.enabled"
          :sync="true"
          :color="{checked: '#5fc04c', unchecked: 'grey'}"
          @change="setEnbaled(row.item.id, row.item.enabled)"
        />
      </template>
      <template
        #cell(statuswebhook)="data"
      >
        <state-icons
          v-if="data.item.number_of_triggers !== undefined && data.item.number_of_triggers > 0"
          :done="checkPass(data.item.last_triggers)"
          :error="checkFail(data.item.last_triggers)"
          :warning="checkBoth(data.item.last_triggers)"
        />
      </template>
      <template
        #cell(btn_edit)="data"
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
          sortable: false,
          tdClass: 'word-break',
        },
        {
          key: 'name',
          label: this.$t('webhook.name'),
          sortable: true,
          tdClass: 'word-break',
        },
        {
          key: 'url',
          label: this.$t('webhook.url'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-sm-table-cell',
        },
        {
          key: 'events',
          label: this.$t('webhook.events'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-lg-table-cell',
          formatter: (values) => {
            if (Array.isArray(values)) {
              return values.map((value) => this.$t(`webhook.${value}`)).join(', ');
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
