<i18n>
{
	"en": {
		"newtoken": "New token",
		"showrevokedtoken": "Show revoked tokens",
		"tokencopysuccess": "Token copied successfully"
	},
	"fr": {
		"newtoken": "Nouveau token",
		"showrevokedtoken": "Afficher les tokens expirés",
		"tokencopysuccess": "Token copié avec succès"
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
		<user-token v-if="view=='token'" :token="token" @done='showList'></user-token>
		
		<div class = 'tokens' v-if="view=='list'">
			<h4>
				Tokens
				<small class = 'float-right'>
					<toggle-button v-model="showRevoked" :labels="{checked: 'Yes', unchecked: 'No'}" @change="getTokens" /><span class = 'ml-3 toggle-label'>{{$t('showrevokedtoken')}}</span>
				</small>
			</h4>
			<b-table stacked="sm" striped hover :items="user.tokens" :fields="fields" :sort-desc="true" :sort-by.sync="sortBy"  @row-clicked='selectToken' tbody-tr-class="link">
				<template slot="title" slot-scope="data">
					<a @click.stop="token=data.item" v-b-modal.tokenModal>{{data.value}}</a>
				</template>
				<template slot='scope_type' slot-scope='data'>
					<div v-if="data.value=='album'"><v-icon name="book" class="mr-3"></v-icon>{{data.item.album.name}}</div>
					<div v-if="data.value=='user'"><v-icon name="user" class="mr-3"></v-icon>{{$t('user')}}</div>
				</template>
				<template slot="expiration_time" slot-scope="data">
					<span :class="(data.item.revoked)?'text-danger':''">{{data.value|formatDate}} <br class='d-lg-none'> <small>{{data.value|formatTime}}</small> </span>
				</template>
				<template slot="not_before_time" slot-scope="data">
					{{data.value|formatDate}} <br class='d-lg-none'> <small>{{data.value|formatTime}}</small>
				</template>
				<template slot="issued_at_time" slot-scope="data">
					{{data.value|formatDate}} <br class='d-lg-none'> <small>{{data.value|formatTime}}</small>
				</template>
				<template slot="permission" slot-scope="data">
					{{data.item|formatPermissions}}
				</template>
				<template slot="actions" slot-scope="data">
					<v-icon name="times" v-if="data.item.revoked" class="text-danger" title="revoked"></v-icon>
				</template>
				
			</b-table>
	</div>
  <b-modal id="tokenModal" ref="tokenModal" centered no-fade hide-header hide-footer size="lg">
    <dl class="my-2 row">
			<dt class="col-xs-12 col-sm-3">{{token.title}}</dt>
			<dd class="col-xs-10 col-sm-8"><input type="text" readonly v-model="token.secret" class="form-control form-control-sm"></dd>
			<div class="col-xs-2 col-sm-1 pointer"><button type="button" class="btn btn-secondary btn-sm" v-clipboard:copy="token.secret" v-clipboard:success="onCopy" v-clipboard:error="onCopyError"><v-icon name="paste" scale="1"></v-icon></button></div>
		</dl>
  </b-modal>
	
		
</div>
</template>

<script>
import Vue from 'vue'
import VueClipboard from 'vue-clipboard2'

VueClipboard.config.autoSetContainer = true // add this line
Vue.use(VueClipboard)
import { mapGetters } from 'vuex'
import newUserToken from '@/components/user/newUserToken'
import userToken from '@/components/user/userToken'
export default{
	name: 'userSettingsToken',
	components: {newUserToken,userToken},
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
					key: 'title',
					label: 'description',
					sortable: true,
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
					key: 'not_before_time',
					label: 'not before date',
					sortable: true,
					class: 'd-none d-md-table-cell'
				},
				{
					key: 'issued_at_time',
					label: 'create date',
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
			this.view='list'
		},
		getTokens () {
			this.$store.dispatch('getUserTokens',{showRevoked: this.showRevoked})
		},
		onCopy () {
			this.$snotify.success(this.$t('tokencopysuccess'))
			this.$refs.tokenModal.hide()
		},
		onCopyError () {
			this.$snotify.error(this.$t('sorryerror'))
			this.$refs.tokenModal.hide()
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
</style>