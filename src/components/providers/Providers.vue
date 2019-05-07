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
      v-if="view=='new'"
      :album-i-d="albumID"
      @done="view='list'"
    />

    <div
      v-if="view=='list'"
      class="tokens"
    >
      <h4>
        Report Providers
      </h4>
      <b-table
        stacked="sm"
        striped
        hover
        :items="providers"
        :sort-desc="true"
        tbody-tr-class="link"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import NewProvider from '@/components/providers/NewProvider'

export default {
	name: 'Providers',
	components: { NewProvider },
	props: {
		albumID: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {
			view: 'list'
		}
	},
	computed: {
		...mapGetters({
			providers: 'providers'
		})
	}
}
</script>
