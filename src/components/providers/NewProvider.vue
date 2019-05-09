<i18n>
{
	"en": {
    "newprovider": "New report provider",
    "nameProvider": "Name of the provider",
    "urlProvider": "Configuration URL of the provider"

	},
	"fr": {
		"newprovider": "Nouveau report provider",
    "nameProvider": "Nom du provider",
    "urlProvider": "Configuration URL of the provider"
	}
}
</i18n>

<template>
  <div>
    <div
      class="my-3 selection-button-container"
      style=" position: relative;"
    >
      <h4>
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
          <input
            v-model="provider.url"
            type="text"
            :placeholder="$t('urlProvider')"
            class="form-control"
            required
            maxlength="1024"
          >
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-12 offset-md-3 col-md-9">
          <button
            type="submit"
            class="btn btn-primary"
          >
            {{ $t('create') }}
          </button><button
            type="reset"
            class="btn btn-secondary ml-3"
            @click="cancel"
          >
            {{ $t('cancel') }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
export default {
	name: 'NewProvider',
	props: {
		albumID: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {
			provider: {
				name: '',
				url: ''
			}
		}
	},
	methods: {
		createProvider () {
			this.$store.dispatch('postProvider', { query: this.provider, albumID: this.albumID }).then(res => {
				if (res.status !== 200) {
					this.$snotify.error('Sorry, an error occured')
				} else {
					this.$emit('done')
				}
			}).catch(err => {
				console.log(err)
			})
		},
		cancel () {
			this.$emit('done')
		}
	}
}
</script>
