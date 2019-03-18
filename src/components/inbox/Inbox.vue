<template>
  <div>
    <input-import-study />
    <list />
  </div>
</template>

<script>
import List from '@/components/inbox/List'
import InputImportStudy from '@/components/study/InputImportStudy'

export default {
	name: 'Inbox',
	components: { List, InputImportStudy },
	props: {
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
