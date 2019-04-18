<!--
		errorFiles: Array of the error to show
			- type: Array
			- required: true
			- default: []

		this component show the file path present in the array errorFiles
    send emit "remove-file-ui" to the parent when :
			- when the user delete an error file
-->

<template>
  <div>
    <div
      class="files-listing"
    >
      <div
        v-for="(error, index) in errorFiles"
        :key="index"
        class="file-listing"
      >
        <div
          class="row"
        >
          <div
            class="col-12"
          >
            <!--
              https://css-tricks.com/almanac/properties/o/overflow-wrap/
            -->
            {{ error.id }}
          </div>
          <div
            class="col-sm-auto"
            style="color: red"
          >
            {{ error.value }}
          </div>
        </div>
      </div>
    </div>
    <div
      class="mt-2 d-flex justify-content-center"
    >
      <button
        class="btn btn-primary"
        @click="hideErrors"
      >
        <span>Hide errors</span>
      </button>
    </div>
  </div>
</template>

<script>

export default {
	name: 'ListErrorFiles',
	props: {
		errorFiles: {
			type: Array,
			required: true,
			default: () => []
		}
	},
	data () {
		return {
			height: '22',
			width: '22'
		}
	},
	computed: {
	},
	watch: {
	},
	created () {
	},
	mounted () {
	},
	destroyed () {
		this.$emit('show-errors', false)
	},
	methods: {
		hideErrors () {
			this.$emit('show-errors', false)
		}
	}
}
</script>

<style scoped>
  .file-listing{
    width: 250px;
    padding: 10px;
    border-bottom: 1px solid #ddd;
    overflow-wrap: break-word;
	}
  .files-listing{
    width: 260px;
    margin: auto;
    max-height: 400px;
    overflow: auto;
  }
  .container-btn{
    padding: 10px;
  }
  .inputfile {
    width: 0.1px;
    height: 0.1px;
    opacity: 0;
    overflow: hidden;
    position: absolute;
    z-index: -1;
  }
</style>
