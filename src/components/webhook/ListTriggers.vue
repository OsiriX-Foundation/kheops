<i18n>
{
  "en": {
    "new_series": "new serie",
    "new_user": "new user",
    "manualtrigger": "Manual trigger",
    "date": "Date",
    "event": "Event",
    "deliveries": "Recent attempts",
    "noattempts": "There are no attempts to show",
    "refresh": "Refresh"
  },
  "fr": {
    "new_series": "nouvelles série",
    "new_user": "nouvel utilisateur",
    "manualtrigger": "Déclenchement manuel",
    "date": "Date",
    "event": "Evènement",
    "deliveries": "Tentatives récentes",
    "noattempts": "Il n'y aucune tentative faîte",
    "refresh": "Rafraîchir"
  }
}
</i18n>
<template>
  <span>
    <div
      class="my-3"
    >
      <div class="d-flex mb-2">
        <h4>
          {{ $t('deliveries') }}
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
      stacked="sm"
      striped
      hover
      show-empty
      :items="triggers"
      :fields="fields"
      :sort-desc="true"
      :per-page="perPage"
      :current-page="currentPage"
      :busy="loading"
      tbody-tr-class="link"
      @row-clicked="showRowDetails"
    >
      <template v-slot:cell(show_details)="row">
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
        v-slot:cell(status)="row"
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
      </template><template v-slot:empty>
        <div
          class="text-warning text-center"
        >
          {{ $t('noattempts') }}
        </div>
      </template>
      <template v-slot:table-busy>
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
          label: this.$t('event'),
          tdClass: 'word-break',
          class: 'd-none d-lg-table-cell',
          formatter: (values) => this.$t(values),
        },
        {
          key: 'attempts',
          label: this.$t('date'),
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
