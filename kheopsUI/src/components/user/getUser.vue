<template>
  <div class="card">
    <div class="card-body">
      <form @submit.prevent="getUser">
        <div class="input-group mb-2">
          <div>
            <input-auto-complet
              :placeholder="$t('user.emailuser')"
              :reset="resetUser"
              @input-reset="setResetInput(false)"
              @input-value="setUsername"
            />
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
import { mapGetters } from 'vuex';
import InputAutoComplet from '@/components/globals/InputAutoComplet';
import { CurrentUser } from '@/mixins/currentuser.js';

export default {
  name: 'FormGetUser',
  components: { InputAutoComplet },
  mixins: [CurrentUser],
  data() {
    return {
      new_user_name: '',
      resetUser: false,
    };
  },
  computed: {
    ...mapGetters('oidcStore', [
      'oidcIsAuthenticated',
    ]),
  },
  methods: {
    validEmail(email) {
      const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      return re.test(email);
    },
    getUser() {
      const headers = this.getHeaders();
      this.$store.dispatch('checkUser', { user: this.new_user_name, headers }).then((sub) => {
        if (!sub) this.$snotify.error(this.$t('user.usernotfound'));
        else {
          this.$emit('get-user', sub);
          this.new_user_name = '';
          this.setResetInput(true);
        }
      });
    },
    getHeaders() {
      if (this.oidcIsAuthenticated) {
        return {
          Authorization: `Bearer ${this.currentuserAccessToken}`,
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
    setUsername(username) {
      this.new_user_name = username;
    },
    setResetInput(value) {
      this.resetUser = value;
    },
  },

};
</script>
