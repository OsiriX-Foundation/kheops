<template>
  <div class="card container-fluid my-3">
    <div class="card-body">
      <div class="row align-items-center">
        <p class="col-xl-auto mb-1">
          {{ text }}
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
			default: ''
		},
		btnPrimaryText: {
			type: String,
			required: true,
			default: ''
		},
		btnDangerText: {
			type: String,
			required: true,
			default: ''
		},
		methodConfirm: {
			type: Function,
			required: true,
			default: () => -1
		},
		methodCancel: {
			type: Function,
			required: true,
			default: () => -1
		}
	}
}
</script>
