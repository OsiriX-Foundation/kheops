<template>
  <div>
    <div
      class="my-3 selection-button-container provider-position"
    >
      <h4>
        <button
          type="button"
          class="btn btn-link btn-sm d-md-none"
          @click.stop="cancel"
        >
          <span>
            <v-icon
              name="arrow-left"
              color="white"
            />
          </span>
        </button>
        {{ $t('provider.editprovider') }}
      </h4>
    </div>
    <form @submit.prevent="updateProvider">
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('provider.nameProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <input
            v-model="provider.name"
            type="text"
            class="form-control"
            required
            maxlength="1024"
          >
          <field-obligatory
            :state="provider.name !== ''"
          />
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('provider.urlProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <div class="input-group mb-3">
            <input
              v-model="provider.url"
              type="text"
              class="form-control"
              required
              maxlength="1024"
            >

            <div
              v-if="show"
              class="input-group-append"
            >
              <state-provider
                :loading="loading"
                :check-u-r-l="checkedURL"
                :class-icon="'ml-2 mt-2'"
              />
            </div>
            <field-obligatory
              :state="provider.url !== ''"
            />
            <field-obligatory
              v-if="provider.url !== ''"
              :state="checkUrl(provider.url)"
              :text="$t('urlnotvalid')"
            />
          </div>
        </div>
      </div>
      <done-delete-button
        class-row="mb-2"
        class-col="offset-md-3 col-12 col-sm-12 col-md-3"
        class-col-warning-remove="offset-md-3 col-sm-12 col-md-9"
        :text-warning-remove="$t('provider.warningremove')"
        :disabled-done="disabledCreate"
        :loading="onloading"
        @remove="deleteProvider"
      />
    </form>
  </div>
</template>
<script>
import { mapGetters } from 'vuex';
import StateProvider from '@/components/providers/StateProvider';
import FieldObligatory from '@/components/globals/FieldObligatory';
import DoneDeleteButton from '@/components/globalbutton/DoneDeleteButton';
import { validator } from '@/mixins/validator.js';

export default {
  name: 'EditProvider',
  components: { StateProvider, FieldObligatory, DoneDeleteButton },
  mixins: [validator],
  props: {
    albumID: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      show: false,
      checkedURL: false,
      loading: false,
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
    disabledCreate() {
      return (this.provider.name === ''
        || this.provider.url === ''
        || !this.checkUrl(this.provider.url))
        || this.loading
        || this.onloading;
    },
  },
  created() {
    this.$store.dispatch('getProvider', { albumID: this.albumID, clientID: this.clientID }).then((res) => {
      if (res.status !== 200) {
        this.$snotify.error('Sorry, an error occurred');
        this.cancel();
      }
    }).catch((err) => {
      if (err.response !== undefined && err.response.status === 404) {
        this.$snotify.error('Report provider not found');
      } else {
        this.$snotify.error('Sorry, an error occurred');
      }
      this.cancel();
    });
  },
  methods: {
    updateProvider() {
      this.setStateProvider(false, true, true);
      this.onloading = true;
      const paramsURL = {
        albumID: this.albumID,
        clientID: this.clientID,
      };
      const query = {
        name: this.provider.name,
        url: this.provider.url,
      };

      this.$store.dispatch('updateProvider', { paramsURL, query }).then((res) => {
        if (res.status !== 200) {
          this.setStateProvider(false, false, true);
          this.onloading = false;
        } else {
          this.$emit('done');
        }
      }).catch(() => {
        this.setStateProvider(false, false, true);
        this.onloading = false;
      });
    },
    setStateProvider(checkURL, loading, show) {
      this.checkedURL = checkURL;
      this.loading = loading;
      this.show = show;
    },
    cancel() {
      this.$emit('done');
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
