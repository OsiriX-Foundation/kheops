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
        <input-auto-complet
          :placeholder="$t('user.emailuser')"
          :context="context"
          :disabled="!enableAdd"
          :submit="false"
          @keydown-enter-prevent="checkUser"
          @input-value="setUsername"
        />
        <div
          v-if="onloading === false"
          class="input-group-append"
        >
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
        <div
          v-if="onloading === true"
        >
          <kheops-clip-loader
            size="25px"
            class="ml-2"
          />
        </div>
      </div>
    </h5>
  </div>
</template>

<script>
import { HTTP } from '@/router/http';
import InputAutoComplet from '@/components/globals/InputAutoComplet';
import KheopsClipLoader from '@/components/globalloading/KheopsClipLoader';

export default {
  name: 'AddUser',
  components: { KheopsClipLoader, InputAutoComplet },
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
      onloading: false,
    };
  },
  computed: {
    accessVar() {
      return this.scope === 'album' ? 'album_access' : 'study_access';
    },
    context() {
      if (this.scope === 'album') {
        return {
          key: 'album',
          value: this.id,
        };
      }
      return {
        key: 'studyInstanceUID',
        value: this.id,
      };
    },
  },
  watch: {
    enableAdd: {
      handler() {
        if (!this.enableAdd) {
          this.deleteUser();
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
        this.onloading = true;
        const username = this.newUserName;
        this.checkSpecificUser(username).then((res) => {
          if (res.status === 204) {
            this.$snotify.error(this.$t('user.usernotfound'));
          } else if (!res.data[this.accessVar]) {
            this.$snotify.error(this.scope === 'album' ? this.$t('user.noaccessalbum', { user: username }) : this.$t('user.noaccessstudy', { user: username }));
          } else if (res.status === 200 && res.data[this.accessVar]) {
            this.setUser(res.data.email);
          }
          this.onloading = false;
        }).catch(() => {
          this.onloading = false;
        });
      } else {
        this.$snotify.error(this.$t('user.nouser'));
      }
    },
    setUser(user) {
      this.user = user;
      this.newUserName = '';
      this.$emit('private-user', this.user);
    },
    setUsername(username) {
      this.newUserName = username;
    },
  },
};
</script>
