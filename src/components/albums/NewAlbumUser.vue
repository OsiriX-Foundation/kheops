<template>
  <table class="table tableAddUserNewAlbum">
    <thead>
      <tr>
        <th>{{ $t('albumuser.username') }}</th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="user in users"
        :key="user.email"
      >
        <td>
          {{ user|getUsername }} {{ user.name !== undefined ? `- ( ${user.name} )` : '' }}
          <span
            v-if="user.is_admin"
          >
            -
            <span
              class="font-neutral"
            >
              {{ $t("albumuser.Admin") }}
            </span>
          </span>
          <div
            class="d-block d-md-none"
          >
            <a
              class="font-white"
              @click.stop="toggleAdmin(user)"
            >
              {{ (user.is_admin)?$t('albumuser.changeroleuser'):$t('albumuser.changeroleadmin') }}
              <v-icon
                name="user"
              />
            </a>
            <br>
            <a
              class="text-danger"
              @click="deleteUser(user)"
            >
              {{ $t('albumuser.remove') }}
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
              {{ (user.is_admin)?$t('albumuser.changeroleuser'):$t('albumuser.changeroleadmin') }}
              <v-icon
                name="user"
              />
            </a>
            <br>
            <a
              class="text-danger"
              @click="deleteUser(user)"
            >
              {{ $t('albumuser.remove') }}
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
