<i18n>
{
	"en": {
    "edit": "Edit"
	},
	"fr": {
    "edit": "Editer"
	}
}
</i18n>
<template>
  <div>
    <h4>
    Report Providers
    </h4>
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
        <div
          v-if="true"
          class="text-success"
        >
          <span class="nowrap">
            <v-icon
              name="check-circle"
              class="mr-2"
            />
          </span>
        </div>
        <div
          class="text-danger"
          v-else
        >
          <v-icon
            name="ban"
            class="mr-2"
          />
        </div>
      </template>
      <template
        slot="btn_edit"
        slot-scope="data"
      >
        <button
          class="btn btn-primary"
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
      fields: {
        name: {
          label: 'Name of provider',
          sortable: true
        },
        client_id: {
          label: 'Id du client',
          sortable: true,
					class: 'd-none d-lg-table-cell'
        },
        'user.email': {
          label: 'User',
          sortable: true,
					class: 'd-none d-sm-table-cell'
        },
        created_time: {
          label: 'Created time',
          sortable: true,
					class: 'd-none d-md-table-cell'
        },
        btn_edit : {
          label: '',
          sortable: false
        },
        url_check : {
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
    edit(clientId) {
      this.$emit('providerselectededit', clientId)
    },
    checkURL(url) {
      this.$store.dispatch('testURLProvider', { url: url }).then(res => {
        if (res.status !== 200) {
          return false
        }
        return true
      }).catch(err => {
        return false
      })
    }
	}
}
</script>
