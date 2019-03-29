<template>
  <div id="file-drag-drop">
    <input
      id="file"
      ref="inputfiles"
      type="file"
      name="file"
      class="inputfile"
      allowdirs
      multiple
      @change="inputLoadFiles"
    >
    <form
      ref="fileform"
      class="fileform"
    >
      <label
        ref="dragdrop"
        class="drag-drop"
        for="file"
        :class="{'drag-drop-hover': hover }"
      >
        Load your files
      </label>
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
              {{ file.path }}
            </div>
            <div
              class="col-sm-auto"
            >
              <span
                v-if="file.state.sendFiles && !file.state.done"
              >
                <clip-loader
                  :loading="file.state.sendFiles"
                  :color="colorSpinner"
                  :size="sizeSpinner"
                />
              </span>
              <span
                v-else-if="file.state.done"
              >
                <v-icon
                  color="green"
                  class="align-middle"
                  name="check"
                />
              </span>
              <span
                v-else
              >
                <button
                  v-if="file.state.err"
                  type="button"
                  class="btn btn-link btn-sm text-center"
                >
                  <span>
                    <error-icon
                      :height="'22'"
                      :width="'22'"
                      color="red"
                    />
                  </span>
                </button>
                <button
                  type="button"
                  class="btn btn-link btn-sm text-center"
                  @click="removeFileName(file.name)"
                >
                  <span>
                    <v-icon
                      color="red"
                      class="align-middle"
                      name="trash"
                    />
                  </span>
                </button>
              </span>
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
import { HTTP } from '@/router/http'
import ErrorIcon from '@/components/kheopsSVG/ErrorIcon.vue'

