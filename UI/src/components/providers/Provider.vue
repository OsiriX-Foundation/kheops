<template>
  <div v-if="Object.keys(provider).length > 0">
    <div
      class="my-3 provider-position"
    >
      <h4
        class="row word-break mb-3"
      >
        <button
          type="button"
          class="btn btn-link btn-sm d-md-none"
          @click.stop="back"
        >
          <span>
            <v-icon
              name="arrow-left"
              color="white"
            />
          </span>
        </button>
        <span>
          {{ provider.name }}
        </span>
      </h4>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('provider.url') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.url }}
          </dd>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('provider.stateurl') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <div class="d-inline-flex">
            <state-provider
              :loading="provider.stateURL.loading"
              :check-u-r-l="provider.stateURL.checkURL"
            />
          </div>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('provider.clientid') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.client_id }}
          </dd>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('provider.created_time') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.created_time|formatDateTimeDetails }}
          </dd>
        </div>
      </div>
      <span
        v-if="provider.data"
      >
        <div
          v-if="provider.data.client_name"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.client_name') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              {{ provider.data.client_name }}
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.client_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.client_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.client_uri">
                {{ provider.data.client_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.logo_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.logo_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.logo_uri">
                {{ provider.data.logo_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.contacts"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.contacts') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <li
              v-for="contact in provider.data.contacts"
              :key="contact.id"
            >
              <a :href="`mailto:${contact}`">
                {{ contact }}
              </a>
            </li>
          </div>
        </div>
        <div
          v-if="provider.data.tos_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.tos_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.tos_uri">
                {{ provider.data.tos_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.policy_uri"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.policy_uri') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <a :href="provider.data.policy_uri">
                {{ provider.data.policy_uri }}
              </a>
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.software_version"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.software_version') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              {{ provider.data.software_version }}
            </dd>
          </div>
        </div>
        <div
          v-if="provider.data.supported_modalities"
          class="row mb-2"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('provider.supported_modalities') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <li
                v-for="modality in provider.data.supported_modalities"
                :key="modality.id"
              >
                {{ modality }}
              </li>
            </dd>
          </div>
        </div>
      </span>
    </div>
    <done-delete-button
      v-if="writePermission"
      class-row="mb-2"
      class-col="offset-md-3 col-12 col-sm-12 col-md-3"
      class-col-warning-remove="offset-md-3 col-sm-12 col-md-9"
      :text-warning-remove="$t('provider.warningremove')"
      :text-button-done="$t('edit')"
      :loading="onloading"
      @done="edit"
      @remove="deleteProvider"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import StateProvider from '@/components/providers/StateProvider';
import DoneDeleteButton from '@/components/globalbutton/DoneDeleteButton';

export default {
  name: 'Provider',
  components: { StateProvider, DoneDeleteButton },
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
      confirmDelete: false,
      onloading: false,
    };
  },
  computed: {
    ...mapGetters({
      provider: 'provider',
    }),
    clientID() {
      return this.$route.params.id;
    },
  },
  created() {
    this.$store.dispatch('getProvider', { albumID: this.albumID, clientID: this.clientID }).then((res) => {
      if (res.status !== 200) {
        this.$snotify.error('Sorry, an error occurred');
        this.back();
      }
    }).catch((err) => {
      if (err.response !== undefined && err.response.status === 404) {
        this.$snotify.error('Report provider not found');
      } else {
        this.$snotify.error('Sorry, an error occurred');
      }
      this.back();
    });
  },
  methods: {
    back() {
      this.$emit('done');
    },
    edit() {
      this.$emit('providerselectededit', this.clientID);
    },
    deleteProvider() {
      this.onloading = true;
      this.$store.dispatch('deleteProvider', { albumID: this.albumID, clientID: this.clientID }).then((res) => {
        if (res.status !== 204) {
          this.onloading = false;
          this.$snotify.error('Sorry, an error occurred');
        } else {
          this.$emit('done');
        }
      }).catch(() => {
        this.onloading = false;
      });
    },
  },
};
</script>
