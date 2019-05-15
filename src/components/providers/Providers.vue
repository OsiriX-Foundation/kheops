<i18n>
	{
		"en": {
			"newprovider": "New report provider"

		},
		"fr": {
			"newprovider": "Nouveau report provider"
		}
	}
</i18n>

<template>
  <div>
    <div
      v-if="view=='list'"
      class="my-3 selection-button-container"
      style=" position: relative;"
    >
      <h4>
        <span
          class="link"
          @click="view='new'"
        >
          <v-icon
            name="plus"
            scale="1"
            class="mr-3"
          />{{ $t('newprovider') }}
        </span>
      </h4>
    </div>

    <new-provider
      v-if="view === 'new'"
      :album-i-d="albumID"
      @done="view='list'"
    />
    <provider
      v-if="view === 'provider'"
      :album-i-d="albumID"
      :client-i-d="clientIdSelected"
      @done="view='list'"
      @providerselectededit="editProvider"
    />
    <edit-provider
      v-if="view === 'edit'"
      :album-i-d="albumID"
      :client-i-d="clientIdSelected"
      @done="view='list'"
    />
    <list-providers
      v-if="view === 'list'"
      :album-i-d="albumID"
      @providerselectedshow="showProvider"
      @providerselectededit="editProvider"
    />
  </div>
</template>

<script>
import NewProvider from '@/components/providers/NewProvider'
import Provider from '@/components/providers/Provider'
import ListProviders from '@/components/providers/ListProviders'
import EditProvider from '@/components/providers/EditProvider'

export default {
	name: 'Providers',
	components: { NewProvider, Provider, ListProviders, EditProvider },
	props: {
		albumID: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {
			view: 'list',
			clientIdSelected: ''
		}
	},
	methods: {
		showProvider (clientId) {
			this.$store.dispatch('initProvider')
			this.clientIdSelected = clientId
			this.view = 'provider'
		},
		editProvider (clientId) {
			this.$store.dispatch('initProvider')
			this.clientIdSelected = clientId
			this.view = 'edit'
		}
	}
}
</script>

<style scoped>
.selection-button-container{
  height: 60px;
}
</style>
