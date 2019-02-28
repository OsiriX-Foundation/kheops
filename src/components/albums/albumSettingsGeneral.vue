/* eslint-disable */
<i18n>
{
	"en":{
		"albumname": "Album name",
		"albumdescription": "Album description",
		"notification": "Notifications"
	},
	"fr": {
		"albumname": "Nom de l'album",
		"albumdescription": "Description de l'album",
		"notification": "Notifications"
	}
}
</i18n>

<template>
  <div class="container">
    <dl>
      <dt>{{ $t('albumname') }}</dt>
      <dd>
        <div v-if="edit.name === '-1'">
          {{ album.name }} <span
            v-if="album.is_admin && edit.name === '-1'"
            class="icon-edit"
            @click="edit.name=album.name"
          >
            <v-icon name="pencil-alt" />
          </span>
        </div>
        <div v-if="edit.name !== '-1'">
          <form @submit.prevent="updateAlbum">
            <div class="input-group mb-2">
              <div>
                <input
                  v-model="edit.name"
                  type="text"
                  class="form-control"
                >
              </div>
              <div class="input-group-append">
                <button
                  class="btn btn-primary"
                  type="submit"
                >
                  {{ $t('update') }}
                </button>
                <button
                  class="btn btn-secondary"
                  type="reset"
                  tabindex="0"
                  @keyup.esc="edit.name = '-1'"
                  @click="edit.name = '-1'"
                >
                  {{ $t('cancel') }}
                </button>
              </div>
            </div>
          </form>
        </div>
      </dd>
      <dt>
        {{ $t('albumdescription') }}<span
          v-if="album.is_admin && edit.description === '-1'"
          class="icon-edit float-right"
          @click="edit.description=album.description"
        >
          <v-icon name="pencil-alt" />
        </span>
      </dt>
      <dd class="album_description">
        <div v-if="edit.description === '-1'">
          <p
            v-for="(p,pidx) in formattedAlbumDescription"
            :key="pidx"
            class="my-0"
          >
            {{ p }}
          </p>
        </div>
        <div v-if="edit.description !== '-1'">
          <form @submit.prevent="updateAlbum">
            <div class="">
              <div>
                <textarea
                  v-model="edit.description"
                  rows="6"
                  class="form-control"
                />
              </div>
              <div>
                <button
                  class="btn btn-primary"
                  type="submit"
                >
                  {{ $t('update') }}
                </button>
                <button
                  class="btn btn-secondary"
                  type="reset"
                  tabindex="0"
                  @keyup.esc="edit.description = '-1'"
                  @click="edit.description = '-1'"
                >
                  {{ $t('cancel') }}
                </button>
              </div>
            </div>
          </form>
        </div>
      </dd>
    </dl>
    <!--
		<dl>
			<dt>{{$t('notification')}}</dt>
			<dd style = 'margin-top: 10px'>
				<div class = 'row'>
					<div class = 'col'>
						<toggle-button v-model="album.notification_new_series" :labels="{checked: 'Yes', unchecked: 'No'}" :sync="true" @change='updateAlbum'/> <label>New Study</label>
					</div>
					<div class = 'col'>
						<toggle-button v-model="album.notification_new_comment" :labels="{checked: 'Yes', unchecked: 'No'}" :sync="true"  @change='updateAlbum'/> <label>New comment</label>
					</div>
				</div>
			</dd>
		</dl>
		-->
    <album-buttons
      :album="album"
      :users="users"
      :show-quit="true"
      :show-delete="album.is_admin"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import AlbumButtons from '@/components/albums/AlbumButtons'

export default {
	name: 'AlbumSettingsGeneral',
	components: { AlbumButtons },
	data () {
		return {
			edit: {
				name: '-1',
				description: '-1'
			}
		}
	},
	computed: {
		...mapGetters({
			album: 'album',
			users: 'users'
		}),
		formattedAlbumDescription () {
			return this.album.description.split('\n')
		}
	},
	created () {
		this.$store.dispatch('getAlbum', { album_id: this.$route.params.album_id })
		this.$store.dispatch('getUsers')
	},
	methods: {
		updateAlbum () {
			if (!this.album.is_admin) {
				this.$snotify.error(this.$t('permissiondenied'))
				return
			}
			let params = {}
			_.forEach(this.edit, (v, k) => {
				if (v === '-1') return
				if (this.album[k] !== v) {
					params[k] = v
				}
			})
			params.notificationNewComment = this.album.notification_new_comment
			params.notificationNewSeries = this.album.notification_new_series

			this.$store.dispatch('patchAlbum', params).then(() => {
				this.$snotify.success(this.$t('albumupdatesuccess'))
				this.edit.name = '-1'
				this.edit.description = '-1'
			}).catch(() => {
				this.$snotify.error(this.$t('sorryerror'))
			})
		}
	}
}

</script>

<style scoped>
dd span.icon-edit, dt span.icon-edit {
	margin: 0 10px;
	cursor: pointer;
}

dl {
	font-size: 125%;
}
dl label {
	font-size: 100%;
	margin-left: 20px;
}

dd.album_description{
	border: 1px solid #333;
	height: 10em;
	padding: 10px;
}

</style>

