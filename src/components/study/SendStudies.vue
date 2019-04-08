<i18n>
{
	"en": {
		"filesSend": "{count} file has been send. | {count} files has been send.",
		"filesErrors": "{count} file occur an error. | {count} files occur an error.",
		"showError": "Show errors",
		"hideError": "Hide errors"
	},
	"fr": {
		"filesSend": "{count} fichier a été envoyé. | {count} fichiers ont été envoyés.",
		"filesErrors": "{count} fichier a rencontré une erreur. | {count} fichiers ont rencontré une erreur.",
		"showError": "Montrer les erreurs",
		"hideError": "Cacher les erreurs"
	}
}
</i18n>
<template>
  <div>
    <div
      v-if="files.length > 0 && progressBarVal !== lengthFilesSend"
    >
      <b-progress-bar
        :value="progressBarVal"
        :max="lengthFilesSend"
        show-progress
        animated
        style="text-align:center"
      >
        {{ progressBarVal }} / {{ lengthFilesSend }}
      </b-progress-bar>
    </div>
    <div
      v-else-if="lengthFilesSend !== 0"
    >
      {{ $tc("filesSend", lengthFilesSend - errorFiles.length, {count: (lengthFilesSend - errorFiles.length)}) }}
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

export default {
	name: 'SendStudies',
	components: { ListErrorFiles, ErrorIcon },
	props: {
		files: {
			type: Array,
			required: true,
			default: () => []
		},
		lengthFilesSend: {
			type: Number,
			required: true,
			default: 0
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
			}
		}
	},
	computed: {
		totalSizeFiles () {
			return this.files.reduce(function (total, currentValue) {
				return total + currentValue.content.size
			}, 0)
		},
		progressBarVal () {
			let currentVal = this.lengthFilesSend - (this.files.length)
			return currentVal < 0 ? 0 : currentVal
		}
	},
	watch: {
		sendingFiles () {
			if (this.sendingFiles) {
				this.sendFiles()
			}
		},
		files () {
			if (this.files.length === 0) {
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
			this.copyFiles = _.cloneDeep(this.files)
			if (this.maxsize > this.totalSizeFiles && this.copyFiles.length < this.maxsend) {
				this.sendFormDataPromise(this.copyFiles)
			} else {
				this.sendBySize()
			}
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
					let currentFiles = this.getArrayFilesToSend(state.tmpIndex, index + 1)
					const makeNextPromise = () => () => {
						return this.sendFormDataPromise(currentFiles)
					}
					promiseChain = promiseChain.then(makeNextPromise())
					state.tmpIndex = index + 1
					state.size = 0
				}
			})
			if (state.tmpIndex <= this.copyFiles.length) {
				let currentFiles = this.copyFiles.slice(state.tmpIndex, this.copyFiles.length)
				const makeNextPromise = () => () => {
					return this.sendFormDataPromise(currentFiles)
				}
				promiseChain = promiseChain.then(makeNextPromise())
			}
		},
		getArrayFilesToSend (firstIndex, secondIndex) {
			if (firstIndex === secondIndex) {
				return [this.copyFiles[secondIndex]]
			} else {
				return this.copyFiles.slice(firstIndex, secondIndex)
			}
		},
		createFormData (files) {
			let formData = new FormData()
			files.forEach((file) => {
				formData.append(file.id, file.content)
			})
			return formData
		},
		sendFormDataPromise (files) {
			return new Promise((resolve, reject) => {
				let formData = this.createFormData(files)
				HTTP.post('/studies', formData, this.config).then(res => {
					if (res.status === 200) {
						formData.forEach((val) => {
							this.removeFileName(val.name)
						})
					}
					if (res.status === 202) {
						this.errDicom(formData, res.data, '0008119A', '00041500')
						this.errDicom(formData, res.data, '00081198', '00041500')
					}
					resolve(res)
				}).catch(res => {
					this.errDicom(formData, res, '0008119A', '00041500')
					this.errDicom(formData, res, '00081198', '00041500')
					resolve(res)
				})
			})
		},
		errDicom (formData, res, iderr, idvalue) {
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
				formData.forEach((val) => {
					this.removeFileName(val.name)
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
		initVariables () {
			this.errorFiles = []
		},
		removeFileName (filename) {
			let index = this.files.findIndex(x => x.name === filename)
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
