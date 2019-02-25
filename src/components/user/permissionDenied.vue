<i18n>
{
	"en": {
		"permissiondenied": "Permission denied",
		"sorrynotauthorized": "Sorry, you are not authorized to view this section",
		"gotohome": "Go To Home Page",
		"gotologin": "Go To Login Page"
	},
	"fr": {
		"permissiondenied": "Permission refus&eacute;e",
		"sorrynotauthorized": "D&eacute;sol&eacute;, vous n&#x27;&ecirc;tes pas autoris&eacute; &agrave; acc&eacute;der &agrave; cette section",
		"gotohome": "Aller &agrave; la page d&#x27;accueil",
		"gotologin": "Aller &agrave; la page de login"
	}
}
</i18n>

<template>
  <b-jumbotron
    bg-variant="info"
    text-variant="white"
    border-variant="dark"
  >
    <template slot="header">
      {{ $t("permissiondenied") }}
    </template>
    <template slot="lead">
      {{ $t("sorrynotauthorized") }}
    </template>
    <hr class="my-4">
    <p>
      <b-button
        size="lg"
        @click="changeLocation('home')"
      >
        {{ $t("gotohome") }}
      </b-button>
      <b-button
        size="lg"
        @click="changeLocation('login')"
      >
        {{ $t("gotologin") }}
      </b-button>
    </p>
  </b-jumbotron>
</template>

<script>
export default {
	name: 'PermissionDenied',
	data () {
		return {
		}
	},
	methods: {
		changeLocation (path) {
			var vm = this
			vm.$router.push('/' + path)
		}
	}
}
</script>
