<i18n>
{
	"en": {
    "editprovider": "Edit provider",
    "nameProvider": "Name of the provider",
    "urlProvider": "Configuration URL of the provider",
    "newClientId": "Generate a new client ID",
    "edit": "Confirm",
    "remove": "Remove"
	},
	"fr": {
		"editprovider": "Edition d'un provider",
    "nameProvider": "Nom du provider",
    "urlProvider": "URL de configuration",
    "newClientId": "Generer un nouveau client ID",
    "edit": "Confirmer",
    "remove": "Supprimer"
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
        {{ $t('editprovider') }}
      </h4>
    </div>
    <form @submit.prevent="updateProvider">
      <div class="row">
        <div class="col-xs-12 col-sm-12 col-md-3">
          <b>{{ $t('nameProvider') }}</b>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <input
            v-model="provider.name"
            type="text"
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
      <div
        class="row"
      >
        <div class="col-xs-12 col-sm-12 col-md-3">
          <dt>{{ $t('newClientId') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-9 mb-3">
          <dd>
            <toggle-button
              v-model="newClientId"
              :labels="{checked: 'Yes', unchecked: 'No'}"
            />
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-12 offset-md-3 col-md-9">
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading"
          >
            {{ $t('edit') }}
          </button>
          <button
            type="button"
            class="btn btn-danger ml-3"
            @click="deleteProvider"
          >
            {{ $t('remove') }}
          </button>
          <button
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
import { mapGetters } from 'vuex'
import StateProvider from '@/components/providers/StateProvider'

export default {
	name: 'EditProvider',
	components: { StateProvider },
	props: {
		albumID: {
			type: String,
			required: true,
			default: ''
		},
		clientID: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {
			newClientId: false,
			show: false,
			checkURL: false,
			loading: false
		}
	},
	computed: {
		...mapGetters({
			provider: 'provider'
		})
	},
	created: function () {
		this.$store.dispatch('getProvider', { albumID: this.albumID, clientID: this.clientID }).then(res => {
			if (res.status !== 200) {
				this.$snotify.error('Sorry, an error occured')
			}
		}).catch(err => {
			console.log(err)
		})
	},
	methods: {
		updateProvider () {
			this.setStateProvider(false, true, true)
			const paramsURL = {
				albumID: this.albumID,
				clientID: this.clientID
			}
			const query = {
				name: this.provider.name,
				url: this.provider.url,
				new_client_id: this.newClientId
			}

			this.$store.dispatch('updateProvider', { paramsURL, query }).then(res => {
				if (res.status !== 200) {
					this.setStateProvider(false, false, true)
				} else {
					this.$snotify.success('Provider updated')
					this.$emit('done')
				}
			}).catch(err => {
				this.setStateProvider(false, false, true)
				console.log(err)
			})
		},
		setStateProvider (checkURL, loading, show) {
			this.checkURL = checkURL
			this.loading = loading
			this.show = show
		},
		cancel () {
			this.$emit('done')
		},
		deleteProvider () {
			this.$store.dispatch('deleteProvider', { albumID: this.albumID, clientID: this.clientID }).then(res => {
				if (res.status !== 204) {
					this.$snotify.error('Sorry, an error occured')
				} else {
					this.$snotify.success('Provider remove')
					this.$emit('done')
				}
			}).catch(err => {
				console.log(err)
			})
		}
	}
}
</script>
