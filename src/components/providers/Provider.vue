<template>
  <div v-if="Object.keys(provider).length > 0">
    <div
      class="my-3 selection-button-container"
      style=" position: relative;"
    >
      <h4>
        {{ provider.name }}
      </h4>

      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('url') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.url }}
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('user') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.user.email }}
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('clientid') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.client_id }}
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('created_time') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ provider.created_time }}
          </dd>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-xs-12 col-sm-12 offset-md-3 col-md-9">
        <button
          class="btn btn-primary"
          @click.stop="edit()"
        >
          Edit
        </button>
        <button
          type="button"
          class="btn btn-danger ml-3"
          @click="deleteProvider"
        >
          Remove
        </button>
        <button
          type="submit"
          class="btn btn-secondary ml-3"
          @click="back"
        >
          Back
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
	name: 'Provider',
	props: {
		albumID: {
			type: String,
			required: true,
			default: ''
		},
		clientID: {
			type: String,
			required: true,
			default: ''
		}
	},
	data () {
		return {
		}
	},
	computed: {
		...mapGetters({
			provider: 'provider'
		})
	},
	created: function () {
    this.$store.dispatch('getProvider', { albumID: this.albumID, clientID: this.clientID }).then(res => {
      if (res.status !== 200) {
        this.$snotify.error('Sorry, an error occured')
      }
    }).catch(err => {
      console.log(err)
    })
	},
	methods: {
		back () {
			this.$emit('done')
    },
    edit() {
      this.$emit('providerselectededit', this.clientID)
    },
    deleteProvider () {
      this.$store.dispatch('deleteProvider', { albumID: this.albumID, clientID: this.clientID }).then(res => {
        if (res.status !== 204) {
          this.$snotify.error('Sorry, an error occured')
        } else {
          this.$snotify.success('Provider remove')
			    this.$emit('done')
        }
      }).catch(err => {
        console.log(err)
      })
    }
	}
}
</script>
