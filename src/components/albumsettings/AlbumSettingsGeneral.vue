<i18n>
{
  "en":{
    "albumname": "Album name",
    "albumdescription": "Album description",
    "notification": "Notifications",
    "cancel": "Cancel",
    "update": "Update"
  },
  "fr": {
    "albumname": "Nom de l'album",
    "albumdescription": "Description de l'album",
    "notification": "Notifications",
    "cancel": "Annuler",
    "update": "Mettre à jour"
  }
}
</i18n>

<template>
  <div class="container">
    <dl
      class="font-large word-break-all"
    >
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
                  v-focus
                  type="text"
                  class="form-control"
                  maxlength="255"
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
        {{ $t('albumdescription') }}
        <span
          v-if="album.is_admin && edit.description === '-1'"
          class="icon-edit float-right"
          @click="edit.description=album.description"
        >
          <v-icon name="pencil-alt" />
        </span>
      </dt>
      <dd
        class="album_description"
      >
        <div v-if="edit.description === '-1'">
          <p
            v-for="(p,pidx) in formattedAlbumDescription"
            :key="pidx"
            class="my-0 word-break-all"
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
                  v-focus
                  rows="5"
                  class="form-control no-resize"
                  maxlength="2048"
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
    <album-buttons
      :album="album"
      :users="users"
      :show-quit="true"
      :show-delete="album.is_admin"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import AlbumButtons from '@/components/albumsettings/AlbumButtons';

export default {
  name: 'AlbumSettingsGeneral',
  components: { AlbumButtons },
  props: {
    album: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  data() {
    return {
      edit: {
        name: '-1',
        description: '-1',
      },
    };
  },
  computed: {
    ...mapGetters({
      users: 'albumUsers',
    }),
    formattedAlbumDescription() {
      if (this.album.description !== undefined) {
        return this.album.description.split('\n');
      }
      return '';
    },
  },
  created() {
    this.$store.dispatch('getUsersAlbum', { album_id: this.album.album_id });
  },
  methods: {
    updateAlbum() {
      if (!this.album.is_admin) {
        this.$snotify.error(this.$t('permissiondenied'));
        return;
      }
      const queries = {};
      Object.keys(this.edit).forEach((id) => {
        if (this.edit[id] !== '-1') {
          queries[id] = this.edit[id];
        }
      });

      this.$store.dispatch('editAlbum', { album_id: this.album.album_id, queries }).then((res) => {
        if (res.status === 200) {
          this.edit.description = '-1';
          this.edit.name = '-1';
        }
      });
    },
  },
};

</script>
