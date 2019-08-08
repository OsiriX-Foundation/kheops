<template>
  <span>
    <b-dropdown
      v-if="providers.length > 0"
      size="sm"
      variant="link"
      no-caret
      right
    >
      <template slot="button-content">
        <v-icon
          class="align-middle"
          style="margin-right:1"
          name="build"
        />
      </template>
      <b-dropdown-form
        v-for="provider in providers"
        :id="study.StudyInstanceUID.Value[0]"
        :key="provider.id"
        :action="serverURL + '/report'"
        method="post"
      >
        <b-form-input
          v-if="checkProviderModalities(study, provider)"
          type="text"
          hidden
          name="accessToken"
          :value="accessToken"
        />
        <b-form-input
          v-if="checkProviderModalities(study, provider)"
          type="text"
          hidden
          name="client_id"
          :value="provider.client_id"
        />
        <b-form-input
          v-if="checkProviderModalities(study, provider)"
          type="text"
          hidden
          name="studyUID"
          :value="study.StudyInstanceUID.Value[0]"
        />
        <button
          v-if="checkProviderModalities(study, provider)"
          style="cursor: pointer"
          type="submit"
          class="dropdown-item"
        >
          {{ provider.name }}
        </button>
      </b-dropdown-form>
    </b-dropdown>
  </span>
</template>
<script>

import Vue from 'vue'
import { serverURL } from '@/app_config'

export default {
	name: 'IconListProviders',
	components: { },
	mixins: [ ],
	props: {
		providers: {
			type: Array,
			required: true,
			default: () => ([])
		},
		study: {
			type: Object,
			required: true,
			default: () => ({})
		}
	},
	data () {
		return {
			serverURL: serverURL
		}
	},
	computed: {
		accessToken () {
			return Vue.prototype.$keycloak.token
		}
	},

	watch: {
	},

	created () {
	},
	mounted () {
	},
	methods: {
		checkProviderModalities (study, provider) {
			if (provider.data.supported_modalities === undefined) {
				return true
			}
			let result = false
			let modalitiesInStudy = study.ModalitiesInStudy.Value[0].split(',')
			modalitiesInStudy.forEach(modality => {
				if (provider.data.supported_modalities.includes(modality)) {
					result = true
				}
			})

			return result
		}
	}
}
</script>
