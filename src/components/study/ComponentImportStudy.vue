<i18n>
{
	"en": {
		"cantUpload": "You can't upload when files are sending",
		"upload": "Drop your files / directories !",
		"cantUploadAlbum": "You can't load files in this album.",
		"cancel": "Cancel"
	},
	"fr": {
		"cantUpload": "Vous ne pouvez pas charger d'autres fichiers pendant un envoi.",
		"upload": "Lâcher vos fichiers / dossiers !",
		"cantUploadAlbum": "Vous ne pouvez pas charger des fichiers dans cet album.",
		"cancel": "Annuler"
	}
}
</i18n>

<template>
  <div
    id="file-drag-drop"
    ref="filedragdrop"
  >
    <!--
			https://stackoverflow.com/questions/34817656/add-class-in-drop-area-file-input-when-dragging-an-external-image-over-dragen
		-->
    <form
      id="fileform"
      ref="fileform"
      :class="['fileform', hover ? 'dragenterFormClass' : '']"
    >
      <div
        id="dragingcomponenet"
        ref="dragingcomponenet"
        class="dragingcomponenet"
      >
        <div
          v-if="hover"
          class="outPopUp"
        >
          <p
            v-if="albumNoPermission"
          >
            {{ $t("cantUploadAlbum") }}
          </p>
          <p
            v-else-if="sending && files.length > 0"
          >
            <span>
              {{ $t("cantUpload") }}
            </span>
          </p>
          <p
            v-else
          >
            {{ $t("upload") }}
          </p>
        </div>
        <div
          v-if="loading"
          class="outPopUp"
        >
          <clip-loader
            :loading="loading"
            :size="'60px'"
            :color="'white'"
          />
        </div>
        <div
          :class="['dropzone-area', classDragIn]"
        >
          <list
            ref="list"
            :album="album"
            @loadfiles="inputLoadFiles"
            @loaddirectories="inputLoadFiles"
            @demohover="demoHover"
          />
        </div>
      </div>
    </form>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import List from '@/components/inbox/List'
import ClipLoader from 'vue-spinner/src/ClipLoader.vue'
import mobiledetect from '@/mixins/mobiledetect.js'

