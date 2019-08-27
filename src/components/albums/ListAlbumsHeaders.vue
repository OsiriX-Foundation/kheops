<i18n>
{
	"en": {
		"newalbum": "New album",
		"share": "Invite user"
	},
	"fr": {
		"newalbum": "Nouvel album",
		"share": "Inviter un utilisateur"
	}
}
</i18n>
<template>
  <div
    class="d-flex"
  >
    <div
      class="p-2"
    >
      <h3>
        <router-link
          to="albums/new"
          active-class="active"
          style="display: inline; color: white"
        >
          <v-icon
            name="plus"
            class="mr-2"
          />{{ $t('newalbum') }}
        </router-link>
      </h3>
    </div>
    <div
      class="p-2"
    >
      <button
        type="button"
        class="btn btn-link btn-sm text-center"
        style="display: inline; color: white"
        :disabled="disabledBtnShare"
        @click="inviteClick"
      >
        <span>
          <v-icon
            name="user-plus"
            scale="1.5"
          />
        </span><br>
        {{ $t("share") }}
      </button>
    </div>
    <div
      class="ml-auto"
    >
      <button
        type="button"
        class="btn btn-link btn-lg"
        style="display: inline; color: white"
        @click="searchClick"
      >
        <v-icon
          name="search"
          scale="2"
        />
      </button>
    </div>
  </div>
</template>

<script>

export default {
	name: 'ListAlbumsHeaders',
	components: { },
	props: {
		disabledBtnShare: {
			type: Boolean,
			required: false,
			default: true
		}
	},
	data () {
		return {}
	},
	methods: {
		searchClick () {
			this.$emit('searchClick')
		},
		inviteClick () {
			this.$emit('inviteClick')
		}
	}
}

</script>
<style scoped>
.btn-link {
	font-weight: 400;
	color: white;
	background-color: transparent;
}

.btn-link:hover {
	color: #c7d1db;
	text-decoration: underline;
	background-color: transparent;
	border-color: transparent;
}
</style>
