<!--
    scope: Define the scope (Study or Album)
      - type: String
      - required: true
      - default: ''
    id: Define the id of the target of the comment (StudyID or AlbumID)
      - type: String
      - required: true
      - default: ''
    enableAdd: Enable input if this prop is true.
      - type: Boolean
      - required: true
      - default: true

    this component send emit "private-user" to the parent when :
      - when user is delete
      - when user is add
      - when Enable Add is set to false in parent component
-->
<i18n>
{
  "en": {
    "userunknown": "User {user} unknown",
    "nouser": "No user specified",
    "noaccessalbum": "{user} has no access to this album",
    "noaccessstudy": "{user} has no access to this study"
  },
  "fr" : {
    "userunknown": "Utilisateur {user} inconnu",
    "nouser": "Aucun utilisateur spécifié",
    "noaccessalbum": "{user} n'a pas d'accès à cet album",
    "noaccessstudy": "{user} n'a pas d'accès à cette étude"
  }
}
</i18n>
<template>
  <div>
    <h5
      v-if="user"
      class="user"
    >
      <span
        class="badge badge-secondary"
      >
        {{ user }}
        <span
          class="icon pointer"
          @click="deleteUser()"
        >
          <v-icon name="times" />
        </span>
      </span>
    </h5>
    <h5
      v-else
      class="user"
    >
      <div class="input-group mb-3">
        <input
          ref="textcomment"
          v-model="newUserName"
          type="email"
          class="form-control form-control-sm"
          placeholder="email"
          aria-label="Email"
          :disabled="!enableAdd"
          @keydown.enter.prevent="checkUser"
        >
        <div class="input-group-append">
          <button
            id="button-addon2"
            class="btn btn-outline-secondary btn-sm"
            type="button"
            title="add user"
            :disabled="!enableAdd"
            @click="checkUser()"
          >
            <v-icon name="plus" />
          </button>
        </div>
      </div>
    </h5>
  </div>
</template>

<script>
import { HTTP } from '@/router/http';

export default {
  name: 'AddUser',
  props: {
    scope: {
      type: String,
      required: true,
      default: '',
    },
    enableAdd: {
      type: Boolean,
      required: true,
      default: true,
    },
    id: {
      type: String,
      required: true,
      default: '',
    },
  },
  data() {
    return {
      user: '',
      newUserName: '',
    };
  },
  computed: {
    accessVar() {
      return this.scope === 'album' ? 'album_access' : 'study_access';
    },
  },
  watch: {
    enableAdd: {
      handler() {
        if (!this.enableAdd) {
          this.deleteUser();
        } else {
          const { textcomment } = this.$refs;
          setTimeout(() => { textcomment.focus(); }, 0);
        }
      },
    },
  },
  methods: {
    deleteUser() {
      this.user = '';
      this.$emit('private-user', this.user);
    },
    checkSpecificUser(username) {
      const request = `users?reference=${username}&${this.scope === 'album' ? 'album' : 'studyInstanceUID'}=${this.id}`;
      return HTTP.get(request, { headers: { Accept: 'application/json' } });
    },
    checkUser() {
      if (this.newUserName.length > 0) {
        const username = this.newUserName;
        this.checkSpecificUser(username).then((res) => {
          if (res.status === 204) {
            this.$snotify.error(this.$t('userunknown', { user: username }));
          } else if (!res.data[this.accessVar]) {
            this.$snotify.error(this.scope === 'album' ? this.$t('noaccessalbum', { user: username }) : this.$t('noaccessstudy', { user: username }));
          } else if (res.status === 200 && res.data[this.accessVar]) {
            this.setUser(res.data.email);
          }
        }).catch(() => {
          console.log('Sorry, an error occured');
        });
      } else {
        this.$snotify.error(this.$t('nouser'));
      }
    },
    setUser(user) {
      this.user = user;
      this.newUserName = '';
      this.$emit('private-user', this.user);
    },
  },
};
</script>
