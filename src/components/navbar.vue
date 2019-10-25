<i18n>
{
  "en" : {
    "inbox": "Inbox",
    "albums": "Albums",
    "favorites": "Favorites",
    "recents": "Recents"
  },
  "fr": {
    "inbox": "Boîte de réception",
    "albums": "Albums",
    "favorites": "Favoris",
    "recents": "Récents"
  }
}
</i18n>

<template>
  <div
    id="navbar_container"
    class="container"
  >
    <div class="nav-container">
      <ul class="nav nav-pills nav-fill">
        <li class="nav-item">
          <router-link
            to="/inbox"
            class="nav-link"
            active-class="active"
          >
            <v-icon
              name="bars"
              class="navbar-icon-margin"
            />{{ $t("inbox") }}
          </router-link>
        </li>
        <li class="nav-item">
          <router-link
            to="/albums"
            class="nav-link"
            active-class="active"
          >
            <v-icon
              name="book"
              class="navbar-icon-margin"
            />{{ $t("albums") }}
          </router-link>
        </li>
        <!--
        <li class="nav-item">
          <router-link to="/favorites" class="nav-link" active-class="active"><v-icon name='star'/>{{ $t("favorites") }}</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/recents" class="nav-link" active-class="active"><v-icon name='clock'/>{{ $t("recents") }}</router-link>
        </li>
        -->
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NavBar',
  data() {
    return {
      activePath: 'inbox',
    };
  },
  watch: {
    $route(to) {
      [, this.activePath] = to.path.split('/');
    },
  },
};

</script>
