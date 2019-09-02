
<template>
  <span>

    <span
      :class="classIconPN(study.flag.is_hover)"
      class="ml-1"
    >
      <a
        v-if="showDownloadIcon"
        href="#"
        class="download"
        @click.stop="getURLDownload()"
      >
        <v-icon
          class="align-middle"
          style="margin-right:1"
          name="download"
        />
      </a>
      <span
        v-if="OS.match(/(Mac|iPhone|iPod|iPad)/i) && showViewerIcon"
        class="ml-1"
        @click.stop="openViewer('Osirix')"
      >
        <osirix-icon
          width="22px"
          height="22px"
        />
      </span>
      <span
        v-if="study.ModalitiesInStudy[0] !== 'SR' && showViewerIcon"
        class="ml-1"
        @click.stop="openViewer('Ohif')"
      >
        <visibility-icon
          width="24px"
          height="24px"
        />
      </span>
      <label
        v-if="showImportIcon"
        for="file"
        style="cursor:pointer; display: inline;"
        class="ml-1"
        @click="setStudyUID()"
      >
        <add-icon
          width="24px"
          height="24px"
        />
      </label>
      <span
        v-if="showReportProviderIcon"
      >
        <slot name="reportprovider" />
      </span>
    </span>
		<span
      v-if="showCommentIcon"
      :class="study.flag.is_commented ? '' : classIconPN(study.flag.is_hover)"
      class="ml-1"
      @click.stop="showComments(study, 'comments')"
    >
      <v-icon
        class="align-middle"
        style="margin-right:1"
        name="comment-dots"
        :color="study.flag.is_commented ? '' : 'grey'"
      />
    </span>
    <span
      v-if="showFavoriteIcon"
      :class="study.flag.is_favorite ? '' : classIconPN(study.flag.is_hover)"
      class="ml-1"
      @click.stop="toggleFavorite()"
    >
      <v-icon
        class="align-middle"
        style="margin-right:1"
        name="star"
        :color="(!study.flag.is_favorite) ? 'grey' : ''"
      />
    </span>
  </span>
</template>
<script>
import OsirixIcon from '@/components/kheopsSVG/OsirixIcon.vue'
import VisibilityIcon from '@/components/kheopsSVG/VisibilityIcon.vue'
import AddIcon from '@/components/kheopsSVG/AddIcon'
import { ViewerToken } from '@/mixins/tokens.js'
import Vue from 'vue'

