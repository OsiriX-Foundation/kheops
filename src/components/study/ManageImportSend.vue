<template>
  <div>
    <div class="container-fluid">
      <send-studies
        :files="files"
        :sending-files="sendingFiles"
        @files-sending="setSendingFiles"
      />
    </div>
    <component-import-study
      :sending-files="sendingFiles"
      :scope="scope"
      :album="album"
      @files-loaded="loadFiles"
    />
  </div>
</template>


<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy'
import SendStudies from '@/components/study/SendStudies'

export default {
	name: 'ManageImportSend',
	components: { ComponentImportStudy, SendStudies },
	props: {
		scope: {
			type: String,
			required: true,
			default: ''
		},
		album: {
			type: Object,
			required: false,
			default: () => ({})
		}
	},
	data () {
		return {
			files: [],
			lengthFiles: 0,
			sendingFiles: false
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
	methods: {
		loadFiles (files) {
			this.sendingFiles = true
			this.files = files
		},
		setSendingFiles (sendingFiles) {
			this.sendingFiles = sendingFiles
		}
	}
}
</script>
