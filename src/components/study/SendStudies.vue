<i18n>
{
	"en": {
		"filesSend": "{count} file has been sent | {count} file has been sent | {count} files have been sent",
		"locationSend": "in your inbox. | in an",
		"album": "album.",
		"filesErrors": "{count} file produced an error. | {count} file produced an error. | {count} files produced an error.",
		"showError": "Show errors",
		"hideError": "Hide errors",
		"cancel": "Cancel",
		"titleBoxSending": "{msg} files send",
		"titleBoxSended": "{msg} files sended"
	},
	"fr": {
		"filesSend": "{count} fichier a été envoyé | {count} fichiers ont été envoyés",
		"locationSend": "dans votre boîte de réception. | dans un",
		"album": "album.",
		"filesErrors": "{count} fichier a rencontré une erreur. | {count} fichiers ont rencontré une erreur.",
		"showError": "Montrer les erreurs",
		"hideError": "Cacher les erreurs",
		"cancel": "Annuler",
		"titleBoxSending": "{msg} fichiers envoyés",
		"titleBoxSended": "{msg} fichiers ont été envoyés"
	}
}
</i18n>
<template>
  <div>
    <div
      v-if="show"
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
          class="p-2"
        >
          <span
            v-if="sending === true"
          >
            {{ $t("titleBoxSending", {msg: sentFiles}) }}
          </span>
          <span
            v-else-if="sending === false"
          >
            {{ $t("titleBoxSended", {msg: sentFiles}) }}
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
            @click="hide=!hide"
          >
            <span
              v-if="hide===false"
            >
              <remove-icon
                :height="'16'"
                :width="'16'"
              />
            </span>
            <span
              v-if="hide===true"
            >
              <add-icon
                :height="'16'"
                :width="'16'"
              />
            </span>
          </button>
          <!--
						Close icon
					-->
          <button
            type="button"
            class="btn btn-link btn-sm"
            @click="setShow()"
          >
            <close-icon
              :height="'16'"
              :width="'16'"
            />
          </button>
        </div>
      </div>
      <div
        v-if="hide === false"
        class="p-2"
      >
        <!--
					When sending
				-->
        <div
          v-if="files.length > 0 && sending === true"
        >
          <div
            v-if="cancel === false"
          >
            <b-progress-bar
              :value="sentFiles"
              :max="totalSize"
              show-progress
              animated
              style="text-align:center"
            >
              {{ sentFiles }} / {{ totalSize }}
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
                  :height="SVGheight"
                  :width="SVGwidth"
                  color="red"
                />
              </button>
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
        <!--
					When sending finish
				-->
        <div
          v-else-if="(show === true) && (sentFiles === totalSize || sending === false)"
          class="row"
        >
          <div
            class="col-12 mt-2 mb-2"
          >
            {{ $tc("filesSend", sentFiles - error.length, {count: (sentFiles - error.length)}) }}
            {{ $tc("locationSend", albumId !== '' ? 0 : 1) }}
            <span
              v-if="albumId !== ''"
            >
              <a
                href="#"
                @click="goToAlbum()"
              >
                {{ $t("album") }}
              </a>.
            </span>
            <div
              v-if="error.length > 0"
            >
              {{ $tc("filesErrors", error.length, {count: error.length}) }}
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
        <!--
					Show the errors
				-->
        <div
          v-if="error.length > 0 && showErrors"
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

export default {
	name: 'SendStudies',
	components: { ListErrorFiles, ErrorIcon, ClipLoader, BlockIcon, CloseIcon, AddIcon, RemoveIcon },
	props: {
	},
	data () {
		return {
			SVGheight: '20',
			SVGwidth: '20',
			SpinnerCancelSize: '30px',
			maxsize: 10e6,
			maxsend: 99,
			config: {
				headers: {
					'Accept': 'application/dicom+json'
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
			copyFiles: [],
			albumId: '',
			showErrors: false,
			cancel: false,
			show: false,
			hide: false
		}
	},
	computed: {
		...mapGetters({
			sending: 'sending',
			files: 'files',
			totalSize: 'totalSize',
			error: 'error',
			sentFiles: 'sentFiles'
		}),
		totalSizeFiles () {
			return this.files.reduce(function (total, currentValue) {
				return total + currentValue.content.size
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
			if (this.files.length === 0 && (this.totalSize - this.files === this.totalSize || this.cancel === true)) {
				this.cancel = false
				this.$emit('files-sending', false)
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
			this.$router.push('/albums/' + this.albumId + '?view=studies')
		},
		setShow () {
			this.show = !this.show
			this.cancel = true
		},
		setCancel () {
			this.cancel = !this.cancel
		},
		sendFiles () {
			this.initVariablesForSending()
			if (this.maxsize > this.totalSizeFiles && this.copyFiles.length < this.maxsend) {
				this.sendFormDataPromise(this.copyFiles)
			} else {
				this.sendBySize()
			}
		},
		initVariablesForSending () {
			this.show = true
			this.cancel = false
			this.albumId = this.$route.params.album_id ? this.$route.params.album_id : ''
			this.copyFiles = _.cloneDeep(this.files)

			this.$store.dispatch('setSending', { sending: true })
			this.$store.dispatch('initErrorFiles')
			this.$store.dispatch('initSentFiles')
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
				if (this.maxsize < state.size || ((index - state.tmpIndex) >= this.maxsend)) {
					const nextPromise = this.createNextPromise(state.tmpIndex, index + 1)
					promiseChain = promiseChain.then(nextPromise())
					state.tmpIndex = index + 1
					state.size = 0
				} else if (index === this.copyFiles.length - 1) {
					const nextPromise = this.createNextPromise(state.tmpIndex, this.copyFiles.length)
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
			return new Promise(resolve => {
				if (!this.cancel && this.files.length > 0) {
					let formData = this.createFormData(files)
					const request = `/studies${this.albumId ? '?album=' + this.albumId : ''}`
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
			this.$store.dispatch('setSentFiles', { sentFiles: files.length })
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
		setShowErrors (value) {
			this.showErrors = value
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
		max-width: 400px;
		opacity: 1;
		display: block;
	}
	.closeBtn {
		position: relative;
		border-bottom: 1px solid #f1f1f1;
	}
</style>
