<i18n>
{
	"en": {
		"newtoken": "New token",
		"description": "description",
		"scope": "scope",
		"album": "album",
		"permission": "permission",
		"write": "write",
		"read": "read",
		"download": "download",
		"appropriate": "appropriate",
		"expirationdate": "expiration date"
	},
	"fr": {
		"newtoken": "Nouveau token",
		"description": "description",
		"scope": "applicable à",
		"album": "album",
		"permission": "permission",
		"write": "écriture",
		"read": "lecture",
		"download": "téléchargement",
		"appropriate": "approprier",
		"expirationdate": "date d'expiration"
	}
}
</i18n>

<template>
	<div id = 'newUserToken'>
		<div class="my-3 selection-button-container" style = ' position: relative;'>
			<h4>
				{{$t('newtoken')}}
			</h4>
		</div>
		<form @submit.prevent="createToken">
			<fieldset>
				<div class = 'row'>
					<div class = 'col-xs-12 col-sm-3'><dt>{{$t('description')}}</dt></div>
					<div class = 'col-xs-12 col-sm-9'>
						<dd>
							<input type = 'text' v-model='token.title' :placeholder="$t('description')" class = 'form-control' required>
						</dd>
					</div>
				</div>
				<div class = 'row'>
					<div class = 'col-xs-12 col-sm-3'><dt>{{$t('scope')}}</dt></div>
					<div class = 'col-xs-12 col-sm-9'>
						<dd>
							<select class = 'form-control' v-model='token.scope_type'>
								<option v-for="(scope,idx) in scopes" :key="idx" :value='scope'>{{$t(scope)}}</option>
							</select>
						</dd>
					</div>
				</div>
				<div class = 'row' v-if="token.scope_type=='album'">
					<div class = 'col-xs-12 col-sm-3'><dt>{{$t('album')}}</dt></div>
					<div class = 'col-xs-12 col-sm-9'>
						<dd>
							<select class = 'form-control' v-model='token.album'>
								<option v-for="album in albums" :key="album.album_id" :value='album.album_id'>{{album.name}}</option>
							</select>
						</dd>
					</div>
				</div>
				<div class = 'row' v-if="token.scope_type=='album'">
					<div class = 'col-xs-12 col-sm-3'><dt>{{$t('permission')}}</dt></div>
					<div class = 'col-xs-12 col-sm-9'>
						<dd>
							<toggle-button v-model="token.write_permission" :labels="{checked: 'Yes', unchecked: 'No'}" /> <label>{{$t('write')}}</label><br>
							<toggle-button v-model="token.read_permission" :labels="{checked: 'Yes', unchecked: 'No'}" /> <label>{{$t('read')}}</label><br>
							<toggle-button v-model="token.download_permission" :labels="{checked: 'Yes', unchecked: 'No'}" class = 'ml-3' v-if='token.read_permission' /> <label  v-if='token.read_permission'>{{$t('download')}}</label><br>
							<toggle-button v-model="token.appropriate_permission" :labels="{checked: 'Yes', unchecked: 'No'}" class = 'ml-3' v-if='token.read_permission' /> <label  v-if='token.read_permission'>{{$t('appropriate')}}</label>
						</dd>
					</div>
				</div>
				<div class = 'row'>
					<div class = 'col-xs-12 col-sm-3'><dt>{{$t('expirationdate')}}</dt></div>
					<div class = 'col-xs-12 col-sm-3'>
						<dd>
							<datepicker v-model="token.expiration_time"  :bootstrap-styling='false' input-class="form-control form-control-sm  search-calendar" :calendar-button="false" calendar-button-icon=""  wrapper-class='calendar-wrapper' :placeholder="$t('expirationdate')" :clear-button="true" clear-button-icon='fa fa-times'></datepicker>
						</dd>
					</div>
				</div>
				<div class = 'row'>
					<div class = 'col-xs-12 offset-sm-3 col-sm-9'><button type = 'submit' class = 'btn btn-primary' :disabled="!token.title || (token.scope_type==='album' && !token.album)">{{$t('create')}}</button><button type = 'reset' class = 'btn btn-secondary ml-3' @click="cancel">{{$t('cancel')}}</button></div>
				</div>
			</fieldset>
		</form>
	</div>
</template>

<script>
import moment from 'moment'
import Datepicker from 'vuejs-datepicker'
import { mapGetters } from 'vuex'
export default {
	name: 'newUserToken',
	components: { Datepicker },
	data () {
		return {
			token: {
				title: '',
				scope_type: 'user',
				album: '',
				read_permission: false,
				write_permission: false,
				appropriate_permission: false,
				download_permission: false,
				not_before_time: moment().toDate(),
				expiration_time: moment().add(7, 'days').toDate()
			},
			scopes: ['user', 'album']
		}
	},
	computed: {
		...mapGetters({
			albums: 'albums'
		})
	},
	methods: {
		createToken () {
			if (this.token.scope_type !== 'album') {
				this.token.read_permission = false
				this.token.write_permission = false
			}
			if (!this.token.read_permission) {
				this.token.download_permission = false
				this.token.appropriate_permission = false
			}
			let token = this.token
			token.expiration_time = moment(this.token.expiration_time).format()
			token.not_before_time = moment(this.token.not_before_time).format()
			this.$store.dispatch('createToken', { token: token }).then(() => {
				this.$snotify.success('token created successfully')
				this.$emit('done')
			}).catch(() => {
				this.$snotify.error(this.$t('sorryerror'))
			})
		},
		cancel () {
			this.$emit('done')
		}
	},
	created () {
		this.$store.dispatch('getAlbums', { pageNb: 1, limit: 40, sortBy: 'created_time', sortDesc: true })
	}
}
</script>

<style scoped>
dt{
	text-align: right;
	text-transform: capitalize;
}
label{
	text-transform: capitalize;
	margin-left: 1em;
}
div.calendar-wrapper{
	color: #333;
}

</style>
