<i18n>
{
  "en": {
    "username": "User name",
    "user": "user",
    "send": "Send",
    "cancel": "Cancel"
  },
  "fr": {
    "username": "Utilisateur",
    "user": "Utilisateur",
    "send": "Envoyer",
    "cancel": "Annuler"
  }
}
</i18n>

<template>
  <div class="card">
    <div class="card-body">
      <form @submit.prevent="getUser">
        <div class="input-group mb-2">
          <div>
            <input
              v-model="new_user_name"
              v-focus
              type="email"
              class="form-control"
              :placeholder="'email '+$t('user')"
            >
          </div>
          <div class="input-group-append">
            <button
              class="btn btn-primary"
              type="submit"
              :disabled="!validEmail(new_user_name)"
            >
              {{ $t('send') }}
            </button>
            <button
              class="btn btn-secondary"
              type="reset"
              tabindex="0"
              @keyup.esc="new_user_name=''"
              @click="cancel"
            >
              {{ $t('cancel') }}
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { CurrentUser } from '@/mixins/currentuser.js';

export default {
  name: 'FormGetUser',
  mixins: [CurrentUser],
  data() {
    return {
      new_user_name: '',
    };
  },
  methods: {
    validEmail(email) {
      const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      return re.test(email);
    },
    getUser() {
      const headers = this.getHeaders();
      this.$store.dispatch('checkUser', { user: this.new_user_name, headers }).then((sub) => {
        if (!sub) this.$snotify.error('Sorry, unknown user');
        else {
          this.$emit('get-user', sub);
          this.new_user_name = '';
        }
      });
    },
    getHeaders() {
      if (this.authenticated) {
        return {
          Authorization: `Bearer ${this.currentuserKeycloakToken}`,
          Accept: 'application/json',
        };
      }
      return {
        Accept: 'application/json',
      };
    },
    cancel() {
      this.new_user_name = '';
      this.$emit('cancel-user');
    },
  },

};
</script>
