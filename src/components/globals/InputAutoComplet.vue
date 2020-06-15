<!--
    context: object to define the context of the search.
      - type: Object
      - required: false
      - default: {}
      Structure of the object:
        {
          key: queryparameter key (example album, studyInstanceUID)
          value: queryparameter value
        }
-->
<template>
  <span>
    <input
      id="userslist-choice"
      ref="inputList"
      v-model="user"
      v-focus
      type="email"
      :placeholder="placeholder"
      :disabled="disabled"
      list="userslist"
      name="userslist-choice"
      class="form-control"
      autocomplete="off"
      @keydown.enter.prevent="pressedEnter"
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
    context: {
      type: Object,
      required: false,
      default: () => ({}),
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false,
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
    disabled: {
      handler() {
        if (this.disabled === false) {
          const { inputList } = this.$refs;
          setTimeout(() => { inputList.focus(); }, 0);
        }
      },
    },
  },
  created() {
    this.addContext();
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
    addContext() {
      if (Object.keys(this.context).length > 0 && this.context.constructor === Object) {
        this.queryParams[this.context.key] = this.context.value;
      }
    },
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
      const request = 'users';
      const queryParams = httpoperations.getFormData(this.queryParams);
      return HTTP.get(`${request}?${queryParams}`, { headers: { Accept: 'application/json' } });
    },
    inputValue() {
      this.$emit('input-value', this.user);
    },
    pressedEnter() {
      this.$emit('keydown-enter-prevent');
    },
  },
};
</script>
