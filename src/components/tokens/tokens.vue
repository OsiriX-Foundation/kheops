<i18n>
	{
		"en": {
			"newtoken": "New token",
			"showrevokedtoken": "Show revoked tokens",
			"showinvalidtoken": "Show invalid tokens",
			"revoke": "revoke",
			"revoked": "revoked",
			"active": "active",
			"expired": "expired",
			"revokedsuccess": "revoked successfully",
			"expiration date": "expiration date",
			"status": "status",
			"description": "description",
			"scope": "scope",
			"create date": "create date",
			"last used": "last used",
			"permission": "permission"

		},
		"fr": {
			"newtoken": "Nouveau token",
			"showrevokedtoken": "Afficher les tokens révoqués",
			"showinvalidtoken": "Afficher les tokens invalides",
			"revoke": "révoquer",
			"revoked": "révoqué",
			"active": "actif",
			"expired": "expiré",
			"revokedsuccess": "révoqué avec succès",
			"expiration date": "date d'expiration",
			"scope": "application",
			"create date": "créé le",
			"last used": "dern. utilisation",
			"permission": "permission"
		}
	}
</i18n>

<template>
  <div class="tokens">
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
          />{{ $t('newtoken') }}
        </span>
      </h4>
    </div>

    <new-token
      v-if="view=='new'"
      :scope="scope"
      :albumid="albumid"
      @done="view='list'"
    />
    <token
      v-if="view=='token'"
      :token="token"
      @done="showList"
      @revoke="revoke"
    />

    <div
      v-if="view=='list'"
      class="tokens"
    >
      <h4>
        Tokens
        <small class="float-right">
          <toggle-button
            v-model="showInvalid"
            :labels="{checked: 'Yes', unchecked: 'No'}"
            @change="getTokens"
          /><span class="ml-2 toggle-label">
            {{ $t('showinvalidtoken') }}
          </span>
        </small>
      </h4>
      <b-table
        stacked="sm"
        striped
        hover
        :items="tokens"
        :fields="fields"
        :sort-desc="true"
        :sort-by.sync="sortBy"
        tbody-tr-class="link"
        @row-clicked="selectToken"
      >
        <template
          slot="scope_type"
          slot-scope="data"
        >
          <div v-if="data.value=='album'">
            <router-link
              :to="`/albums/${data.item.album.id}`"
              @click.stop
            >
              <v-icon
                name="book"
                class="mr-2"
              />{{ data.item.album.name }}
            </router-link>
          </div>
          <div v-if="data.value=='user'">
            <v-icon
              name="user"
              class="mr-2"
            />{{ $t('user') }}
          </div>
        </template>
        <template
          slot="HEAD_expiration_time"
          slot-scope="data"
        >
          {{ $t(data.label) }}
        </template>
        <template
          slot="expiration_time"
          slot-scope="data"
        >
          <span :class="(data.item.revoked)?'text-danger':''">
            {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
          </span>
        </template>
        <template
          slot="HEAD_issued_at_time"
          slot-scope="data"
        >
          {{ $t(data.label) }}
        </template>
        <template
          slot="issued_at_time"
          slot-scope="data"
        >
          {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
        </template>
        <template
          slot="last_used"
          slot-scope="data"
        >
          {{ data.value|formatDate }} <br class="d-lg-none"> <small>{{ data.value|formatTime }}</small>
        </template>
        <template
          slot="HEAD_permission"
          slot-scope="data"
        >
          {{ $t(data.label) }}
        </template>
        <template
          slot="permission"
          slot-scope="data"
        >
          {{ data.item|formatPermissions }}
        </template>
        <template
          slot="status"
          slot-scope="data"
        >
          <div
            v-if="tokenStatus(data.item)=='active'"
            class="text-success"
          >
            <span class="nowrap">
              <v-icon
                name="check-circle"
                class="mr-2"
              />{{ $t("active") }}
            </span>
          </div>
          <div
            v-if="tokenStatus(data.item)=='revoked'"
            class="text-danger"
          >
            <v-icon
              name="ban"
              class="mr-2"
            />{{ $t("revoked") }}<br>{{ data.item.revoke_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.revoke_time|formatTime }}</small>
          </div>
          <div
            v-if="tokenStatus(data.item)=='expired'"
            class="text-danger"
          >
            <v-icon
              name="ban"
              class="mr-2"
            />{{ $t("expired") }}<br>{{ data.item.expiration_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.expiration_time|formatTime }}</small>
          </div>
          <div v-if="tokenStatus(data.item)=='wait'">
            <v-icon
              name="clock"
              class="mr-2"
            /><br>{{ data.item.not_before_time|formatDate }} <br class="d-lg-none"> <small>{{ data.item.not_before_time|formatTime }}</small>
          </div>
        </template>
        <template
          slot="HEAD_status"
          slot-scope="data"
        >
          {{ $t(data.label) }}
        </template>
        <template
          slot="actions"
          slot-scope="data"
        >
          <button
            v-if="!data.item.revoked"
            type="button"
            class="btn btn-danger btn-xs"
            @click.stop="revoke(data.item.id)"
          >
            {{ $t('revoke') }}
          </button>
          <span
            v-if="data.item.revoked"
            class="text-danger"
          >
            {{ $t('revoked') }}
          </span>
        </template>
      </b-table>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import VueClipboard from 'vue-clipboard2'
import { mapGetters } from 'vuex'
import newToken from '@/components/tokens/newToken'
import token from '@/components/tokens/token'
import moment from 'moment'

VueClipboard.config.autoSetContainer = true // add this line
Vue.use(VueClipboard)
export default {
	name: 'Tokens',
	components: { newToken, token },
	props: {
		scope: {
			type: String,
			required: true
		},
		albumid: {
			type: String,
			required: false,
			default: null
		}
	},
	data () {
		return {
			showRevoked: false, // not used anymore...
			showInvalid: false,
			view: 'list',
			sortBy: 'expiration_date',
			token: {
				album: '',
				appropriate_permission: false,
				download_permission: false,
				expiration_time: '',
				id: 0,
				issued_at_time: '',
				not_before_time: '',
				read_permission: false,
				revoked: false,
				scope_type: '',
				access_token: '',
				title: '',
				write_permission: false
			},
			fields: [
				{
					key: 'status',
					label: 'status',
					sortable: true
				},
				{
					key: 'title',
					label: 'description',
					sortable: true
				},
				{
					key: 'scope_type',
					label: 'scope',
					sortable: true
				},
				{
					key: 'expiration_time',
					label: 'expiration date',
					sortable: true,
					class: 'd-none d-sm-table-cell'
				},
				{
					key: 'issued_at_time',
					label: 'create date',
					sortable: true,
					class: 'd-none d-md-table-cell'
				},
				{
					key: 'last_used',
					label: 'last used',
					sortable: true,
					class: 'd-none d-md-table-cell'
				},
				{
					key: 'permission',
					label: 'permission',
					sortable: true,
					class: 'd-none d-sm-table-cell'
				},
				{
					key: 'actions',
					label: '',
					sortable: false
				}
			]
		}
	},
	computed: {
		...mapGetters({
			user: 'currentUser',
			albumTokens: 'albumTokens'
		}),
		tokens () {
			let tokens = []
			if (this.scope === 'user') {
				tokens = this.user.tokens
			} else if (this.scope === 'album') {
				tokens = this.albumTokens
			}
			return tokens
		}
	},
	created () {
		if (this.scope === 'album') {
			let scopeColIdx = _.findIndex(this.fields, f => { return f.key === 'scope_type' })
			if (scopeColIdx > -1) this.fields.splice(scopeColIdx, 1)
		}
		this.getTokens()
	},
	methods: {
		selectToken (item) {
			this.token = item
			this.view = 'token'
		},
		showList () {
			this.token = {
				album: '',
				appropriate_permission: false,
				download_permission: false,
				expiration_time: '',
				id: 0,
				issued_at_time: '',
				not_before_time: '',
				read_permission: false,
				revoked: false,
				scope_type: '',
				access_token: '',
				title: '',
				write_permission: false
			}
			this.view = 'list'
		},
		getTokens () {
      let queries = {
        valid: !this.showInvalid, 
        album: this.albumid
      }
			if (this.scope === 'album' && this.albumid) {
        let queries = {
          valid: !this.showInvalid, 
          album: this.albumid
        }
				this.$store.dispatch('getAlbumTokens', { queries: queries })
			} else if (this.scope === 'user') {
				this.$store.dispatch('getUserTokens', { showInvalid: this.showInvalid, album_id: this.albumid })
			}
		},
		revoke (tokenId) {
			this.$store.dispatch('revokeToken', { token_id: tokenId }).then((res) => {
				this.$snotify.success(`token ${res.data.title} ${this.$t('revokedsuccess')}`)
				this.getTokens()
			}).catch(() => {
				this.$snotify.error(this.$t('sorryerror'))
			})
		},
		tokenStatus (token) {
			if (token.revoked) {
				return 'revoked'
			} else if (moment(token.not_before_time) > moment()) {
				return 'wait'
			} else if (moment(token.expiration_time) < moment()) {
				return 'expired'
			}	else {
				return 'active'
			}
		}
	}
}
</script>

<style scoped>
.selection-button-container{
height: 60px;
}
.toggle-label{
vertical-align: top;
}
dt{
text-align: right;
}

tr:hover {
visibility: visible;
}

</style>
