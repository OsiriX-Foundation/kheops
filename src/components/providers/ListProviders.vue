<i18n>
{
	"en": {
    "edit": "Edit",
    "urlWorking": "This provider is on",
    "urlNotWorking": "This provider is off",
		"refresh": "Refresh",
		"created_time": "Created time",
		"name_provider": "Name of provider",
		"user": "User"
	},
	"fr": {
    "edit": "Editer",
    "urlWorking": "Ce provider est accessible",
    "urlNotWorking": "Ce provider n'est pas accessible",
		"refresh": "Rafraîchir",
		"created_time": "Date de création",
		"name_provider": "Nom du provider",
		"user": "Utilisateur"
	}
}
</i18n>
<template>
  <div>
    <div class="d-flex">
      <div>
        <h4>
          Providers
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
      stacked="sm"
      striped
      hover
      :items="providers"
      :fields="fields"
      :sort-desc="true"
      tbody-tr-class="link"
      @row-clicked="selectProvider"
    >
      <template
        slot="url_check"
        slot-scope="data"
      >
        <state-provider
          :loading="data.item.stateURL.loading"
          :check-u-r-l="data.item.stateURL.checkURL"
          :class-icon="'ml-2 mt-1'"
        />
      </template>
      <template
        slot="btn_edit"
        slot-scope="data"
      >
        <button
          class="btn btn-sm btn-primary"
          @click.stop="edit(data.item.client_id)"
        >
          {{ $t('edit') }}
        </button>
      </template>
    </b-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import StateProvider from '@/components/providers/StateProvider'
export default {
	name: 'ListProviders',
	components: { StateProvider },
	props: {
		albumID: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {
			fields: {
				name: {
					label: this.$t('name_provider'),
					sortable: true,
					tdClass: 'breakwork'
				},
				'user.email': {
					label: this.$t('user'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'd-none d-sm-table-cell'
				},
				created_time: {
					label: this.$t('created_time'),
					sortable: true,
					tdClass: 'breakwork',
					class: 'd-none d-md-table-cell'
				},
				btn_edit: {
					label: '',
					sortable: false
				},
				url_check: {
					label: '',
					sortable: false
				}
			}
		}
	},
	computed: {
		...mapGetters({
			providers: 'providers'
		})
	},
	created: function () {
		this.$store.dispatch('getProviders', { albumID: this.albumID }).then(res => {
			if (res.status !== 200) {
				this.$snotify.error('Sorry, an error occured')
			}
		}).catch(err => {
			console.log(err)
		})
	},
	methods: {
		selectProvider (rowSelected) {
			this.$emit('providerselectedshow', rowSelected.client_id)
		},
		edit (clientId) {
			this.$emit('providerselectededit', clientId)
		},
		refresh () {
			this.$store.dispatch('getProviders', { albumID: this.albumID }).then(res => {
				if (res.status !== 200) {
					this.$snotify.error('Sorry, an error occured')
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

