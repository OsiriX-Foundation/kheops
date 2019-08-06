<template>
    <v-icon
        v-if="currentHeader !== sortBy"
        class="align-middle"
        name="unfold_more"
    />
    <v-icon
        v-else
        class="align-middle"
        :name="sortDesc ? 'chevron-up' : 'chevron-down'"
    />
</template>
<script>
export default {
	name: 'SortList',
	components: { },
	mixins: [ ],
	props: {
		sortBy: {
			type: String,
			required: true,
			default: ""
        },
        currentHeader: {
			type: String,
			required: true,
			default: ""
        },
        sortDesc: {
            type: Boolean,
            required: true,
            default: false
        }
	},
}
</script>
