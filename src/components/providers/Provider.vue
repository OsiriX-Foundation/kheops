<i18n>
	{
		"en": {
			"url": "URL provider",
      "stateurl": "Access provider",
      "user": "User",
      "clientid": "Client ID",
      "created_time": "Created time",
      "edit": "Edit",
      "remove": "Remove",
      "back": "Back"
		},
		"fr": {
			"url": "URL de configuration",
      "stateurl": "Etat du provider",
      "user": "Utilisateur",
      "clientid": "Identifiant du provider",
      "created_time": "Date de création",
      "edit": "Editer",
      "remove": "Supprimer",
      "back": "Retour"
		}
	}
</i18n>
<template>
  <div v-if="Object.keys(provider).length > 0">
    <div
      class="my-3 selection-button-container"
      style=" position: relative;"
    >
      <h4>
        <span
          class="breakwork"
        >
          {{ provider.name }}
        </span>
      </h4>

      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('url') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.url }}
          </dd>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('stateurl') }}</dt>
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
          <dt>{{ $t('clientid') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.client_id }}
          </dd>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('created_time') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.created_time }}
          </dd>
        </div>
      </div>
    </div>
    <div class="row mb-2">
      <div class="col-xs-12 col-sm-12 offset-md-3 col-md-9">
        <button
          v-if="writePermission"
          class="btn btn-primary"
          @click.stop="edit()"
        >
          {{ $t('edit') }}
        </button>
        <button
          v-if="writePermission"
          type="button"
          class="btn btn-danger ml-3"
          @click="deleteProvider"
        >
          {{ $t('remove') }}
        </button>
        <button
          type="submit"
          class="btn btn-secondary"
          :class="writePermission ? 'ml-3': ''"
          @click="back"
        >
          {{ $t('back') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import StateProvider from '@/components/providers/StateProvider'
export default {
	name: 'Provider',
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
		},
		writePermission: {
			type: Boolean,
			required: true,
			default: false
		}
	},
	data () {
		return {
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
		back () {
			this.$emit('done')
		},
		edit () {
			this.$emit('providerselectededit', this.clientID)
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
<style>
	.breakwork {
		word-break: break-word;
	}
</style>
