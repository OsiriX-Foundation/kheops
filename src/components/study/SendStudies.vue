<i18n>
{
	"en": {
		"filesSend": "{count} file has been send. | {count} files has been send.",
		"filesErrors": "{count} file occur an error. | {count} files occur an error.",
		"showError": "Show errors",
		"hideError": "Hide errors",
		"cancel": "Cancel"
	},
	"fr": {
		"filesSend": "{count} fichier a été envoyé. | {count} fichiers ont été envoyés.",
		"filesErrors": "{count} fichier a rencontré une erreur. | {count} fichiers ont rencontré une erreur.",
		"showError": "Montrer les erreurs",
		"hideError": "Cacher les erreurs",
		"cancel": "Annuler"
	}
}
</i18n>
<template>
  <div class="container">
    <div
      v-if="copyFiles.length > 0 && sendingFiles === true"
    >
      <div
        v-if="cancel === false"
      >
        <div class="row">
          <div
            class="col-12"
          >
            <b-progress-bar
              :value="lengthFilesSended"
              :max="copyFiles.length"
              show-progress
              animated
              style="text-align:center"
            >
              {{ lengthFilesSended }} / {{ copyFiles.length }}
            </b-progress-bar>
          </div>
          <div
            class="col-2"
          >
            <button
              type="button"
              class="btn btn-link btn-sm text-center"
              style="color: red"
              @click="cancel=!cancel"
            >
              <span>
                {{ $t("cancel") }}
              </span>
              <block-icon
                :height="SVGheight"
                :width="SVGwidth"
                color="red"
              />
            </button>
          </div>
        </div>
      </div>
      <div
        v-else
      >
        <clip-loader
          :loading="cancel"
          :size="SpinnerCancelSize"
          :color="'red'"
        />
      </div>
    </div>
    <div
      v-else-if="copyFiles.length > 0 && (copyFiles.length === lengthFilesSended || sendingFiles === false)"
    >
      <div class="row">
        <div
          class="col-12"
        >
          {{ $tc("filesSend", lengthFilesSended - errorFiles.length, {count: (lengthFilesSended - errorFiles.length)}) }}
          <div
            v-if="errorFiles.length > 0"
          >
            {{ $tc("filesErrors", errorFiles.length, {count: errorFiles.length}) }}
            <button
              type="button"
              class="btn btn-link btn-sm text-center"
              style="color: red"
              @click="showErrors=!showErrors"
            >
              <span v-if="!showErrors">
                {{ $t("showError") }}
              </span>
              <span v-else>
                {{ $t("hideError") }}
              </span>
              <error-icon
                :height="SVGheight"
                :width="SVGwidth"
                color="red"
              />
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="errorFiles.length > 0 && showErrors"
    >
      <list-error-files
        :error-files="errorFiles"
        @show-errors="setShowErrors"
      />
    </div>
  </div>
</template>

<script>
import { HTTP } from '@/router/http'
import ListErrorFiles from '@/components/study/ListErrorFiles'
import ErrorIcon from '@/components/kheopsSVG/ErrorIcon.vue'
import ClipLoader from 'vue-spinner/src/ClipLoader.vue'
import BlockIcon from '@/components/kheopsSVG/BlockIcon'

