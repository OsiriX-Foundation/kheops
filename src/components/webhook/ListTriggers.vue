<i18n>
{
  "en": {
    "new_series": "new serie",
    "new_user": "new user",
    "activate": "Trigger by adding {event}",
    "manualtrigger": "Manual trigger",
    "attempt":"Attempt",
    "status": "Status",
    "date": "Date",
    "event": "Event",
    "-1": "An error occur",
    "deliveries": "Recent attempts"
  },
  "fr": {
    "new_series": "nouvelles série",
    "new_user": "nouvel utilisateur",
    "activate": "Déclenché par l'ajout de {event}",
    "manualtrigger": "Déclenchement manuel",
    "attempt":"Tentative",
    "status": "Status",
    "date": "Date",
    "event": "Evènement",
    "-1": "Une erreur est survenue",
    "deliveries": "Tentatives récentes"
  }
}
</i18n>
<template>
  <span>
    <div
      class="my-3"
    >
      <div>
        <h4>
          {{ $t('deliveries') }}
        </h4>
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
          <p>{{ $t('activate', {event: $t(row.item.event)}) }}</p>
          <p
            v-if="row.item.is_manual_trigger === true"
          >
            {{ $t('manualtrigger') }}
          </p>
          <span
            class="d-flex flex-wrap flex-row bd-highlight mb-3"
          >
            <span
              v-for="attempt in row.item.attempts"
              :key="attempt.id"
              class="p-2 bd-highlight"
            >
              <b>{{ $t('attempt') }} {{ attempt.attempt }}</b> <br>
              {{ $t('status') }}: {{ $t(attempt.status) }}<br>
              {{ $t('date') }}: {{ attempt.time | formatDateTimeDetails }}<br>
              <br>
            </span>
          </span>
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
      />
    </div>
  </span>
</template>

<script>

export default {
  name: 'ListTriggers',
  components: { },
  props: {
    triggers: {
      type: Array,
      required: true,
      default: () => [],
    },
  },
  data() {
    return {
      perPage: 10,
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
    rows() {
      return this.triggers.length;
    }
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
  },
};
</script>
