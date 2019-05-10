<i18n>
{
	"en": {
		"filesSend": "{count} files have been sent | {count} file has been sent | {count} files have been sent",
		"locationSend": "in your inbox. | in an",
		"album": "album.",
		"filesErrors": "{count} files produced an error. | {count} file produced an error. | {count} files produced an error.",
		"showError": "Show errors",
		"hideError": "Hide errors",
		"cancel": "Cancel",
		"titleBoxSending": "Sending files",
		"titleBoxSended": "Files sent",
		"unknownError": "{count} unknown file produced this error : | {count} unknown files produced this error :"
	},
	"fr": {
		"filesSend": "{count} fichier a été envoyé | {count} fichier a été envoyé | {count} fichiers ont été envoyés",
		"locationSend": "dans votre boîte de réception. | dans un",
		"album": "album.",
		"filesErrors": "{count} fichier a rencontré une erreur. | {count} fichier a rencontré une erreur. | {count} fichiers ont rencontré une erreur.",
		"showError": "Montrer les erreurs",
		"hideError": "Cacher les erreurs",
		"cancel": "Annuler",
		"titleBoxSending": "Fichiers en cours d'envois",
		"titleBoxSended": "Fichiers envoyés",
		"unknownError": "{count} fichier inconnu a produit cette erreur : | {count} fichiers inconnus ont produit cette erreur :"
	}
}
</i18n>
<template>
  <div>
    <div
      v-if="UI.show"
      class="chat-popup container-fluid p-0"
    >
      <div
        class="closeBtn d-flex"
      >
        <div
          v-if="sending === true"
          class="p-2"
        >
          <clip-loader
            :loading="sending"
            :size="'20px'"
            :color="'white'"
          />
        </div>
        <div
          v-if="sending === false"
          class="p-2"
        >
          <done-icon
            :height="'20'"
            :width="'20'"
          />
        </div>
        <div
          class="p-2"
        >
          <span
            v-if="sending === true"
          >
            {{ $t("titleBoxSending") }}
          </span>
          <span
            v-else-if="sending === false"
          >
            {{ $t("titleBoxSended") }}
          </span>
        </div>
        <div
          class="ml-auto p-1"
        >
          <!--
						Reduce / Show icon
					-->
          <button
            type="button"
            class="btn btn-link btn-sm"
            @click="UI.hide=!UI.hide"
          >
            <span
              v-if="UI.hide===false"
            >
              <remove-icon
                :height="UI.SVGHeaderHeight"
                :width="UI.SVGHeaderWidth"
              />
            </span>
            <span
              v-if="UI.hide===true"
            >
              <add-icon
                :height="UI.SVGHeaderHeight"
                :width="UI.SVGHeaderWidth"
              />
            </span>
          </button>
        </div>
        <!--
						Close icon
					-->
        <div
          class="p-1"
        >
          <button
            type="button"
            class="btn btn-link btn-sm"
            @click="closeWindow()"
          >
            <close-icon
              :height="UI.SVGHeaderHeight"
              :width="UI.SVGHeaderWidth"
            />
          </button>
        </div>
      </div>
      <div
        v-if="UI.hide === false"
        class="p-2"
      >
        <!--
					When sending
				-->
        <div
          v-if="files.length > 0 && sending === true"
        >
          <div
            v-if="UI.cancel === false"
          >
            <b-progress-bar
              :value="countSentFiles+progress"
              :max="totalSize"
              show-progress
              animated
              style="text-align:center"
            >
              {{ countSentFiles }} / {{ totalSize }}
            </b-progress-bar>
            <div
              class="d-flex justify-content-center mt-1 mb-1"
            >
              <button
                type="button"
                class="btn btn-link btn-sm text-center"
                style="color: red"
                @click="setCancel()"
              >
                <span>
                  {{ $t("cancel") }}
                </span>
                <block-icon
                  :height="UI.SVGheight"
                  :width="UI.SVGwidth"
                  color="red"
                />
              </button>
            </div>
          </div>
          <div
            v-else
          >
            <clip-loader
              :loading="UI.cancel"
              :size="UI.SpinnerCancelSize"
              :color="'red'"
            />
          </div>
        </div>
        <!--
					When sending finish
				-->
        <div
          v-else-if="(UI.show === true) && (countSentFiles === totalSize || sending === false)"
          class="row"
        >
          <div
            class="col-12 mt-2 mb-2"
          >
            {{ $tc("filesSend", countSentFiles - error.length - totalUnknownFilesError, {count: (countSentFiles - error.length - totalUnknownFilesError)}) }}
            {{ $tc("locationSend", source !== 'inbox' ? 0 : 1) }}
            <span
              v-if="source !== 'inbox'"
            >
              <a
                href="#"
                @click="goToAlbum()"
              >
                {{ $t("album") }}
              </a>
            </span>
            <div
              v-if="Object.keys(listErrorUnknownFiles).length > 0"
            >
              <div
                v-for="(item, key) in listErrorUnknownFiles"
                :key="item.key"
              >
                {{ $tc("unknownError", item, {count: item }) }} <br>
                <span style="color: red">
                  {{ errorValues[key] }}
                </span>
              </div>
            </div>

            <div
              v-if="error.length > 0"
            >
              {{ $tc("filesErrors", error.length, {count: error.length}) }}
              <button
                type="button"
                class="btn btn-link btn-sm text-center"
                style="color: red"
                @click="UI.showErrors=!UI.showErrors"
              >
                <span v-if="!UI.showErrors">
                  {{ $t("showError") }}
                </span>
                <span v-else>
                  {{ $t("hideError") }}
                </span>
                <error-icon
                  :height="UI.SVGheight"
                  :width="UI.SVGwidth"
                  color="red"
                />
              </button>
            </div>
          </div>
        </div>
        <!--
					Unknow files error
				-->

        <!--
					Show the errors
				-->
        <div
          v-if="error.length > 0 && UI.showErrors"
          class="row"
        >
          <div
            class="col-12 mt-2 mb-2"
          >
            <list-error-files
              :error-files="error"
              @show-errors="setShowErrors"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { HTTP } from '@/router/http'
