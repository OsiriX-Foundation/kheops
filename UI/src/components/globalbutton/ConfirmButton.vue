<template>
  <div class="card container-fluid my-3">
    <div class="card-body">
      <div class="row align-items-center">
        <p class="col-xl-auto mb-1">
          {{ text }}
          <span v-if="showDeleteContactEmail === true">
            <br>
            <b>{{ textDeleteContact }}</b>
          </span>
        </p>
        <div class="col-auto col-sm-auto mb-1">
          <button
            type="button"
            class="btn btn-primary"
            @click="methodConfirm"
          >
            {{ btnPrimaryText }}
          </button>
        </div>
        <div class="col-auto col-sm-auto mb-1">
          <button
            type="button"
            class="btn btn-danger"
            @click="methodCancel"
          >
            {{ btnDangerText }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'ConfirmButton',
  props: {
    text: {
      type: String,
      required: true,
      default: '',
    },
    textDeleteContact: {
      type: String,
      required: false,
      default: '',
    },
    btnPrimaryText: {
      type: String,
      required: true,
      default: '',
    },
    btnDangerText: {
      type: String,
      required: true,
      default: '',
    },
    methodConfirm: {
      type: Function,
      required: true,
      default: () => -1,
    },
    methodCancel: {
      type: Function,
      required: true,
      default: () => -1,
    },
  },
  computed: {
    showDeleteContactEmail() {
      let showDeleteContactEmail = false;
      if (process.env.VUE_APP_SHOW_DELETE_CONTACT !== undefined) {
        showDeleteContactEmail = process.env.VUE_APP_SHOW_DELETE_CONTACT.includes('true');
      }
      return showDeleteContactEmail;
    },
  },
};
</script>
