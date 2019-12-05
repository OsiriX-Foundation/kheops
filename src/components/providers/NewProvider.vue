<i18n>
{
  "en": {
    "newprovider": "New provider",
    "nameProvider": "Name of the provider",
    "urlProvider": "Configuration URL of the provider"

  },
  "fr": {
    "newprovider": "Nouveau provider",
    "nameProvider": "Nom du provider",
    "urlProvider": "URL de configuration"
  }
}
</i18n>

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
        {{ $t('newprovider') }}
      </h4>
    </div>
    <form @submit.prevent="createProvider">
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('nameProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <input
            v-model="provider.name"
            v-focus
            type="text"
            :placeholder="$t('nameProvider')"
            class="form-control"
            required
            maxlength="1024"
          >
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('urlProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <div class="input-group mb-3">
            <input
              v-model="provider.url"
              type="text"
              :placeholder="$t('urlProvider')"
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
                :check-u-r-l="checkURL"
                :class-icon="'ml-2 mt-2'"
              />
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="offset-md-3 col-md-9 d-none d-sm-none d-md-block">
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading"
          >
            {{ $t('create') }}
          </button>
          <button
            type="reset"
            class="btn btn-secondary ml-3"
            @click="cancel"
          >
            {{ $t('cancel') }}
          </button>
        </div>
        <div class="col-12 d-md-none">
          <button
            type="submit"
            class="btn btn-primary btn-block"
            :disabled="loading"
          >
            {{ $t('create') }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
import StateProvider from '@/components/providers/StateProvider';

export default {
  name: 'NewProvider',
  components: { StateProvider },
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
      checkURL: false,
    };
  },
  methods: {
    createProvider() {
      this.setStateProvider(false, true, true);
      this.$store.dispatch('postProvider', { query: this.provider, albumID: this.albumID }).then((res) => {
        if (res.status !== 201) {
          this.setStateProvider(false, false, true);
        } else {
          this.$emit('done');
        }
      }).catch((err) => {
        this.setStateProvider(false, false, true);
        console.log(err);
      });
    },
    setStateProvider(checkURL, loading, show) {
      this.checkURL = checkURL;
      this.loading = loading;
      this.show = show;
    },
    cancel() {
      this.$emit('done');
    },
  },
};
</script>
