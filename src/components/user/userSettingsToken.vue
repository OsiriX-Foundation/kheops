<i18n>
	{
		"en": {
			"newtoken": "New token",
			"showrevokedtoken": "Show revoked tokens",
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
			"showrevokedtoken": "Afficher les tokens expirés",
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
	<div id = 'userSettingsToken'>
		<div class="my-3 selection-button-container" style = ' position: relative;' v-if="view=='list'">
			<h4>
				<span class = 'link' @click="view='new'"><v-icon name = 'plus' scale='1' class='mr-3'></v-icon>{{$t('newtoken')}}</span>
			</h4>
		</div>

		<new-user-token v-if="view=='new'" @done="view='list'"></new-user-token>
		<user-token v-if="view=='token'" :token="token" @done='showList' @revoke='revoke'></user-token>

		<div class = 'tokens' v-if="view=='list'">
			<h4>
				Tokens
				<small class = 'float-right'>
					<toggle-button v-model="showRevoked" :labels="{checked: 'Yes', unchecked: 'No'}" @change="getTokens" /><span class = 'ml-2 toggle-label'>{{$t('showrevokedtoken')}}</span>
				</small>
			</h4>
			<b-table stacked="sm" striped hover :items="user.tokens" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"  @row-clicked='selectToken' tbody-tr-class="link">
			<template slot='scope_type' slot-scope='data'>
				<div v-if="data.value=='album'"><router-link :to="`/albums/${data.item.album.id}`" @click.stop ><v-icon name="book" class="mr-2"></v-icon>{{data.item.album.name}}</router-link></div>
				<div v-if="data.value=='user'"><v-icon name="user" class="mr-2"></v-icon>{{$t('user')}}</div>
			</template>
			<template slot="HEAD_expiration_time" slot-scope="data">
				{{$t(data.label)}}
			</template>
			<template slot="expiration_time" slot-scope="data">
				<span :class="(data.item.revoked)?'text-danger':''">{{data.value|formatDate}} <br class='d-lg-none'> <small>{{data.value|formatTime}}</small> </span>
			</template>
			<template slot="HEAD_issued_at_time" slot-scope="data">
				{{$t(data.label)}}
			</template>
			<template slot="issued_at_time" slot-scope="data">
				{{data.value|formatDate}} <br class='d-lg-none'> <small>{{data.value|formatTime}}</small>
			</template>
			<template slot="HEAD_permission" slot-scope="data">
				{{$t(data.label)}}
			</template>
			<template slot="permission" slot-scope="data">
				{{data.item|formatPermissions}}
			</template>
			<template slot="status" slot-scope="data">
				<div class="text-success" v-if="tokenStatus(data.item)=='active'"><span class="nowrap"><v-icon name="check-circle" class='mr-2'></v-icon>{{$t("active")}}</span></div>
				<div class="text-danger" v-if="tokenStatus(data.item)=='revoked'"><v-icon name="ban" class="mr-2"></v-icon>{{$t("revoked")}}<br>{{data.item.revoke_time|formatDate}} <br class='d-lg-none'> <small>{{data.item.revoke_time|formatTime}}</small></div>
				<div class="text-danger" v-if="tokenStatus(data.item)=='expired'"><v-icon name="ban" class="mr-2"></v-icon>{{$t("expired")}}<br>{{data.item.expiration_time|formatDate}} <br class='d-lg-none'> <small>{{data.item.expiration_time|formatTime}}</small></div>
				<div v-if="tokenStatus(data.item)=='wait'"><v-icon name="clock" class="mr-2"></v-icon><br>{{data.item.not_before_time|formatDate}} <br class='d-lg-none'> <small>{{data.item.not_before_time|formatTime}}</small></div>
			</template>
			<template slot="HEAD_status" slot-scope="data">
				{{$t(data.label)}}
			</template>
			<template slot="actions" slot-scope="data">
				<button type="button" class="btn btn-danger btn-xs revoke-btn" v-if="!data.item.revoked" @click.stop="revoke(data.item.id)">{{$t('revoke')}}</button>
				<span class="text-danger revoke-btn" v-if="data.item.revoked">{{$t('revoked')}}</span>
			</template>

		</b-table>
	</div>


</div>
</template>

<script>
import Vue from 'vue'
import VueClipboard from 'vue-clipboard2'
import { mapGetters } from 'vuex'
import newUserToken from '@/components/user/newUserToken'
import userToken from '@/components/user/userToken'
import moment from 'moment'

VueClipboard.config.autoSetContainer = true // add this line
Vue.use(VueClipboard)
export default {
	name: 'userSettingsToken',
	components: { newUserToken, userToken },
	data () {
		return {
			showRevoked: false,
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
				secret: '',
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
					sortable: true
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
					sortable: true
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
			albums: 'albums'
		})
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
				secret: '',
				title: '',
				write_permission: false
			}
			this.view = 'list'
		},
		getTokens () {
			this.$store.dispatch('getUserTokens', { showRevoked: this.showRevoked })
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
	},
	created () {
		this.getTokens()
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
.revoke-btn{
visibility: hidden;
}

tr:hover .revoke-btn{
visibility: visible;
}

</style>
