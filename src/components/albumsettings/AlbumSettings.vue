<template>
  <div
    id="albumSettings"
    class="container"
  >
    <div class="row">
      <div class="d-none d-md-block col-md-3 col-lg-2">
        <album-settings-menu-nav
          :categories="categories"
        />
      </div>
      <div class="d-block d-sm-block d-md-none col-12 ">
        <album-settings-menu-dropdown
          :categories="categories"
        />
      </div>
      <div class="col-sm-12 col-md-9 col-lg-10">
        <album-settings-general
          v-if="currentCategory === 'general' || currentCategory === undefined"
          :album="album"
        />
        <album-settings-user
          v-if="currentCategory === 'user'"
          :album="album"
        />
        <album-settings-token
          v-if="currentCategory === 'tokens'"
          :album="album"
        />
        <album-settings-report-provider
          v-if="currentCategory === 'providerSR'"
          :album="album"
        />
      </div>
    </div>
  </div>
</template>

<script>
import AlbumSettingsGeneral from '@/components/albumsettings/AlbumSettingsGeneral';
import AlbumSettingsUser from '@/components/albumsettings/AlbumSettingsUser';
import AlbumSettingsToken from '@/components/albumsettings/AlbumSettingsToken';
import AlbumSettingsReportProvider from '@/components/albumsettings/AlbumSettingsReportProvider';
import AlbumSettingsMenuNav from '@/components/albumsettings/AlbumSettingsMenuNav';
import AlbumSettingsMenuDropdown from '@/components/albumsettings/AlbumSettingsMenuDropdown';

export default {
  name: 'AlbumSettings',
  components: {
    AlbumSettingsGeneral,
    AlbumSettingsUser,
    AlbumSettingsToken,
    AlbumSettingsReportProvider,
    AlbumSettingsMenuNav,
    AlbumSettingsMenuDropdown,
  },
  props: {
    album: {
      type: Object,
      required: true,
      default: () => {},
    },
  },
  data() {
    return {
      basicCategories: ['general', 'user', 'providerSR'],
      adminCategories: ['tokens'],
    };
  },
  computed: {
    categories() {
      if (this.album.is_admin) {
        return this.basicCategories.concat(this.adminCategories);
      }
      return this.basicCategories;
    },
    currentCategory() {
      return this.$route.params.category !== undefined ? this.$route.params.category : this.basicCategories[0];
    },
  },
  watch: {
  },
  created() {
  },
  beforeDestroy() {
  },
  methods: {
  },
};
</script>
