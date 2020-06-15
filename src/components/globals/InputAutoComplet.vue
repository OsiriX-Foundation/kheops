<template>
  <span>
    <input
      id="userslist-choice"
      ref="inputList"
      v-model="user"
      v-focus
      type="email"
      :placeholder="placeholder"
      list="userslist"
      name="userslist-choice"
      class="form-control"
      autocomplete="off"
    >
    <datalist
      id="userslist"
      ref="userslist"
    >
      <option
        v-for="searchuser in users"
        :key="searchuser.key"
        :value="searchuser.email !== undefined ? searchuser.email : 'email undefined'"
      />
    </datalist>
  </span>
</template>

<script>
import { HTTP } from '@/router/http';
import httpoperations from '@/mixins/httpoperations';

export default {
  name: 'InputAutoComplet',
  props: {
    placeholder: {
      type: String,
      required: false,
      default: '',
    },
  },
  data() {
    return {
      queryParams: {
        search: '',
        limit: 3,
        offset: 0,
      },
      charminlength: 3,
      users: [],
      user: '',
      cpt: 0,
    };
  },
  watch: {
    user: {
      handler() {
        this.inputValue();
        if (this.user.length >= this.charminlength) {
          this.searchUser(this.user);
        } else {
          this.users = [];
        }
      },
    },
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
  methods: {
    searchUser(user) {
      this.queryParams.search = user;
      this.cpt += 1;
      const tmpcpt = this.cpt;
      this.search().then((res) => {
        if (tmpcpt === this.cpt) {
          this.users = res.data;
        }
      });
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
