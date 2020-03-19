<template>
  <div>
    <div class="d-flex">
      <div>
        <h4>
          Report Providers
        </h4>
      </div>
      <div class="ml-auto">
        <button
          class="btn btn-sm btn-primary"
          @click.stop="refresh()"
        >
          {{ $t('refresh') }}
        </button>
      </div>
    </div>
    <b-table
      striped
      hover
      show-empty
      :items="providers"
      :fields="fields"
      :sort-desc="true"
      :busy="loadingData"
      tbody-tr-class="link"
      @row-clicked="selectProvider"
    >
      <template
        v-slot:cell(url_check)="data"
      >
        <state-provider
          :loading="data.item.stateURL.loading"
          :check-u-r-l="data.item.stateURL.checkURL"
          :class-icon="'ml-2 mt-1'"
        />
      </template>
      <template
        v-if="writePermission"
        v-slot:cell(btn_edit)="data"
      >
        <button
          class="btn btn-sm btn-primary"
          @click.stop="edit(data.item.client_id)"
        >
          {{ $t('edit') }}
        </button>
      </template>
      <template v-slot:table-busy>
        <loading />
      </template>
      <template v-slot:empty>
        <div
          class="text-warning text-center"
        >
          <list-empty
            :status="status"
            :text-empty="$t('provider.noreports')"
            @reload="getProviders()"
          />
        </div>
      </template>
      <template v-slot:emptyfiltered>
        <div
          class="text-warning text-center"
        >
          {{ $t('provider.noreports') }}
        </div>
      </template>
    </b-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import Loading from '@/components/globalloading/Loading';
import StateProvider from '@/components/providers/StateProvider';
import ListEmpty from '@/components/globallist/ListEmpty';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'ListProviders',
  components: { StateProvider, Loading, ListEmpty },
  props: {
    albumID: {
      type: String,
      required: true,
      default: '',
    },
    writePermission: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  data() {
    return {
      status: -1,
      loadingData: false,
      fields: [
        {
          key: 'url_check',
          label: '',
          sortable: false,
        },
        {
          key: 'name',
          label: this.$t('provider.name_provider'),
          sortable: true,
          tdClass: 'word-break',
        },
        {
          key: 'url',
          label: this.$t('provider.url'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-sm-table-cell',
        },
        {
          key: 'created_time',
          label: this.$t('provider.created_time'),
          sortable: true,
          tdClass: 'word-break',
          class: 'd-none d-lg-table-cell',
          formatter: (createdTime) => {
            if (createdTime !== undefined) {
              return this.$options.filters.formatDateTimeDetails(createdTime);
            }
            return '';
          },
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
      providers: 'providers',
    }),
  },
  created() {
    this.getProviders();
  },
  methods: {
    getProviders() {
      this.loadingData = true;
      return this.$store.dispatch('getProviders', { albumID: this.albumID }).then((res) => {
        this.loadingData = false;
        this.status = -1;
      }).catch((err) => {
        this.loadingData = false;
        this.status = httpoperations.getStatusError(err);
      });
    },
    selectProvider(rowSelected) {
      this.$emit('providerselectedshow', rowSelected.client_id);
    },
    edit(clientId) {
      this.$emit('providerselectededit', clientId);
    },
    refresh() {
      this.getProviders();
    },
  },
};
</script>
