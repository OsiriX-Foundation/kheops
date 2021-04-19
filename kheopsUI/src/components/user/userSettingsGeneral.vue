<template>
  <div id="userSettingsGeneral">
    <form>
      <div class="row">
        <div class="col-xs-12 col-sm-4 col-md-4 col-lg-3 text-left text-sm-right">
          {{ $t('user.language') }}
        </div>
        <div class="col-xs-12 col-sm-8 col-md-8 col-lg-9 text-left mb-2">
          <select
            v-model="lang"
            class="form-control"
          >
            <option value="en">
              English
            </option>
            <option value="fr">
              Fançais
            </option>
          </select>
        </div>
      </div>
      <div
        v-if="disabledUserManagement"
        class="row mt-2"
      >
        <div class="col-xs-12 col-sm-4 col-md-4 col-lg-3 text-left text-sm-right">
          {{ $t('user.accountmanagement') }}
        </div>
        <div class="col-xs-12 col-sm-8 col-md-8 col-lg-9 text-left mb-2">
          <button
            type="button"
            class="btn btn-primary"
            @click="gomanagement()"
          >
            {{ $t('user.manage') }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
export default {
  name: 'UserSettingsGeneral',
  data() {
    return {
    };
  },
  computed: {
    lang: {
      get() {
        return this.$i18n.locale;
      },
      set(value) {
        this.$root.$i18n.locale = value;
      },
    },
    disabledUserManagement() {
      return process.env.VUE_APP_USER_MANAGEMENT !== undefined && !process.env.VUE_APP_USER_MANAGEMENT.includes('false');
    },
  },
  methods: {
    gomanagement() {
      window.open(process.env.VUE_APP_USER_MANAGEMENT);
    },
  },
};
</script>
