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
  <div>
    <div
      class="pt-2"
    >
      <div
        class="d-flex flex-wrap"
      >
        <div class="p-2 align-self-center">
          <button
            class="btn btn-secondary ml-2"
            @click="goNewAlbum"
          >
            <v-icon
              name="plus"
              class="mr-2"
            />
            {{ $t('newalbum') }}
          </button>
        </div>
        <div
          class="align-self-center"
        >
          <button
            type="button"
            class="btn btn-link btn-sm text-center inline-white"
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
          class="ml-auto align-self-center"
        >
          <button
            type="button"
            class="btn btn-link kheopsicon"
            @click="reloadAlbums()"
          >
            <v-icon
              name="refresh"
              scale="2"
            />
          </button>
          <button
            type="button"
            class="btn btn-link kheopsicon"
            @click="searchClick"
          >
            <v-icon
              name="search"
              scale="1.8"
            />
          </button>
        </div>
      </div>
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
      default: true,
    },
  },
  data() {
    return {};
  },
  methods: {
    searchClick() {
      this.$emit('searchClick');
    },
    inviteClick() {
      this.$emit('inviteClick');
    },
    reloadAlbums() {
      this.$emit('reloadAlbums');
    },
    goNewAlbum() {
      this.$router.push('/albums/new');
    },
  },
};

</script>
