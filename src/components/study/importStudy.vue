<template>
  <div id="file-drag-drop">
    <form ref="fileform">
      <span class="drop-files">
        Drop the files here!
      </span>
    </form>
    <div
      v-for="(file, key) in files"
      :key="key"
      class="file-listing"
    >
      {{ file.name }}
    </div>
  </div>
</template>

<script>
export default {
	name: 'DragAndDrop',
	data () {
		return {
			dragAndDropCapable: false,
			files: []
		}
	},
	computed: {

	},
	watch: {

	},
	created () {

	},
	mounted () {
		this.dragAndDropCapable = this.determineDragAndDropCapable()
		if (this.dragAndDropCapable) {
			// For each event add an event listener that prevents the default action
			['drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'drop'].forEach(function (evt) {
				this.$refs.fileform.addEventListener(evt, function (e) {
					e.preventDefault()
					e.stopPropagation()
				}, false)
			}.bind(this))

			// Capture the files from the drop event and add them to local files array
			this.$refs.fileform.addEventListener('drop', function (e) {
				for (let i = 0; i < e.dataTransfer.files.length; i++) {
					this.files.push(e.dataTransfer.files[i])
				}
			}.bind(this))
		}
	},
	methods: {
		determineDragAndDropCapable () {
			var div = document.createElement('div')
			return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) && 'FormData' in window && 'FileReader' in window
		}
	}
}
</script>

<style scoped>
  form{
    display: block;
    height: 100px;
    width: 400px;
    background: #ccc;
    margin: auto;
    margin-top: 40px;
    text-align: center;
    line-height: 100px;
    border-radius: 4px;
  }
	div.file-listing{
    width: 400px;
    margin: auto;
    padding: 10px;
    border-bottom: 1px solid #ddd;
	}

	div.file-listing img{
		height: 100px;
	}
</style>
