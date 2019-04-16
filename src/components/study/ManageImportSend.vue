<template>
  <div>
    <component-import-study
      :scope="scope"
      :album="album"
    />
  </div>
</template>


<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy'

export default {
	name: 'ManageImportSend',
	components: { ComponentImportStudy },
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