export default {
	name: 'ComponentDragAndDrop',
	components: { List, ClipLoader },
	props: {
		album: {
			type: Object,
			required: false,
			default: () => ({})
		}
	},
	data () {
		return {
			dragAndDropCapable: false,
			hover: false,
			excludeFiles: ['DICOMDIR'],
			count: 0,
			counterDraging: 0,
			loading: false
		}
	},
	computed: {
		...mapGetters({
			sending: 'sending',
			files: 'files'
		}),
		albumNoPermission () {
			return !(this.album.is_admin || this.album.add_series) && this.scope === 'album'
		},
		classDragIn () {
			if (!mobiledetect.mobileAndTabletcheck()) {
				if (this.hover || this.loading) {
					return 'dragenterClass'
				} else {
					return 'dragNotEnterFormClass'
				}
			} else {
				return ''
			}
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
			this.$refs.fileform.addEventListener('drop', async function (e) {
				if (this.hover) this.hover = false
				if (!this.sending && !this.loading && !this.albumNoPermission) {
					this.loading = true
					this.manageDataTransfer(e.dataTransfer.items)
				}
				this.counterDraging--
			}.bind(this))

			/*
			https://stackoverflow.com/questions/7110353/html5-dragleave-fired-when-hovering-a-child-element
			//$refs.dragingcomponenet
			*/
			this.$refs.fileform.addEventListener('dragenter', function (e) {
				this.counterDraging += 1
				this.hover = true
			}.bind(this))

			this.$refs.fileform.addEventListener('dragleave', function (e) {
				this.counterDraging--
				if (this.counterDraging === 0) {
					this.hover = false
				}
			}.bind(this))
		}
	},
	methods: {
		demoHover () {
			this.hover = true
			setTimeout(() => this.hover = false, 1500)
		},
		storeFiles (files) {
			this.$store.dispatch('setSending', { sending: true })
			this.$store.dispatch('setFiles', { files: files })
			this.$store.dispatch('setSource', { source: this.album.album_id !== undefined ? this.album.album_id : 'inbox' })
		},
		createObjFiles (file, path, name) {
			if (!this.excludeFileName(name)) {
				const objFile = {
					'content': file,
					'path': path,
					'name': name,
					'id': this.count.toString(16)
				}
				this.count++
				return objFile
			}
		},
		inputLoadFiles (filesFromInput) {
			let arrayFiles = []
			for (let i = 0; i < filesFromInput.length; i++) {
				const pathFile = filesFromInput[i].webkitRelativePath ? filesFromInput[i].webkitRelativePath : filesFromInput[i].name
				const objFile = this.createObjFiles(filesFromInput[i], pathFile, filesFromInput[i].name)
				if (objFile) {
					arrayFiles.push(objFile)
				}
			}
			this.storeFiles(arrayFiles)
		},
		manageDataTransfer (dataTransferItems) {
			const arrayPromises = []
			for (let i = 0; i < dataTransferItems.length; i++) {
				let entry = this.determineGetAsEntry(dataTransferItems[i])
				if (entry && entry.isFile) {
					arrayPromises.push(this.readFilePromise(entry, this))
				} else if (entry && entry.isDirectory) {
					arrayPromises.push(this.readDirectoryPromise(entry, this))
				}
			}

			Promise.all(arrayPromises).then(res => {
				const filesSend = this.removeNonObjectFiles(this.arrayFlatten(res))
				this.loading = false
				this.storeFiles(filesSend)
			})
		},
		removeNonObjectFiles (array) {
			return array.filter(val => { return (val) !== undefined })
		},
		arrayFlatten (array) {
			return array.reduce(function (arrayFlat, arrayToFlatten) {
				return arrayFlat.concat(Array.isArray(arrayToFlatten) ? this.arrayFlatten(arrayToFlatten) : arrayToFlatten)
			}.bind(this), [])
		},
		readFilePromise (entry, _this) {
			return new Promise(function (resolve, reject) {
				entry.file(function (file) {
					resolve(_this.createObjFiles(file, entry.fullPath, file.name))
				})
			})
		},
		// https://stackoverflow.com/questions/18815197/javascript-file-dropping-and-reading-directories-asynchronous-recursion
		readDirectoryPromise (readerDir, _this) {
			let reader = readerDir.createReader()
			return new Promise((resolve) => {
				var iterationAttempts = [];
				(function readEntriesRecursive () {
					reader.readEntries((entries) => {
						if (!entries.length) {
							resolve(Promise.all(iterationAttempts))
						} else {
							iterationAttempts.push(Promise.all(entries.map((entry) => {
								if (entry.isFile) {
									return _this.readFilePromise(entry, _this)
								} else {
									return _this.readDirectoryPromise(entry, _this)
								}
							})))
							readEntriesRecursive()
						}
					})
				})()
			})
		},
		determineDragAndDropCapable () {
			let div = document.createElement('div')
			return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) && 'FormData' in window && 'FileReader' in window
		},
		determineGetAsEntry (item) {
			if (item.getAsEntry !== undefined) {
				return item.getAsEntry()
			} else if (item.webkitGetAsEntry !== undefined) {
				return item.webkitGetAsEntry()
			}
		},
		excludeFileName (name) {
			return this.excludeFiles.indexOf(name) > -1
		}
	}
}
</script>

<style scoped>
	.dragenterClass {
		opacity: 0.5;
	}
	.dragenterFormClass {
		border: 5px dotted green !important;
	}
	.dragNotEnterFormClass {
		border: 5px dotted transparent !important;
	}
	.outPopUp {
		position: fixed;
		z-index: 15;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		font-size:45px;
	}
</style>
