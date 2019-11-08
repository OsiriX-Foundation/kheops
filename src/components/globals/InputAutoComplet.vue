<template>
  <span>
    <input
      v-model="user"
      type="email"
      autofocus
      :placeholder="'email '+$t('user')"
      list="userslist"
      name="userslist"
      ref="inputList"
      class="form-control"
    >
    <label for="userslist">
      <datalist
        id="userslist"
        ref="userslist"
      >
        <option v-if="users.length < minlength"></option>
        <option
          v-for="user in users"
          :id="user.id"
          :value="user.email !== undefined ? user.email : ''"
        >
          {{ user.email !== undefined ? user.email : 'no value' }}
        </option>
      </datalist>
    </label>
  </span>
</template>

<script>
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'InputAutoComplet',
  props: {
  },
  data() {
    return {
      queryParams: {
        search: '',
        limit: 3,
        offset: 0,
      },
      minlength: 1,
      users: [],
      user: '',
      cpt: 0,
    };
  },
  mounted() {
    // https://stackoverflow.com/questions/57202606/show-autocomplete-only-after-3-entered-chars-in-datalist-field
    /*
    this.$refs.inputList.addEventListener('keyup', (e) => {
      console.log(this.minlength)
      if (e.target.value.length >= this.minlength) {
        this.$refs.userslist.setAttribute("id", "userslist");
      } else {
        this.$refs.userslist.setAttribute("id", "");
      }
    });
    */
  },
  watch: {
    user: {
      handler() {
        this.inputValue()
        if (this.user.length >= this.minlength) {
          this.searchUser(this.user);
        } else {
          this.users = []
        }
      },
    },
  },
  methods: {
    searchUser(user) {
      this.queryParams.search = user
      this.cpt += 1
      const tmpcpt = this.cpt
      this.search().then(res => {
        if (tmpcpt === this.cpt) {
          this.users = res.data
        }
      })
    },
    search() {
      const request = `users`;
      const queryParams = httpoperations.getFormData(this.queryParams);
      return HTTP.get(`${request}?${queryParams}`, { headers: { Accept: 'application/json' } });
    },
    inputValue() {
      this.$emit('input-value', this.user);
    },
  },
};

</script>