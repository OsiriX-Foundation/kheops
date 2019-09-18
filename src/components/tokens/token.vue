<i18n>
{
	"en": {
		"token": "token",
		"description": "description",
		"scope": "scope",
		"album": "album",
		"permission": "permission",
		"write": "write",
		"read": "read",
		"download": "show download button",
		"appropriate": "send to user / album",
		"expirationdate": "expiration date",
		"startdate": "start date",
		"creationdate": "creation date",
		"revokeddate": "revoke date",
		"revoke": "revoke",
		"thistokenrevoked": "this token is revoked",
		"lastuse": "last use date"
	},
	"fr": {
		"token": "token",
		"description": "description",
		"scope": "applicable à",
		"album": "album",
		"permission": "permission",
		"write": "écriture",
		"read": "lecture",
		"download": "bouton téléchargement",
		"appropriate": "envoyer à un utlisateur ou album",
		"expirationdate": "date d'expiration",
		"startdate": "date de début",
		"creationdate": "date de création",
		"revokeddate": "date de révoquation",
		"revoke": "révoquer",
		"thistokenrevoked": "ce token a été revoqué",
		"lastuse": "dernière utilisation"
	}
}
</i18n>

<template>
  <div class="token">
    <div
      class="my-3 selection-button-container"
      style=" position: relative;"
    >
      <h4>
        <button
          type="button"
          class="btn btn-link btn-sm d-md-none"
          @click.stop="cancel"
        >
          <span>
            <v-icon
              name="arrow-left"
              color="white"
            />
          </span>
        </button>
        {{ token.title }}
      </h4>
    </div>
    <p
      v-if="token.revoked"
      class="py-3 text-danger"
    >
      {{ $t('thistokenrevoked') }}
    </p>
    <div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('scope') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            {{ token.scope_type }}
          </dd>
        </div>
      </div>
      <div
        v-if="token.scope_type=='album'"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('album') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-9">
          <dd>
            <router-link :to="`/albums/${token.album.album_id}?view=studies`">
              {{ token.album.name }}
            </router-link>
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
          <dd>{{ permissions }}</dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('expirationdate') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.expiration_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('startdate') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.not_before_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div class="row">
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('creationdate') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.issued_at_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div
        v-if="token.last_used"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('lastuse') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd>
            {{ token.last_used|formatDateTime }}
          </dd>
        </div>
      </div>
      <div
        v-if="token.revoked"
        class="row"
      >
        <div class="col-xs-12 col-sm-3">
          <dt>{{ $t('revokeddate') }}</dt>
        </div>
        <div class="col-xs-12 col-sm-3">
          <dd class="text-danger">
            {{ token.revoke_time|formatDateTime }}
          </dd>
        </div>
      </div>
      <div class="row mt-3">
        <div class="offset-sm-3 col-sm-9 d-none d-sm-none d-md-block">
          <button
            type="button"
            class="btn btn-secondary mr-3"
            @click="cancel"
          >
            {{ $t('back') }}
          </button>
          <button
            v-if="!token.revoked"
            type="button"
            class="btn btn-danger ml-3"
            @click="revoke"
          >
            {{ $t('revoke') }}
          </button>
        </div>

        <div class="col-12 d-md-none">
          <button
            v-if="!token.revoked"
            type="button"
            class="btn btn-danger btn-block"
            @click="revoke"
          >
            {{ $t('revoke') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
	name: 'Token',
	props: {
		token: {
			type: Object,
			required: true
		}
	},
	data () {
		return {
		}
	},
	computed: {
		permissions () {
			let perms = []
			_.forEach(this.token, (value, key) => {
				if (key.indexOf('permission') > -1 && value) {
					perms.push(key.replace('_permission', ''))
				}
			})
			return perms.length ? perms.join(', ') : '-'
		}
	},
	methods: {
		revoke () {
			this.$emit('revoke', this.token.id)
			this.cancel()
		},
		cancel () {
			this.$emit('done')
		}
	}
}
</script>

<style scoped>
dt{
	text-align: left;
	text-transform: capitalize;
}
label{
	text-transform: capitalize;
	margin-left: 1em;
}
div.calendar-wrapper{
	color: #333;
}
button{
	text-transform: capitalize;
}
</style>
