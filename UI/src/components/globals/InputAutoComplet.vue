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
      v-if="submit === true"
      id="userslist-choice"
      ref="inputList"
      v-model="user"
      type="email"
      :placeholder="placeholder"
      :disabled="disabled"
      :list="'userslist'+listID"
      name="userslist-choice"
      class="form-control"
      autocomplete="off"
      @keydown.enter="pressedEnter"
    >
    <input
      v-else
      id="userslist-choice"
      ref="inputList"
      v-model="user"
      type="email"
      :placeholder="placeholder"
      :disabled="disabled"
      :list="'userslist'+listID"
      name="userslist-choice"
      class="form-control"
      autocomplete="off"
      @keydown.enter.prevent="pressedEnter"
    >
    <datalist
      v-if="canAutoComplet === true"
      :id="'userslist'+listID"
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
    reset: {
      type: Boolean,
      required: false,
      default: false,
    },
    focus: {
      type: Boolean,
      required: false,
      default: true,
    },
    submit: {
      type: Boolean,
      required: false,
      default: true,
    },
  },
  data() {
    return {
      queryParams: {
        search: '',
        limit: 5,
        offset: 0,
      },
      charminlength: 3,
      users: [],
      user: '',
      cpt: 0,
    };
  },
  computed: {
    listID() {
      return Math.floor((Math.random() * 10000) + 1);
    },
    canAutoComplet() {
      let canAutoComplet = true;
      if (process.env.VUE_APP_DISABLE_AUTOCOMPLET !== undefined) {
        canAutoComplet = !process.env.VUE_APP_DISABLE_AUTOCOMPLET.includes('true');
      }
      return canAutoComplet;
    },
  },
  watch: {
    user: {
      handler() {
        this.inputValue();
        if (this.user.length >= this.charminlength && this.canAutoComplet === true) {
          this.searchUser(this.user);
        } else {
          this.users = [];
        }
      },
    },
    disabled: {
      handler() {
        if (this.disabled === false) {
          this.inputFocus();
        }
      },
    },
    reset: {
      handler() {
        if (this.reset === true) {
          this.inputReset();
        }
      },
    },
  },
  created() {
    this.addContext();
  },
  mounted() {
    if (this.focus === true) {
      this.inputFocus();
    }
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
    inputReset() {
      this.user = '';
      this.$emit('input-reset');
    },
    inputFocus() {
      const { inputList } = this.$refs;
      setTimeout(() => { inputList.focus(); }, 0);
    },
  },
};
</script>
