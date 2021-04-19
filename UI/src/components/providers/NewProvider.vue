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
        {{ $t('provider.newprovider') }}
      </h4>
    </div>
    <form @submit.prevent="createProvider">
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('provider.nameProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <input
            v-model="provider.name"
            v-focus
            type="text"
            :placeholder="$t('provider.nameProvider')"
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
              :placeholder="$t('provider.urlProvider')"
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
      <create-cancel-button
        :disabled="disabledCreate"
        :loading="oncreate"
        class-col="offset-md-3 col-md-9"
        @cancel="cancel"
      />
    </form>
  </div>
</template>

<script>
import StateProvider from '@/components/providers/StateProvider';
import FieldObligatory from '@/components/globals/FieldObligatory';
import CreateCancelButton from '@/components/globalbutton/CreateCancelButton';
import { validator } from '@/mixins/validator.js';

export default {
  name: 'NewProvider',
  components: { StateProvider, FieldObligatory, CreateCancelButton },
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
      provider: {
        name: '',
        url: '',
      },
      show: false,
      loading: false,
      checkedURL: false,
      oncreate: false,
    };
  },
  computed: {
    disabledCreate() {
      return (this.provider.name === ''
        || this.provider.url === ''
        || !this.checkUrl(this.provider.url))
        || this.loading
        || this.oncreate;
    },
  },
  methods: {
    createProvider() {
      this.setStateProvider(false, true, true);
      this.oncreate = true;
      this.$store.dispatch('postProvider', { query: this.provider, albumID: this.albumID }).then((res) => {
        if (res.status !== 201) {
          this.setStateProvider(false, false, true);
          this.oncreate = false;
        } else {
          this.$emit('done');
        }
      }).catch(() => {
        this.setStateProvider(false, false, true);
        this.oncreate = false;
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
  },
};
</script>