export default {
	name: 'DragAndDrop',
	components: { ClipLoader, ErrorIcon },
	data () {
		return {
			dragAndDropCapable: false,
			files: [],
			colorSpinner: 'white',
			sizeSpinner: '24px',
			hover: false,
			maxsize: 10e6
		}
	},
	computed: {
		totalSizeFiles () {
			return this.files.reduce(function (total, currentValue) {
				return total + currentValue.content.size
			}, 0)
		}
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
				if (this.hover) this.hover = false
				console.time('loadFiles')
				for (let i = 0; i < e.dataTransfer.items.length; i++) {
					var entry = e.dataTransfer.items[i].webkitGetAsEntry()
					if (entry.isFile) {
						entry.file(function (file) {
							this.loadFile(file, file.name)
						}.bind(this))
					} else if (entry.isDirectory) {
						this.loadDir(entry, [])
					}
				}
				console.timeEnd('loadFiles')
			}.bind(this))

			this.$refs.dragdrop.addEventListener('dragenter', function (e) {
				if (e.target.className === 'drag-drop') {
					this.hover = true
				}
			}.bind(this))

			this.$refs.dragdrop.addEventListener('dragleave', function (e) {
				if (e.target.className === 'drag-drop drag-drop-hover') {
					this.hover = false
				}
			}.bind(this))
		}
	},
	methods: {
		determineDragAndDropCapable () {
			var div = document.createElement('div')
			return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) && 'FormData' in window && 'FileReader' in window
		},
		loadFile (file, path, name) {
			let objFile = {
				'content': file,
				'state': {
					done: false,
					sendFiles: false,
					err: false
				},
				'path': path,
				'name': name
			}
			this.files.push(objFile)
		},
		inputLoadFiles () {
			let files = this.$refs.inputfiles.files
			for (let i = 0; i < files.length; i++) {
				this.loadFile(files[i], files[i].webkitRelativePath ? files[i].webkitRelativePath : files[i].name, files[i].name)
			}
		},
		loadDir (dir, parentDir) {
			if (dir.isDirectory) {
				let directoryReader = dir.createReader()
				this.readDir(directoryReader)
			} else {
				dir.file(function (file) {
					this.loadFile(file, `${parentDir}`, file.name)
				}.bind(this))
			}
		},
		readDir (directoryReader) {
			directoryReader.readEntries(function (entries) {
				if (entries.length) {
					entries.forEach(function (entry) {
						this.loadDir(entry, entry.fullPath)
					}.bind(this))
					this.readDir(directoryReader)
				}
			}.bind(this))
		},
		sendFiles () {
			const config = {
				headers: {
					'Accept': 'application/dicom+json'
				}
			}
			if (this.maxsize > this.totalSizeFiles) {
				this.sendFormDataPromise(this.files, config)
			} else {
				this.sendBySize(this.files, config)
			}
		},
		sendBySize (files, config) {
			let state = {
				size: 0,
				tmpIndex: 0
			}
			const copyFiles = files
			// https://stackoverflow.com/questions/48014050/wait-promise-inside-for-loop
			let promiseChain = Promise.resolve()
			copyFiles.forEach((file, index) => {
				state.size += file.content.size
				if (state.size > this.maxsize) {
					console.log(state.size)
					let currentFiles = copyFiles.slice(state.tmpIndex, index)
					const makeNextPromise = () => () => {
						return this.sendFormDataPromise(currentFiles, config)
					}
					promiseChain = promiseChain.then(makeNextPromise())
					state.tmpIndex = index
					state.size = 0
				}
			})
			if (state.tmpIndex < copyFiles.length) {
				let currentFiles = copyFiles.slice(state.tmpIndex, copyFiles.length)
				const makeNextPromise = () => () => {
					return this.sendFormDataPromise(currentFiles, config)
				}
				promiseChain = promiseChain.then(makeNextPromise())
			}
		},
		createFormData (files) {
			let formData = new FormData()
			files.forEach((file) => {
				file.state.sendFiles = true
				if (!file.state.done) {
					formData.append(file.content.name, file.content)
				}
			})
			return formData
		},
		/*
		sendFormData (files, config) {
			let formData = this.createFormData(files)
			HTTP.post('/studies', formData, config).then(res => {
				if (res.status === 200) {
					formData.forEach((val) => {
						this.removeFileName(val.name)
					})
				}
				if (res.status === 202) {
					this.errDicom(formData, res.data, '0008119A', '00041500')
					this.errDicom(formData, res, '00081198', '00081150')
				}
				return res
			}).catch(res => {
				this.errDicom(formData, res, '0008119A', '00041500')
				this.errDicom(formData, res, '00081198', '00081150')
			})
		},
		*/
		sendFormDataPromise (files, config) {
			return new Promise((resolve, reject) => {
				let formData = this.createFormData(files)
				HTTP.post('/studies', formData, config).then(res => {
					if (res.status === 200) {
						formData.forEach((val) => {
							this.removeFileName(val.name)
						})
					}
					if (res.status === 202) {
						this.errDicom(formData, res.data, '0008119A', '00041500')
						this.errDicom(formData, res, '00081198', '00081150')
					}
					resolve(res)
				}).catch(res => {
					this.errDicom(formData, res, '0008119A', '00041500')
					this.errDicom(formData, res, '00081198', '00081150')
					resolve(res)
				})
			})
		},
		errDicom (formData, res, iderr, idvalue) {
			if (res.hasOwnProperty(iderr)) {
				let err = this.dicom2array(res[iderr].Value, idvalue)
				formData.forEach((val) => {
					if (err.indexOf(val.name) === -1) {
						this.removeFileName(val.name)
					}
				})
				this.files.forEach((val) => {
					val.state.sendFiles = false
					val.state.err = true
				})
			}
		},
		dicom2array (dicom, id) {
			let tab = []
			dicom.forEach(x => {
				tab.push(x[id].Value[0])
			})
			return tab
		},
		removeFileName (filename) {
			let index = this.files.findIndex(x => x.name === filename)
			this.files.splice(index, 1)
		}
	}
}
</script>

<style scoped>
  form{
    display: block;
    height: 100px;
    width: 400px;
    margin: auto;
    margin-top: 40px;
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
  .inputfile {
    width: 0.1px;
    height: 0.1px;
    opacity: 0;
    overflow: hidden;
    position: absolute;
    z-index: -1;
  }
  .drag-drop {
    display: block;
    height: 100px;
    width: 400px;
    margin: auto;
    text-align: center;
    line-height: 100px;
    border-radius: 4px;
    background: rgb(163, 161, 161);
		border: 2px dotted black;
    cursor: pointer;
  }
	.drag-drop-hover {
    background: #ccc;
		border: 4px black;
	}
</style>
