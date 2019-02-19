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
		"expirationdate": "expiration date",
		"tokencopysuccess": "Token copied successfully"

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
		"expirationdate": "date d'expiration",
		"tokencopysuccess": "Token copié avec succès"
	}
}
</i18n>

<template>
  <div id="newToken">
    <div
      class="my-3 selection-button-container"
      style=" position: relative;"
    >
      <h4>
        {{ $t('newtoken') }}
      </h4>
    </div>
    <form @submit.prevent="createToken">
      <fieldset>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('description') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <input
                v-model="token.title"
                type="text"
                :placeholder="$t('description')"
                class="form-control"
                required
              >
            </dd>
          </div>
        </div>
        <div
          v-if="scope!=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('scope') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <select
                v-model="token.scope_type"
                class="form-control"
              >
                <option
                  v-for="(option_scope,idx) in scopes"
                  :key="idx"
                  :value="option_scope"
                >
                  {{ $t(option_scope) }}
                </option>
              </select>
            </dd>
          </div>
        </div>
        <div
          v-if="token.scope_type==='album' && scope!=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('album') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <select
                v-model="token.album"
                class="form-control"
              >
                <option
                  v-for="album in albums"
                  :key="album.album_id"
                  :value="album.album_id"
                >
                  {{ album.name }}
                </option>
              </select>
            </dd>
          </div>
        </div>
        <div
          v-if="token.scope_type=='album'"
          class="row"
        >
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('permission') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-9">
            <dd>
              <toggle-button
                v-model="token.write_permission"
                :labels="{checked: 'Yes', unchecked: 'No'}"
              /> <label>{{ $t('write') }}</label><br>
              <toggle-button
                v-model="token.read_permission"
                :labels="{checked: 'Yes', unchecked: 'No'}"
              /> <label>{{ $t('read') }}</label><br>
              <toggle-button
                v-if="token.read_permission"
                v-model="token.download_permission"
                :labels="{checked: 'Yes', unchecked: 'No'}"
                class="ml-3"
              /> <label v-if="token.read_permission">
                {{ $t('download') }}
              </label><br>
              <toggle-button
                v-if="token.read_permission"
                v-model="token.appropriate_permission"
                :labels="{checked: 'Yes', unchecked: 'No'}"
                class="ml-3"
              /> <label v-if="token.read_permission">
                {{ $t('appropriate') }}
              </label>
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 col-sm-3">
            <dt>{{ $t('expirationdate') }}</dt>
          </div>
          <div class="col-xs-12 col-sm-3">
            <dd>
              <datepicker
                v-model="token.expiration_time"
                :bootstrap-styling="false"
                input-class="form-control form-control-sm  search-calendar"
                :calendar-button="false"
                calendar-button-icon=""
                wrapper-class="calendar-wrapper"
                :placeholder="$t('expirationdate')"
                :clear-button="true"
                clear-button-icon="fa fa-times"
              />
            </dd>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12 offset-sm-3 col-sm-9">
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="!token.title || (token.scope_type==='album' && !token.album)"
            >
              {{ $t('create') }}
            </button><button
              type="reset"
              class="btn btn-secondary ml-3"
              @click="cancel"
            >
              {{ $t('cancel') }}
            </button>
          </div>
        </div>
      </fieldset>
    </form>

    <b-modal
      id="tokenModal"
      ref="tokenModal"
      centered
      no-fade
      hide-footer
      no-close-on-backdrop
      size="lg"
    >
      <dl class="my-2 row">
        <dt class="col-xs-12 col-sm-3">
          {{ token.title }}
        </dt>
        <dd class="col-xs-10 col-sm-8">
          <input
            v-model="token.secret"
            type="text"
            readonly
            class="form-control form-control-sm"
          >
        </dd>
        <div class="col-xs-2 col-sm-1 pointer">
          <button
            v-clipboard:copy="token.secret"
            v-clipboard:success="onCopy"
            v-clipboard:error="onCopyError"
            type="button"
            class="btn btn-secondary btn-sm"
          >
            <v-icon
              name="paste"
              scale="1"
            />
          </button>
        </div>
      </dl>
    </b-modal>
  </div>
</template>

<script>
import moment from 'moment'
import Datepicker from 'vuejs-datepicker'
import { mapGetters } from 'vuex'
export default {
	name: 'NewToken',
	components: { Datepicker },
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
			token: {
				title: '',
				scope_type: this.scope,
				album: this.albumid,
				secret: '',
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
	created () {
		this.$store.dispatch('getAlbums', { pageNb: 1, limit: 100, sortBy: 'created_time', sortDesc: true, canCreateCapabilityToken: 'true' })
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
			this.$store.dispatch('createToken', { token: token }).then(res => {
				this.token.secret = res.data.secret
				this.$snotify.success('token created successfully')
				this.$refs.tokenModal.show()
			}).catch(() => {
				this.$snotify.error(this.$t('sorryerror'))
			})
		},
		onCopy () {
			this.$snotify.success(this.$t('tokencopysuccess'))
			this.$refs.tokenModal.hide()
			this.$emit('done')
		},
		onCopyError () {
			this.$snotify.error(this.$t('sorryerror'))
			this.$refs.tokenModal.hide()
		},

		cancel () {
			this.$emit('done')
		}
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
