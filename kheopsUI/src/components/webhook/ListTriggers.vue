<template>
  <span>
    <div
      class="my-3"
    >
      <div class="d-flex mb-2">
        <h4>
          {{ $t('webhook.deliveries') }}
        </h4>
        <button
          class="ml-auto btn btn-sm btn-primary"
          @click="refresh()"
        >
          {{ $t('refresh') }}
        </button>
      </div>
    </div>
    <b-table
      id="table-triggers"
      striped
      hover
      show-empty
      sort-icon-left
      :items="triggers"
      :fields="fields"
      :sort-desc="true"
      :per-page="perPage"
      :current-page="currentPage"
      :busy="loading"
      tbody-tr-class="link"
      @row-clicked="showRowDetails"
    >
      <template #cell(show_details)="row">
        <b-button
          size="sm"
          class="mr-1 pt-0"
          pill
          @click="showRowDetails(row.item)"
        >
          ...
        </b-button>
      </template>
      <template
        slot="row-details"
        slot-scope="row"
      >
        <b-card
          class="pointer-default"
        >
          <detail-attempts
            :trigger="row.item"
            @manualtrigger="manualtrigger"
          />
        </b-card>
      </template>
      <template
        #cell(status)="row"
      >
        <v-icon
          v-if="checkStatus(row.item.attempts) === true"
          name="check-circle"
          :color="'#41B883'"
        />
        <v-icon
          v-if="checkStatus(row.item.attempts) === false"
          name="ban"
          :color="'red'"
        />
      </template><template #empty>
        <div
          class="text-warning text-center"
        >
          {{ $t('webhook.noattempts') }}
        </div>
      </template>
      <template #table-busy>
        <loading />
      </template>
    </b-table>
    <div
      class="my-3 d-flex flex-wrap"
    >
      <b-pagination
        v-model="currentPage"
        class="ml-auto"
        :total-rows="rows"
        :per-page="perPage"
        :limit="limit"
        aria-controls="table-triggers"
        size="sm"
        @change="changePage"
      />
    </div>
  </span>
</template>

<script>
import DetailAttempts from '@/components/webhook/DetailAttempts';
import Loading from '@/components/globalloading/Loading';

export default {
  name: 'ListTriggers',
  components: { DetailAttempts, Loading },
  props: {
    triggers: {
      type: Array,
      required: true,
      default: () => [],
    },
    perPage: {
      type: Number,
      required: true,
      default: 10,
    },
    rows: {
      type: Number,
      required: true,
      default: 1,
    },
    loading: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  data() {
    return {
      currentPage: 1,
      limit: 6,
      fields: [
        {
          key: 'status',
          label: '',
        },
        {
          key: 'id',
          label: '',
          tdClass: 'word-break',
        },
        {
          key: 'event',
          label: this.$t('webhook.event'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-lg-table-cell',
          formatter: (values) => this.$t(`webhook.${values}`),
        },
        {
          key: 'attempts',
          label: this.$t('webhook.date'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-md-table-cell',
          formatter: (values) => {
            if (Array.isArray(values) && values[0].time !== undefined) {
              return this.$options.filters.formatDateTimeDetails(values[0].time);
            }
            return '';
          },
        },
        {
          key: 'show_details',
          label: '',
          thStyle: {
            width: '20px',
          },
        },
      ],
    };
  },
  computed: {
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
    checkStatus(trigger) {
      if (trigger !== undefined && trigger !== null) {
        return trigger.filter((t) => (t.status >= 200 && t.status <= 299)).length > 0;
      }
      return false;
    },
    showRowDetails(item) {
      // eslint-disable-next-line
      this.$set(item, '_showDetails', !item._showDetails);
    },
    changePage(page) {
      this.$emit('change', page);
    },
    manualtrigger() {
      this.$emit('manualtrigger');
    },
    refresh() {
      this.$emit('refresh');
    },
  },
};
</script>