import ListErrorFiles from '@/components/study/ListErrorFiles'
import ErrorIcon from '@/components/kheopsSVG/ErrorIcon.vue'
import ClipLoader from 'vue-spinner/src/ClipLoader.vue'
import BlockIcon from '@/components/kheopsSVG/BlockIcon'
import CloseIcon from '@/components/kheopsSVG/CloseIcon'
import AddIcon from '@/components/kheopsSVG/AddIcon'
import RemoveIcon from '@/components/kheopsSVG/RemoveIcon'
import DoneIcon from '@/components/kheopsSVG/DoneIcon'

export default {
	name: 'SendStudies',
	components: { ListErrorFiles, ErrorIcon, ClipLoader, BlockIcon, CloseIcon, AddIcon, RemoveIcon, DoneIcon },
	props: {
	},
	data () {
		return {
			UI: {
				show: false,
				hide: false,
				cancel: false,
				showErrors: false,
				SVGheight: '20',
				SVGwidth: '20',
				SVGHeaderHeight: '16',
				SVGHeaderWidth: '16',
				SpinnerCancelSize: '30px'
			},
			maxsize: 10e6,
			maxsend: 99,
			config: {
				headers: {
					'Accept': 'application/dicom+json'
				},
				onUploadProgress: progressEvent => {
					this.progress = this.currentFilesLength * (progressEvent.loaded / progressEvent.total)
				}
			},
			errorValues: {
				292: 'Authorization Error',
				272: 'Non DICOM file'
			},
			errorDicom: {
				'0008119A': '00041500',
				'00081198': '00041500'
			},
			dicomTagError: '00081197',
			listErrorUnknownFiles: {},
			totalUnknownFilesError: 0,
			progress: 0,
			copyFiles: [],
			countSentFiles: 0
		}
	},
	computed: {
		...mapGetters({
			sending: 'sending',
			files: 'files',
			totalSize: 'totalSize',
			error: 'error',
			source: 'source'
		}),
		totalSizeFiles () {
			return this.copyFiles.reduce(function (total, file) {
				return total + file.content.size
			}, 0)
		}
	},
	watch: {
		sending () {
			if (this.sending === true) {
				this.sendFiles()
			}
		},
		files () {
			if (this.files.length === 0 && (this.countSentFiles === this.totalSize || this.UI.cancel === true)) {
				this.UI.cancel = false
				this.$store.dispatch('setSending', { sending: false })
			}
		}
	},
	created () {
	},
	mounted () {
	},
	destroyed () {
	},
	methods: {
		goToAlbum () {
			this.$router.push(`/albums/${this.source}?view=studies`)
		},
		closeWindow () {
			this.UI.show = !this.UI.show
			this.UI.cancel = true
		},
		setCancel () {
			this.UI.cancel = !this.UI.cancel
		},
		sendFiles () {
			this.initVariablesForSending()
			if (this.maxsize > this.totalSizeFiles && this.copyFiles.length <= this.maxsend) {
				this.sendFormDataPromise(this.copyFiles)
			} else {
				this.sendBySize(this.copyFiles)
			}
		},
		initVariablesForSending () {
			this.UI.show = true
			this.UI.cancel = false
			this.countSentFiles = 0
			this.copyFiles = _.cloneDeep(this.files)
			this.progress = 0
			this.listErrorUnknownFiles = {}
			this.totalUnknownFilesError = 0

			this.$store.dispatch('setSending', { sending: true })
			this.$store.dispatch('initErrorFiles')
		},
		sendBySize (files) {
			let state = {
				size: 0,
				tmpIndex: 0
			}
			// https://stackoverflow.com/questions/48014050/wait-promise-inside-for-loop
			let promiseChain = Promise.resolve()

			files.forEach(async (file, index) => {
				state.size += file.content.size
				if (this.maxsize < state.size || ((index - state.tmpIndex) >= this.maxsend)) {
					const nextPromise = this.createNextPromise(state.tmpIndex, index + 1)
					promiseChain = promiseChain.then(nextPromise())
					state.tmpIndex = index + 1
					state.size = 0
				} else if (index === files.length - 1) {
					const nextPromise = this.createNextPromise(state.tmpIndex, files.length)
					promiseChain = promiseChain.then(nextPromise())
				}
			})
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
			return new Promise((resolve) => {
				if (!this.UI.cancel && this.files.length > 0) {
					let formData = this.createFormData(files)
					this.currentFilesLength = files.length
					const request = `/studies${this.source !== 'inbox' ? '?album=' + this.source : ''}`
					HTTP.post(request, formData, this.config).then(res => {
						this.manageResult(files, res.data)
						resolve(res)
					}).catch(res => {
						this.manageResult(files, res)
						resolve(res)
					})
				} else if (this.files.length > 0) {
					this.$store.dispatch('initFiles')
					resolve('removeFiles')
				} else {
					resolve('noFiles')
				}
			})
		},
		manageResult (files, data) {
			this.getErrorsDicomFromResponse(data)
			this.$store.dispatch('removeFilesId', { files: files })
			this.countSentFiles += files.length
		},
		createFormData (files) {
			let formData = new FormData()
			files.forEach((file) => {
				formData.append(file.id, file.content)
			})
			return formData
		},
		getErrorsDicomFromResponse (res) {
			for (var key in this.errorDicom) {
				if (res.hasOwnProperty(key)) {
					const errorInResponse = this.dicom2map(res[key].Value, this.errorDicom[key])
					this.generateListError(res[key].Value, this.errorDicom[key])
					this.createListError(errorInResponse)
				}
			}
		},
		createListError (error) {
			error.forEach((errorCode, id) => {
				const fileError = this.copyFiles.find(file => { return file.id === id })
				if (fileError) {
					this.$store.dispatch('setErrorFiles', { error: this.createObjErrors(fileError.path, this.errorValues[errorCode]) })
				}
			})
		},
		generateListError (dicom, dicomTagFile) {
			dicom.forEach(x => {
				if (!x.hasOwnProperty(dicomTagFile)) {
					let errorCode = x[this.dicomTagError].Value[0]
					if (this.listErrorUnknownFiles.hasOwnProperty(errorCode)) {
						this.listErrorUnknownFiles[errorCode] += 1
					} else {
						this.listErrorUnknownFiles[errorCode] = 1
					}
					this.totalUnknownFilesError += 1
				}
			})
		},
		dicom2map (dicom, dicomTagFile) {
			let map = new Map()
			dicom.forEach(x => {
				if (x.hasOwnProperty(dicomTagFile)) {
					let errorCode = x[this.dicomTagError].Value[0]
					let idFile = x[dicomTagFile].Value[0]
					map.set(idFile, errorCode)
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
		setShowErrors (value) {
			this.UI.showErrors = value
		}
	}
}
</script>

<style scoped>
	.chat-popup {
		position: fixed;
		background: #303030;
		bottom: 0;
		right: 15px;
		border: 3px solid #f1f1f1;
		z-index: 9;
		max-width: 300px;
		opacity: 1;
		display: block;
	}
	.closeBtn {
		position: relative;
		border-bottom: 1px solid #f1f1f1;
	}
</style>
