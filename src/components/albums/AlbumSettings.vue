<i18n>
{
  "en": {
    "general": "General",
    "user": "Users",
    "tokens": "Tokens",
    "providerSR": "Report Providers",
    "socialmedia": "Social Media"
  },
  "fr": {
    "general": "Général",
    "user": "Utilisateurs",
    "tokens": "Tokens",
    "providerSR": "Report Providers",
    "socialmedia": "Réseaux sociaux"
  }
}
</i18n>

<template>
  <div
    id="albumSettings"
    class="container"
  >
    <div class="row">
      <div class="d-none d-md-block col-md-2">
        <nav class="nav nav-pills nav-justified flex-column">
          <router-link
            v-for="(cat,idx) in categories"
            :key="idx"
            :to="{ name: 'albumsettings', params: { category: cat }}"
            class="nav-link"
            :class="(currentCategory === cat || (cat === 'general' && currentCategory === undefined)) ? 'active' : ''"
            active-class="active"
          >
            {{ $t(cat) }}
          </router-link>
        </nav>
      </div>
      <div class="d-block d-sm-block d-md-none col-12 ">
        <b-dropdown
          id="dropdown-right"
          :text="$t(currentCategory)"
          variant="primary"
          class="m-2 p-2 d-flex justify-content-center"
          toggle-class="col-12"
          menu-class="col-10"
        >
          <b-dropdown-item
            v-for="(cat,idx) in categories"
            :key="idx"
            href="#"
            :active="currentCategory === cat"
          >
            <router-link
              :to="{ name: 'albumsettings', params: { category: cat }}"
              class="nav-link font-white"
            >
              {{ $t(cat) }}
            </router-link>
          </b-dropdown-item>
        </b-dropdown>
      </div>
      <div class="col-sm-12 col-md-10">
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
        <album-settings-social-media
          v-if="currentCategory === 'socialmedia'"
          :album="album"
        />
      </div>
    </div>
  </div>
</template>

<script>
import AlbumSettingsGeneral from '@/components/albums/AlbumSettingsGeneral';
import AlbumSettingsUser from '@/components/albums/AlbumSettingsUser';
import AlbumSettingsToken from '@/components/albums/AlbumSettingsToken';
import AlbumSettingsReportProvider from '@/components/albums/AlbumSettingsReportProvider';
import AlbumSettingsSocialMedia from '@/components/albums/AlbumSettingsSocialMedia';

export default {
  name: 'AlbumSettings',
  components: {
    AlbumSettingsGeneral,
    AlbumSettingsUser,
    AlbumSettingsToken,
    AlbumSettingsReportProvider,
    AlbumSettingsSocialMedia,
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
    };
  },
  computed: {
    categories() {
      const categories = [...this.basicCategories];
      if (this.album.is_admin) {
        // categories.push('socialmedia');
        categories.push('tokens');
      }
      return categories;
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
