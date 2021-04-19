<template>
  <div class="container">
    <dl
      class="font-large word-break-all"
    >
      <dt>{{ $t('albumsettings.albumname') }}</dt>
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
          <form
            @submit.prevent="updateAlbum"
          >
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
              <div
                v-if="onloading === false"
                class="input-group-append"
              >
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
              <div
                v-if="onloading === true"
                class="ml-2 mt-1"
              >
                <kheops-clip-loader
                  size="25px"
                />
              </div>
            </div>
          </form>
        </div>
      </dd>
      <dt>
        {{ $t('albumsettings.albumdescription') }}
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
              <div
                v-if="onloading === false"
              >
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
              <div
                v-if="onloading === true"
                class="mt-2"
              >
                <kheops-clip-loader
                  size="25px"
                  class-loader="text-left"
                />
              </div>
            </div>
          </form>
        </div>
      </dd>
      <dd
        class="font-small font-grey"
      >
        {{ $t('albumsettings.id') }}: {{ album.album_id }}
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
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'AlbumSettingsGeneral',
  components: { AlbumButtons, KheopsClipLoader },
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
      onloading: false,
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
      this.onloading = true;
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
        this.onloading = false;
      }).catch(() => {
        this.onloading = false;
      });
    },
  },
};

</script>
