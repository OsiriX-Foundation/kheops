<template>
  <div id="file-drag-drop">
    <form
      v-if="dragAndDropCapable"
      ref="fileform"
      class="fileform"
    >
      Drop the files here!
    </form>
    <div
      v-if="files.length > 0"
    >
      <div
        class="files-listing"
      >
        <div
          v-for="(file, index) in files"
          :key="index"
          class="file-listing"
        >
          <div
            class="row align-items-center"
          >
            <div
              class="col"
            >
              {{ manageFiles[index].dir }}{{ file.name }}
            </div>
            <div
              class="col-sm-auto"
            >
              <span
                v-if="manageFiles[index].sendFiles && !manageFiles[index].done"
              >
                <clip-loader
                  :loading="manageFiles[index].sendFiles"
                  :color="colorSpinner"
                  :size="sizeSpinner"
                />
              </span>
              <span
                v-else-if="manageFiles[index].done"
              >
                <v-icon
                  color="green"
                  class="align-middle"
                  name="check"
                />
              </span>
              <button
                v-else
                type="button"
                class="btn btn-link btn-sm text-center"
                @click="removeFile(index)"
              >
                <span>
                  <v-icon
                    color="red"
                    class="align-middle"
                    name="trash"
                  />
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
      <div
        class="container-btn"
      >
        <div
          class="col text-center"
        >
          <button
            type="button"
            class="btn btn-primary"
            @click="sendFiles"
          >
            Send
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ClipLoader from 'vue-spinner/src/ClipLoader.vue'
import axios from 'axios'

export default {
	name: 'DragAndDrop',
	components: { ClipLoader },
	data () {
		return {
			dragAndDropCapable: true,
			files: [],
			manageFiles: [],
			colorSpinner: 'white',
			sizeSpinner: '24px',
			hover: false
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
				let _this = this
				for (let i = 0; i < e.dataTransfer.items.length; i++) {
					var entry = e.dataTransfer.items[i].webkitGetAsEntry()
					if (entry.isFile) {
						entry.file(function (file) {
							_this.loadFile(file, '')
						})
					} else if (entry.isDirectory) {
						this.loadDir(entry, [])
					}
				}
			}.bind(this))

			this.$refs.fileform.addEventListener('dragenter', function (e) {
				if (e.target.className === 'fileform') {
					this.classList.add('form-hover')
				}
			})

			this.$refs.fileform.addEventListener('dragleave', function (e) {
				if (e.target.className === 'fileform form-hover') {
					this.classList.remove('form-hover')
				}
			})
		}
	},
	methods: {
		determineDragAndDropCapable () {
			var div = document.createElement('div')
			return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) && 'FormData' in window && 'FileReader' in window
		},
		loadFile (file, parentDir) {
			this.files.push(file)
			this.manageFiles.push({
				dir: parentDir,
				done: false,
				sendFiles: false
			})
		},
		loadDir (dir, parentDir) {
			let _this = this
			if (dir.isDirectory) {
				parentDir.push(dir.name)
				let directoryReader = dir.createReader()
				directoryReader.readEntries(function (entries) {
					entries.forEach(function (entry) {
						_this.loadDir(entry, parentDir)
					})
				})
			} else {
				dir.file(function (file) {
					_this.loadFile(file, `${parentDir.join('/')}/`)
				})
			}
		},
		removeFile (index) {
			this.files.splice(index, 1)
			this.manageFiles.splice(index, 1)
		},
		sendFiles () {
			this.files.forEach((file, index) => {
				if (!this.manageFiles[index].done) {
					this.sendFile(file, index)
				}
			})
		},
		sendFile (file, index) {
			let formData = new FormData()
			this.manageFiles[index].sendFiles = true
			formData.append(file.name, file)
			setTimeout(() => {
				this.manageFiles[index].done = true
			}, Math.floor(Math.random() * 3000) + 2000)
			/*
			axios.post('/api/studies',
				formData,
				{
					headers: {
						'Content-Type': 'type="application/dicom"'
					}
				}
			).then(function () {
				console.log('Send, done is true')
			}).catch(function () {
				console.log('Error')
			})
			*/
		}
	}
}
</script>

<style scoped>
  form{
    display: block;
    height: 100px;
    width: 400px;
    background: rgb(163, 161, 161);
		border: 2px dotted black;
    margin: auto;
    margin-top: 40px;
    text-align: center;
    line-height: 100px;
    border-radius: 4px;
  }
	.form-hover {
    background: #ccc;
		border: 4px black;
	}
	.file-listing{
    width: 380px;
    padding: 10px;
    border-bottom: 1px solid #ddd;
	}
  .files-listing{
    width: 400px;
    margin: auto;
    max-height: 400px;
    overflow: auto;
  }
  .container-btn{
    padding: 10px;
  }
</style>
