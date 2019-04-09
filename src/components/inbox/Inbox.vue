<template>
  <div>
    <manage-import-send
      :scope="scope"
    />
  </div>
</template>

<script>
import ManageImportSend from '@/components/study/ManageImportSend'

export default {
	name: 'Inbox',
	components: { ManageImportSend },
	props: {
	},
	data () {
		return {
			scope: 'inbox'
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
	}
}

</script>
