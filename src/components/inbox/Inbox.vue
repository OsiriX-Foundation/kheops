<template>
  <div>
    <component-import-study
      :scope="scope"
    />
  </div>
</template>

<script>
import ComponentImportStudy from '@/components/study/ComponentImportStudy'

export default {
	name: 'Inbox',
	components: { ComponentImportStudy },
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
