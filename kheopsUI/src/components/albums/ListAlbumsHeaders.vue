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
            {{ $t('listalbums.newalbum') }}
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
            {{ $t("listalbums.share") }}
          </button>
        </div>
        <div
          class="ml-auto align-self-center text-right col-xxs-12 col-xs-auto"
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
    <form-get-user
      v-if="form_send_album && disabledBtnShare === false"
      @get-user="sendToUser"
      @cancel-user="form_send_album=false"
    />
  </div>
</template>

<script>
import formGetUser from '@/components/user/getUser';

export default {
  name: 'ListAlbumsHeaders',
  components: { formGetUser },
  props: {
    disabledBtnShare: {
      type: Boolean,
      required: false,
      default: true,
    },
    albumsSelected: {
      type: Array,
      required: true,
      default: () => ([]),
    },
  },
  data() {
    return {
      form_send_album: false,
    };
  },
  methods: {
    searchClick() {
      this.$emit('searchClick');
    },
    inviteClick() {
      this.form_send_album = true;
    },
    reloadAlbums() {
      this.$emit('reloadAlbums');
    },
    goNewAlbum() {
      this.$router.push('/albums/new');
    },
    sendToUser(userId) {
      this.albumsSelected.forEach((album) => {
        this.$store.dispatch('addUser', { album_id: album.album_id, user_id: userId }).then(() => {
          this.$snotify.success(this.$t('listalbums.albumshared'));
        });
      });
    },
  },
};

</script>