export default {
	name: 'ListIcons',
	components: { OsirixIcon, VisibilityIcon, AddIcon },
	mixins: [ ViewerToken ],
	props: {
		study: {
			type: Object,
			required: true,
			default: () => ({})
		},
		mobiledetect: {
			type: Boolean,
			required: true,
			default: false
		},
		albumId: {
			type: String,
			required: true,
			default: ''
		},
		showFavoriteIcon: {
			type: Boolean,
			required: false,
			default: true
		},
		showCommentIcon: {
			type: Boolean,
			required: false,
			default: true
		},
		showDownloadIcon: {
			type: Boolean,
			required: false,
			default: true
		},
		showViewerIcon: {
			type: Boolean,
			required: false,
			default: true
		},
		showImportIcon: {
			type: Boolean,
			required: false,
			default: true
		},
		showReportProviderIcon: {
			type: Boolean,
			required: false,
			default: true
		}
	},
	data () {
		return {
		}
	},
	computed: {
		OS () {
			return navigator.platform
		},
		access_token () {
			return Vue.prototype.$keycloak.token
		}
	},

	watch: {
		studies: {
			handler: function (studies) {
				if (studies.length > 0) {
					this.UI.loading = false
				}
			}
		}
	},

	created () {
	},
	mounted () {
	},
	methods: {
		getSource () {
			if (this.albumId === '') {
				return {
					inbox: true
				}
			} else {
				return {
					album: this.albumId
				}
			}
		},
		classIconPN (visibility) {
			if (visibility || this.mobiledetect) {
				return 'iconsHover'
			} else {
				return 'iconsUnhover'
			}
		},
		toggleFavorite () {
			let params = {
				StudyInstanceUID: this.study.StudyInstanceUID.Value[0],
				queries: this.getSource(),
				value: !this.study.flag.is_favorite
			}
			this.$store.dispatch('favoriteStudy', params)
		},
		getURLDownload () {
			const source = this.albumId === '' ? 'inbox' : this.albumId
			const StudyInstanceUID = this.study.StudyInstanceUID.Value[0]
			this.getViewerToken(this.access_token, StudyInstanceUID, source).then(res => {
				const queryparams = `accept=application%2Fzip&${source === 'inbox' ? 'inbox=true' : 'album=' + source}`
				const URL = `${process.env.VUE_APP_URL_API}/link/${res.data.access_token}/studies/${StudyInstanceUID}?${queryparams}`
				location.href = URL
			}).catch(err => {
				console.log(err)
			})
		},
		openViewer (viewer) {
			const StudyInstanceUID = this.study.StudyInstanceUID.Value[0]
			const source = this.albumId === '' ? 'inbox' : this.albumId
			let ohifWindow
			if (viewer === 'Ohif') {
				ohifWindow = window.open('', 'OHIFViewer')
			}
			this.getViewerToken(this.access_token, StudyInstanceUID, source).then(res => {
				if (viewer === 'Osirix') {
					this.openOsiriX(StudyInstanceUID, res.data.access_token)
				} else if (viewer === 'Ohif') {
					this.openOhif(StudyInstanceUID, res.data.access_token, source === 'inbox' ? 'inbox=true' : 'album=' + source, ohifWindow)
				} else if (viewer === 'Weasis') {
					this.openWeasis(StudyInstanceUID, res.data.access_token)
				}
			}).catch(err => {
				console.log(err)
			})
		},
		openOsiriX (StudyInstanceUID, token) {
			let url = `${process.env.VUE_APP_URL_API}/link/${token}/studies/${StudyInstanceUID}?accept=application/zip`
			window.open(`osirix://?methodName=downloadURL&URL='${encodeURIComponent(url)}'`, '_self')
		},
		openWeasis (StudyInstanceUID, token) {
			let url = `$dicom:get --zip ${process.env.VUE_APP_URL_API}/link/${token}/studies/${StudyInstanceUID}?accept=application/zip`
			window.open(`weasis://?${encodeURIComponent(url)}`, '_self')
		},
		openOhif (StudyInstanceUID, token, queryparams, ohifWindow) {
			let url = `${process.env.VUE_APP_URL_API}/studies/${StudyInstanceUID}/ohifmetadata?${queryparams}`
			ohifWindow.location.href = `${process.env.VUE_APP_URL_VIEWER}/?url=${encodeURIComponent(url)}#token=${token}`
		},
		showComments (study, flagView) {
			let params = {
				StudyInstanceUID: study.StudyInstanceUID.Value[0],
				flag: 'view',
				value: flagView
			}
			this.$store.dispatch('setFlagByStudyUID', params)
			this.$store.dispatch('setShowDetails', {
				StudyInstanceUID: study.StudyInstanceUID.Value[0],
				value: !study._showDetails
			})
		},
		setStudyUID () {
			const StudyInstanceUID = this.study.StudyInstanceUID.Value[0]
			this.$store.dispatch('setStudyUIDtoSend', { studyUID: StudyInstanceUID })
		}
	}
}
</script>

<style scoped>
	.iconsHover{
		visibility: visible;
		display: inline;
		cursor: pointer;
	}
	.iconsUnhover{
		visibility: hidden;
		display: inline;
		cursor: pointer;
	}
	a.download{
		color: #FFF;
	}

	a.download:hover{
		color: #fd7e14;
	}
</style>