export default {
	name: 'SendStudies',
	components: { ListErrorFiles, ErrorIcon, ClipLoader, BlockIcon },
	props: {
		files: {
			type: Array,
			required: true,
			default: () => []
		},
		sendingFiles: {
			type: Boolean,
			required: true,
			default: false
		}
	},
	data () {
		return {
			SVGheight: '20',
			SVGwidth: '20',
			SpinnerCancelSize: '30px',
			errorFiles: [],
			maxsize: 10e7,
			maxsend: 100,
			copyFiles: [],
			config: {
				headers: {
					'Accept': 'application/dicom+json'
				}
			},
			showErrors: false,
			errorValues: {
				292: 'Authorization Error',
				272: 'Non DICOM file'
			},
			errorDicom: [
				{
					'key': '0008119A',
					'value': '00041500'
				},
				{
					'key': '00081198',
					'value': '00041500'
				}
			],
			cancel: false,
			lengthFilesSended: 0
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
		sendingFiles () {
			if (this.sendingFiles === true) {
				this.cancel = false
				this.sendFiles()
			}
		},
		files () {
			if (this.files.length === 0 && (this.copyFiles.length === this.lengthFilesSended || this.cancel === true)) {
				this.cancel = false
				this.$emit('files-sending', false)
			}
		}
	},
	created () {
	},
	mounted () {
	},
	methods: {
		sendFiles () {
			this.initVariables()
			if (this.maxsize > this.totalSizeFiles && this.copyFiles.length < this.maxsend) {
				this.sendFormDataPromise(this.copyFiles)
			} else {
				this.sendBySize()
			}
		},
		initVariables () {
			this.errorFiles = []
			this.copyFiles = _.cloneDeep(this.files)
			this.lengthFilesSended = 0
		},
		sendBySize () {
			let state = {
				size: 0,
				tmpIndex: 0
			}
			// https://stackoverflow.com/questions/48014050/wait-promise-inside-for-loop
			let promiseChain = Promise.resolve()

			this.copyFiles.forEach(async (file, index) => {
				state.size += file.content.size
				if (this.maxsize < state.size || (((index + 1) % this.maxsend === 0) && index !== 0)) {
					const nextPromise = this.createNextPromise(state.tmpIndex, index + 1)
					promiseChain = promiseChain.then(nextPromise())
					state.tmpIndex = index + 1
					state.size = 0
				}
			})

			if (state.tmpIndex <= this.copyFiles.length) {
				const nextPromise = this.createNextPromise(state.tmpIndex, this.copyFiles.length)
				promiseChain = promiseChain.then(nextPromise())
			}
		},
		createNextPromise (firstIndex, secondIndex) {
			let currentFiles = this.getArrayFilesToSend(firstIndex, secondIndex)
			const nextPromise = () => () => {
				return this.sendFormDataPromise(currentFiles)
			}
			return nextPromise
		},
		getArrayFilesToSend (firstIndex, secondIndex) {
			if (firstIndex === secondIndex) {
				return [this.copyFiles[secondIndex]]
			} else {
				return this.copyFiles.slice(firstIndex, secondIndex)
			}
		},
		sendFormDataPromise (files) {
			return new Promise(resolve => {
				if (!this.cancel) {
					let formData = this.createFormData(files)
					const request = `/studies${this.$route.params.album_id ? '?album=' + this.$route.params.album_id : ''}`

					HTTP.post(request, formData, this.config).then(res => {
						this.manageResult(files, res.data)
						resolve(res)
					}).catch(res => {
						this.manageResult(files, res)
						resolve(res)
					})
				} else {
					this.removeFilesId(files)
					resolve('removeFiles')
				}
			})
		},
		manageResult (files, data) {
			this.getErrorsDicomFromResponse(data)
			this.removeFilesId(files)
			this.lengthFilesSended += files.length
		},
		createFormData (files) {
			let formData = new FormData()
			files.forEach((file) => {
				formData.append(file.id, file.content)
			})
			return formData
		},
		getErrorsDicomFromResponse (res) {
			this.errorDicom.forEach((error) => {
				this.errDicom(res, error.key, error.value)
			})
		},
		errDicom (res, iderr, idvalue) {
			if (res.hasOwnProperty(iderr)) {
				const err = this.dicom2map(res[iderr].Value, idvalue)
				err.forEach((errorCode, id) => {
					const fileError = this.copyFiles.find(file => { return file.id === id })
					if (fileError) {
						this.errorFiles.push(
							this.createObjErrors(fileError.path, this.errorValues[errorCode])
						)
					}
				})
			}
		},
		dicom2map (dicom, id) {
			let map = new Map()
			dicom.forEach(x => {
				if (x.hasOwnProperty(id)) {
					map.set(x[id].Value[0], x['00081197'].Value[0])
				}
			})
			return map
		},
		createObjErrors (id, value) {
			return {
				'id': id,
				'value': value
			}
		},
		removeFilesId (files) {
			files.forEach((val) => {
				this.removeFileId(val.id)
			})
		},
		removeFileId (id) {
			let index = this.files.findIndex(x => x.id === id)
			this.files.splice(index, 1)
		},
		setShowErrors (value) {
			this.showErrors = value
		}
	}
}
</script>

<style scoped>
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
</style>
