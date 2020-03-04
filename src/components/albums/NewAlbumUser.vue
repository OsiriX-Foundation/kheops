
<i18n>
{
  "en": {
    "username": "User name",
    "user": "user",
    "changeroleadmin": "change role to data steward",
    "changeroleuser": "change role to user",
    "remove": "Remove user",
    "Admin": "Data steward"
  },
  "fr": {
    "username": "Utilisateur",
    "user": "utilisateur",
    "changeroleadmin": "changer le rôle pour gardien des données",
    "changeroleuser": "changer le rôle pour utilisateur",
    "remove": "Retirer l'utilisateur",
    "Admin": "Gardien des données"
  }
}
</i18n>
<template>
  <table class="table tableAddUserNewAlbum">
    <thead>
      <tr>
        <th>{{ $t('username') }}</th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="user in users"
        :key="user.email"
      >
        <td>
          {{ user|getUsername }} {{ user.email !== undefined ? `- ${user.email}` : '' }}
          <span
            v-if="user.is_admin"
          >
            -
            <span
              class="font-neutral"
            >
              {{ $t("Admin") }}
            </span>
          </span>
          <div
            class="d-block d-md-none"
          >
            <a
              class="font-white"
              @click.stop="toggleAdmin(user)"
            >
              {{ (user.is_admin)?$t('changeroleuser'):$t('changeroleadmin') }}
              <v-icon
                name="user"
              />
            </a>
            <br>
            <a
              class="text-danger"
              @click="deleteUser(user)"
            >
              {{ $t('remove') }}
              <v-icon name="trash" />
            </a>
          </div>
        </td>
        <td
          class="text-right d-none d-md-block"
        >
          <div
            class="user_actions"
          >
            <a
              class="font-white"
              @click.stop="toggleAdmin(user)"
            >
              {{ (user.is_admin)?$t('changeroleuser'):$t('changeroleadmin') }}
              <v-icon
                name="user"
              />
            </a>
            <br>
            <a
              class="text-danger"
              @click="deleteUser(user)"
            >
              {{ $t('remove') }}
              <v-icon name="trash" />
            </a>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>
<script>

export default {
  name: 'NewAlbumUser',
  components: { },
  props: {
    users: {
      type: Array,
      required: true,
      default: () => [],
    },
    loading: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  methods: {
    toggleAdmin(user) {
      this.$emit('toggle-admin', user);
    },
    deleteUser(user) {
      this.$emit('delete-user', user);
    },
  },
};
</script>
